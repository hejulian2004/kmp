# FeedLine (朋友圈) 数据模型说明

本文档描述了朋友圈模块的核心领域模型设计。该模型作为本项目社交功能的基准参考。

## 核心模型

### 1. FeedLinePost (动态帖子)
| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `id` | `String` | 动态唯一标识 |
| `postUser` | `FeedLineUser` | 发布者 |
| `content` | `String` | 动态文字内容 |
| `mediaList` | `List<FeedLineMedia>` | 媒体资源列表 |
| `commentsList` | `List<FeedLineComment>` | 评论列表 |
| `likedUsers` | `List<FeedLineUser>` | 点赞列表 |
| `isLiked` | `Boolean` | 是否点赞过 |
| `createTime` | `Long` | 发布时间 |
| `unreadNotificationCount` | `Int` | 未读提醒数 |

### 2. FeedLineMedia (媒体内容)
- **Image**: 图片 URL 及宽高。
- **Video**: 视频 URL、封面、时长及宽高。

### 3. FeedLineComment (评论)
| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `id` | `String` | 评论唯一标识 |
| `postId` | `String` | 关联动态 ID |
| `commentUser` | `FeedLineUser` | 评论人 |
| `content` | `String` | 评论文字内容 |
| `createTime` | `Long` | 评论时间 |

### 4. FeedLineUser (用户信息)
| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `id` | `String` | 用户唯一 ID |
| `name` | `String` | 名字 |
| `avatarUrl` | `String` | 头像地址 |

## 设计特点
- **结构简洁**: 专注于通用的图文/视频流社交。
- **一致性**: 为 `Instagram` 等其他社交模块提供基础字段规范。
