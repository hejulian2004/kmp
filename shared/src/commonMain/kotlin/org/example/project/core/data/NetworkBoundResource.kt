/**
 * @File: NetworkBoundResource.kt
 * @Package: org.example.project.core.data
 * @Description: 项目全局通用 Stale-While-Revalidate (SWR) 本地优先数据同步管道
 * @Author: 何聚敛
 * @Date: 2026-08-04
 */
package org.example.project.core.data

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.example.project.domain.error.AppError
import org.example.project.domain.error.toAppError

/**
 * 项目全局通用 Stale-While-Revalidate (SWR) 本地优先数据同步 Flow 管道
 *
 * @param Key 查询参数/标识符类型
 * @param Entity 本地领域实体/数据库数据类型
 * @param NetworkDto 网络请求返回的 DTO 数据类型
 *
 * @param key 标识参数
 * @param queryLocal 从本地内存/数据库获取 Flow 的 Lambda
 * @param fetchRemote 从网络 API 发起请求拉取 NetworkDto 的 Lambda
 * @param saveRemoteResult 将网络 DTO 转换为 Entity 并持久化保存到本地 DB/内存的 Lambda
 * @param shouldFetch 校验是否发起网络请求的判断 Lambda（默认总是发起）
 */
inline fun <Key, Entity, NetworkDto> networkBoundResource(
    key: Key,
    crossinline queryLocal: (Key) -> Flow<Entity?>,
    crossinline fetchRemote: suspend (Key) -> NetworkDto,
    crossinline saveRemoteResult: suspend (Key, NetworkDto) -> Unit,
    crossinline shouldFetch: (Entity?) -> Boolean = { true }
): Flow<ResourceState<Entity>> = flow {
    // 1. 读取当前本地数据
    val localData = queryLocal(key).firstOrNull()

    // 2. 发射 Loading 状态（携带本地旧缓存供 UI 0ms 瞬间渲染）
    emit(ResourceState.Loading(localData))

    // 3. 按条件判断是否发起网络同步
    if (shouldFetch(localData)) {
        try {
            val remoteDto = fetchRemote(key)
            saveRemoteResult(key, remoteDto)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            // 捕获网络/解析异常，发射 Error 并附带本地旧数据供 UI 降级
            emit(ResourceState.Error(e.toAppError(), localData))
        }
    }

    // 4. 绑定本地 DB 观察流，保障 SSOT 权威数据变动响应
    emitAll(queryLocal(key).map { entity ->
        if (entity != null) {
            ResourceState.Success(entity)
        } else {
            ResourceState.Error(AppError.NotFound, null)
        }
    })
}
