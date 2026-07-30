# FeedLine 动态流网络接入设计

> **文档版本**：3.5  
> **更新日期**：2026-07-30  
> **适用模块**：`shared/src/commonMain/.../feedline`  
> **关联架构规范**：[网络架构设计](../../architecture/网络架构设计.md) & [数据同步与离线策略](../../architecture/数据同步与离线策略.md)

---

## 1. 模块边界与 ID 标识规范

本文档描述 `FeedLine`（动态流）模块如何遵循全项目通用网络与同步规范。
- **必须**：Repository 对外只能暴露领域类型、领域命令及 `AppResult`，禁止暴露 DTO、Entity、`NetworkResult` 或 Ktor 类型。
- **ID 标识统一规范**：
  - **`localId`**：本地数据库稳态主键 (UUID)，UI 层及 Repository 内部追踪实体的唯一凭证；
  - **`serverId`**：服务端分配的权威唯一 ID（所有 Remote DTO 中的 `id` / `serverId` 均由 `@SerialName` 对应 JSON 中的字段）；
  - **点赞约束**：Repository 的 `setLiked(localPostId, desiredLiked)` 接收 `localPostId`。若解析出的实体 `serverId == null`（即尚未在服务端创建成功的本地帖子），**禁止发起远程网络请求**；
  - **合并去重约束**：远程分页结果按 `serverId` 去重，本地临时帖子按 `localId` 保留。

---

## 2. API 契约与 DTO

### 2.1 简洁 REST 接口映射表

| 方法 | 路径 | 语义 | 幂等控制 |
|---|---|---|---|
| **GET** | `/feed` | 游标分页读取动态流 | 引擎幂等重试 |
| **PUT/DELETE** | `/posts/{serverId}/likes/me` | 幂等设置/取消点赞状态 | `Idempotency-Key` Header |
| **POST** | `/posts` | 发布新动态 | `Idempotency-Key` Header |

### 2.2 DTO 模型 (使用 `@SerialName` 保证 JSON 反序列化全兼容)
```kotlin
package org.example.project.data.remote.feedline.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FeedUserDto(
    @SerialName("id") val serverId: String,
    val name: String,
    val avatarUrl: String? = null
)

@Serializable
data class FeedPostDto(
    @SerialName("id") val serverId: String,
    val author: FeedUserDto,
    val content: String,
    val likeCount: Long,
    val commentCount: Long,
    val likedByCurrentUser: Boolean,
    val createdAtEpochMillis: Long,
)

@Serializable
data class FeedPageDto(
    val items: List<FeedPostDto>,
    val nextCursor: String? = null,
    val hasMore: Boolean = false,
)

@Serializable
data class FeedReactionDto(
    @SerialName("postId") val serverPostId: String,
    val liked: Boolean,
    val likeCount: Long
)

@Serializable
data class CreatePostRequest(val content: String, val mediaIds: List<String> = emptyList())
```

---

## 3. 领域模型与数据 Pipeline

### 3.1 领域发帖命令
```kotlin
package org.example.project.domain.command.feedline

/**
 * 领域发帖命令
 * @param mediaIds 表示已完成上传并由服务端返回的媒体 ID（上传流程遵循 UploadClient 独立规范）
 */
data class CreatePostCommand(
    val content: String,
    val mediaIds: List<String> = emptyList(),
)
```

### 3.2 映射 Pipeline 规范
- **正式主链路**：`DTO → Entity / 关系表 → Database Flow → Domain`。

---

## 4. Repository 接口

```kotlin
interface FeedRepository {
    fun observeFeed(): Flow<List<FeedLinePost>>
    suspend fun refreshFeed(): AppResult<Unit>
    suspend fun loadNextPage(): AppResult<Unit>
    suspend fun setLiked(localPostId: String, desiredLiked: Boolean): AppResult<Unit>
    suspend fun createPost(command: CreatePostCommand): AppResult<Unit>
}
```

---

## 5. 刷新与分页规范

### 5.1 下拉刷新 (`refreshFeed`)
- **必须**：下拉刷新调用 `localDataSource.mergeRemoteFeed(remoteItems = items, preservePending = true)`。
- **约束**：远程实体按 `serverId` 去重更新，**禁止覆盖 Pending 状态中的字段**；本地临时帖子按 `localId` 保留。

### 5.2 游标分页 (`loadNextPage`)
- **必须**：使用 `Mutex` 锁防止并发重复触发分页。
- **必须**：按 `serverId` 去重追加。
- **必须**：仅当数据库写入成功后再更新 `nextCursor`；网络失败时保留原 `cursor`。刷新成功后重置分页状态。

---

## 6. 点赞状态写入流程 (`setLiked`)

