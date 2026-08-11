# Instagram 数据模型说明

本文档描述了 Instagram 模块的核心领域模型设计。该模型在结构上对标了 `FeedLine`（朋友圈），并针对 Instagram 的业务特性进行了扩展。

## 核心模型

### 1. InstagramPost (帖子/动态聚合模型)
这是 Instagram 信息流的核心模型，全量用于 **Feed 动态帖子** 与 **顶部 Story 快拍栏**。包含了帖子、作者、媒体、评论及社交统计。

| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `id` | `String` | 帖子唯一标识 |
| `postUser` | `ProfileUser` | 帖子作者（包含头像、用户名等） |
| `content` | `String` | 帖子配文 (Caption) |
| `mediaList` | `List<InstagramMedia>` | 媒体列表（支持单图、多图轮播、视频） |
| `commentsList` | `List<InstagramComment>` | 评论列表 |
| `likedUsers` | `List<ProfileUser>` | 点赞用户列表 |
| `isLiked` | `Boolean` | 当前用户是否已点赞 |
| `createTime` | `Long` | 发布时间戳 |
| `unreadNotificationCount` | `Int` | 未读通知数（在 Story 中用于标识未读圈/已读圈） |
| **`location`** | `String?` | **[Ins特有]** 地理位置名称 |
| **`taggedUsers`** | `List<ProfileUser>` | **[Ins特有]** 标记的用户列表 (Tag People) |
| **`hashtags`** | `List<String>` | **[Ins特有]** 话题标签列表 (#hashtags) |
| **`collaborators`** | `List<ProfileUser>` | **[Ins特有]** 联合发布者/创作者 (Invite Collaborator) |
| **`isSaved`** | `Boolean` | **[Ins特有]** 当前用户是否已收藏 |
| **`savedCount`** | `Long?` | **[Ins特有]** 帖子被收藏总次数 |
| **`shareCount`** | `Long?` | **[Ins特有]** 帖子被转发/分享总次数 (Shares/Reshares) |
| **`viewCount`** | `Long?` | **[Ins特有]** 视频播放/展现次数 |
| **`isCommentsDisabled`** | `Boolean` | **[Ins特有]** 是否关闭评论功能 (Turn off commenting) |
| **`isLikeCountHidden`** | `Boolean` | **[Ins特有]** 是否隐藏点赞数与播放数 (Hide like and view counts) |
| **`isPinned`** | `Boolean` | **[Ins特有]** 是否置顶在个人主页网格 (Pin to profile) |
| **`audioTitle`** | `String?` | **[Ins特有]** 关联的背景音乐/原声名称 (Audio / Music) |

### 2. InstagramMedia (媒体内容)
采用密封接口设计，区分图片和视频。

- **Image**:
    - `url`: 图片地址
    - `width/height`: 宽高可选
- **Video**:
    - `videoUrl`: 视频地址
    - `coverUrl`: 封面图地址
    - `durationSecond`: 时长（秒）
    - `width/height`: 宽高可选

### 3. InstagramComment (评论)
| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `id` | `String` | 评论唯一标识 |
| `postId` | `String` | 所属帖子 ID |
| `commentUser` | `ProfileUser` | 评论者 |
| `content` | `String` | 评论内容 |
| `createTime` | `Long` | 评论时间戳 |

## 数据流与 MVI 架构

`InstagramHomeScreen` 采用与 `FeedLineScreen` 100% 一致的 MVI 模式：
- **`InstagramHomeRepository`**: 接口层定义 `getHomePosts()` 与 `getStories()`，统一返回 `Flow<List<InstagramPost>>`。
- **`InstagramHomeRepositoryImpl`**: 纯内存 `StateFlow` 缓存存储与假数据产生器（不涉及 Room 等磁盘本地存储）。
- **`InstagramHomeViewModel`**: 持有 `InstagramHomeUiState`，响应 `InstagramHomeIntent` 并通过 `Channel` 分发 `InstagramHomeEffect`。

## 设计原则
- **UI 驱动**: 模型设计为“聚合”结构，ViewModel 获取后可直接用于 UI 渲染，无需二次查询作者或媒体详情。
- **单一模型源**: 统一使用 `InstagramPost` 作为核心实体，避免冗余实体引入。
- **对标通用**: 基础字段名与 `FeedLine` 保持 100% 一致，便于未来沉淀通用的社交组件。