```text
Repository 接收 localPostId
→ MutationSequencer 串行排队
→ 同一准备事务：解析 serverId、校验 (若 serverId==null 拒绝调用)、读取旧字段、写入乐观状态 (Pending)、写入 Outbox 与持久化 rollbackPayload，返回 PreparedLikeMutation(operationId, serverId, rollbackPatch)
→ 本地更新并写入队列完成，即可向 UI 返回 AppResult.PendingSync
→ 发起 HTTP 请求 (PUT/DELETE /posts/{serverId}/likes/me)
→ 判定 RequestOutcome：
  ├─ Success：同一事务确认实体状态为 Synced，更新 Outbox 为 Succeeded
  ├─ Rejected (如 400/403)：同一事务字段回滚，更新 Outbox 为 Cancelled
  └─ Unknown (如 Timeout/连接中断)：同一事务维持 Pending，更新 Outbox 为 ReconcileRequired (复用 idempotencyKey 重试)
```

---

## 7. 发帖流程 (`createPost`)

发帖操作基于 `RequestOutcome` 划分错误类型：

```text
Repository 生成 localPostId 和 operationId
→ 同一事务中：插入本地 Pending 帖子 (localId=UUID, serverId=null) 与 Outbox 发帖任务
→ 事务完成，即刻向 UI 返回 AppResult.PendingSync (UI 上的 DB Flow 瞬间展示临时帖子)
→ 远程 HTTP 发起发帖请求

判定 RequestOutcome：
├─ Success：
│  在同一事务中：用正式 serverId 填充实体的 serverId 字段 (保留 localId 为主键防外键断裂)，标记 Synced，更新 Outbox 状态为 Succeeded，返回 AppResult.Success(Unit)。
├─ NotSent (如发送前离线)：
│  帖子保留 Pending 状态，同一事务中 Outbox 任务状态变更为 RetryScheduled (进入后台退避重试)。
├─ Unknown (如 Timeout / 连接中断 / 5xx)：
│  同一事务中 Outbox 任务状态变更为 ReconcileRequired，复用原 idempotencyKey 继续发送重试或对齐。
└─ Rejected (如 400 / 403 终态业务错)：
   同一事务中：帖子标记为 Failed 状态，Outbox 任务状态变更为 NeedsUserAction (停止重试，供用户手工修正)。
```

---

## 8. 验收清单

- [ ] 验证 `CreatePostCommand` 不泄露 `idempotencyKey` 与 `operationId`。
- [ ] 验证点赞事务 `prepareSetLiked` 在单一 DB 事务中完成 `serverId` 校验、读取旧字段、写入乐观状态与 Outbox；若 `serverId == null` 则返回 null 终止网络请求。
- [ ] 验证请求判定基于 `RequestOutcome` (`NotSent`, `Rejected`, `Unknown`)，遇到 `Unknown` 不直接回滚，而是进入 `ReconcileRequired`。
- [ ] 验证 DTO 使用 `@SerialName("id")` 与 `@SerialName("postId")` 精准匹配后端 JSON 字段名并保持内部 `serverId` 语义清晰。
- [ ] 验证数据流采用 `DTO → Entity → DB Flow → Domain` 闭环。
- [ ] 验证 `refreshFeed` 下拉刷新时保留 `localId` 本地临时帖子，按 `serverId` 去重，不静默覆盖 Pending 状态。
- [ ] 验证崩溃重启后 `RollbackAndCancel` 点赞任务读取 Outbox `rollbackPayload` 自动恢复回滚，且不再重发。

---

## 附录：Repository 发送点赞事务模型参考

```kotlin
data class PreparedLikeMutation(
    val operationId: String,
    val serverId: String,
    val rollbackPatch: LikeRollbackPatch,
)

class FeedRepositoryImpl(...) : FeedRepository {
    override suspend fun setLiked(localPostId: String, desiredLiked: Boolean): AppResult<Unit> {
        return sequencer.execute(entityKey = "POST_$localPostId") {
            val operationId = generateUuid()
            
            // 1. 单一 DB 事务方法：校验 serverId (若 null 返回 null)、读取旧状态、防重复检测、写入乐观状态、插入 Outbox (含 rollbackPayload)
            val prepared = localDataSource.prepareSetLiked(
                localPostId = localPostId, 
                desiredLiked = desiredLiked, 
                operationId = operationId
            ) ?: return@execute AppResult.Success(Unit)

            // 2. 发起网络请求 (使用事务中解析出的 serverId)
            when (val result = remoteDataSource.setLiked(prepared.serverId, desiredLiked, operationId)) {
                is NetworkResult.Success -> {
                    localDataSource.confirmLikeIfCurrent(localPostId, operationId, result.data)
                    AppResult.Success(Unit)
                }
                is NetworkResult.Failure -> {
                    when (result.error.toRequestOutcome()) {
                        RequestOutcome.Unknown -> {
                            // Timeout 等不确定异常：标记 ReconcileRequired，维持 Pending
                            localDataSource.markReconcileRequiredIfCurrent(localPostId, operationId, backoffMillis(), result.error.code)
                        }
                        RequestOutcome.NotSent, RequestOutcome.Rejected -> {
                            // 确定未发送或被拒绝：同一事务完成字段级回滚并取消 Outbox
                            localDataSource.rollbackLikeIfCurrent(localPostId, operationId, prepared.rollbackPatch)
                        }
                    }
                    AppResult.Failure(result.error.toAppError())
                }
            }
        }
    }
}
```
