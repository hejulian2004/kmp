/**
 * @File: FeedLineSduiRegistry.kt
 * @Package: org.example.project.ui.core.sdui.registry
 * @Description: FeedLine 朋友圈模块全量 SDUI 动态组件适配与集中注册入口
 * @Author: 何聚敛
 * @Date: 2026-08-05
 */
package org.example.project.ui.core.sdui.registry

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.example.project.core.network.config.createFakeData
import org.example.project.domain.model.feedline.FeedLineUser
import org.example.project.ui.components.feedline.Avatar
import org.example.project.ui.components.feedline.BottomSheet
import org.example.project.ui.components.feedline.FeedActionBar
import org.example.project.ui.components.feedline.FeedCommentBar
import org.example.project.ui.components.feedline.FeedCommentItem
import org.example.project.ui.components.feedline.FeedCommentList
import org.example.project.ui.components.feedline.FeedLikedUserNameBar
import org.example.project.ui.components.feedline.FeedNotificationBar
import org.example.project.ui.components.feedline.FeedPostItem
import org.example.project.ui.components.feedline.FeedTopBar
import org.example.project.ui.components.feedline.FeedLineConfirmDialog
import org.example.project.ui.components.feedline.PublishScreen
import org.example.project.ui.core.sdui.SduiComponentRegistry

/**
 * 集中注册 FeedLine 朋友圈模块下所有可热更组件
 */
fun registerFeedLineSduiComponents() {
    val defaultUser = FeedLineUser(id = "default", name = "当前用户", avatarUrl = "")
    val fakePosts = createFakeData()
    val samplePost = fakePosts.firstOrNull() ?: org.example.project.core.network.config.createFakePost()

    // 1. 顶部导航栏 (FeedLineTopBar)
    SduiComponentRegistry.register("FeedLineTopBar") { node, onAction ->
        FeedTopBar(
            onShortClickCreatePost = {
                node.actions["onShortClick"]?.let { action -> onAction(action) }
            },
            onLongClickCreatePost = {
                node.actions["onLongClick"]?.let { action -> onAction(action) }
            }
        )
    }

    // 2. 未读通知栏 (FeedLineNotificationBar)
    SduiComponentRegistry.register("FeedLineNotificationBar") { node, onAction ->
        val count = node.properties["count"]?.toIntOrNull() ?: 0
        val avatarUrl = node.properties["avatarUrl"]
        if (count > 0) {
            FeedNotificationBar(
                unreadCount = count,
                latestNotificationUserAvatar = avatarUrl?.ifBlank { null },
                onClick = {
                    node.actions["onClick"]?.let { action -> onAction(action) }
                }
            )
        }
    }

    // 3. 动态核心卡片 (FeedLinePostItem)
    SduiComponentRegistry.register("FeedLinePostItem") { node, onAction ->
        FeedPostItem(
            post = samplePost,
            currentUser = defaultUser,
            onClick = { node.actions["onClick"]?.let { action -> onAction(action) } },
            onNameClick = { node.actions["onNameClick"]?.let { action -> onAction(action) } },
            onLikeClick = { node.actions["onLikeClick"]?.let { action -> onAction(action) } },
            onAddCommentClick = { node.actions["onAddCommentClick"]?.let { action -> onAction(action) } },
            onCommentClick = { node.actions["onCommentClick"]?.let { action -> onAction(action) } },
            onCommentUserClick = { node.actions["onCommentUserClick"]?.let { action -> onAction(action) } },
            onDeleteCommentClick = { node.actions["onDeleteCommentClick"]?.let { action -> onAction(action) } },
            onDeletePostClick = { node.actions["onDeletePostClick"]?.let { action -> onAction(action) } },
            onPostAvatarClick = { node.actions["onPostAvatarClick"]?.let { action -> onAction(action) } },
            onLikedAvatarClick = { node.actions["onLikedAvatarClick"]?.let { action -> onAction(action) } },
            currentTime = 0L
        )
    }

    // 4. 操作栏 (FeedLineActionBar)
    SduiComponentRegistry.register("FeedLineActionBar") { node, onAction ->
        FeedActionBar(
            post = samplePost,
            currentUser = defaultUser,
            onLikeClick = { node.actions["onLikeClick"]?.let { action -> onAction(action) } },
            onAddCommentClick = { node.actions["onAddCommentClick"]?.let { action -> onAction(action) } },
            onDeletePostClick = { node.actions["onDeletePostClick"]?.let { action -> onAction(action) } },
            currentTime = 0L
        )
    }

    // 5. 头像组件 (FeedLineAvatar)
    SduiComponentRegistry.register("FeedLineAvatar") { node, onAction ->
        val url = node.properties["url"] ?: ""
        val sizeDp = node.properties["size"]?.toIntOrNull() ?: 40
        Avatar(
            url = url,
            size = sizeDp.dp,
            onClick = { node.actions["onClick"]?.let { action -> onAction(action) } }
        )
    }

    // 6. 点赞用户姓名栏 (FeedLineLikedUserNameBar)
    SduiComponentRegistry.register("FeedLineLikedUserNameBar") { node, onAction ->
        FeedLikedUserNameBar(
            likedUserList = samplePost.likedUsers,
            onUserClick = { node.actions["onUserClick"]?.let { action -> onAction(action) } }
        )
    }

    // 7. 评论输入栏 (FeedLineCommentBar)
    SduiComponentRegistry.register("FeedLineCommentBar") { node, onAction ->
        val textValue = node.properties["value"] ?: ""
        FeedCommentBar(
            value = textValue,
            onValueChange = {},
            onSendClick = { node.actions["onSendClick"]?.let { action -> onAction(action) } }
        )
    }

    // 8. 单条评论项 (FeedLineCommentItem)
    SduiComponentRegistry.register("FeedLineCommentItem") { node, onAction ->
        val firstComment = samplePost.commentsList.firstOrNull()
        if (firstComment != null) {
            FeedCommentItem(
                currentUser = defaultUser,
                comment = firstComment,
                onCommentClick = { node.actions["onCommentClick"]?.let { action -> onAction(action) } },
                onCommentLongClick = { node.actions["onCommentLongClick"]?.let { action -> onAction(action) } },
                onCommentUserNameClick = { node.actions["onCommentUserClick"]?.let { action -> onAction(action) } }
            )
        }
    }

    // 9. 评论列表 (FeedLineCommentList)
    SduiComponentRegistry.register("FeedLineCommentList") { node, onAction ->
        FeedCommentList(
            currentUser = defaultUser,
            commentsList = samplePost.commentsList,
            onCommentClick = { node.actions["onCommentClick"]?.let { action -> onAction(action) } },
            onDeleteCommentClick = { node.actions["onDeleteCommentClick"]?.let { action -> onAction(action) } },
            onCommentUserClick = { node.actions["onCommentUserClick"]?.let { action -> onAction(action) } }
        )
    }

    // 10. 底部弹出遮罩层 (FeedLineBottomSheet)
    SduiComponentRegistry.register("FeedLineBottomSheet") { node, onAction ->
        BottomSheet(
            onTakePhotoClick = { node.actions["onTakePhotoClick"]?.let { action -> onAction(action) } },
            onRecordVideoClick = { node.actions["onRecordVideoClick"]?.let { action -> onAction(action) } },
            onChooseClick = { node.actions["onChooseClick"]?.let { action -> onAction(action) } },
            onCancelClick = { node.actions["onCancelClick"]?.let { action -> onAction(action) } }
        )
    }

    // 11. 确认二次弹窗 (FeedLineConfirmDialog)
    SduiComponentRegistry.register("FeedLineConfirmDialog") { node, onAction ->
        val title = node.properties["title"] ?: "提示"
        val content = node.properties["content"] ?: "确定要执行操作吗？"
        FeedLineConfirmDialog(
            title = title,
            text = content,
            onConfirmClick = { node.actions["onConfirm"]?.let { action -> onAction(action) } },
            onDismissClick = { node.actions["onDismiss"]?.let { action -> onAction(action) } },
            onDismissRequest = { node.actions["onDismiss"]?.let { action -> onAction(action) } }
        )
    }

    // 12. 发布动态 Screen 层组件 (FeedLinePublishScreen)
    SduiComponentRegistry.register("FeedLinePublishScreen") { node, onAction ->
        PublishScreen(
            initialMediaList = emptyList(),
            onCancelClick = { node.actions["onCancelClick"]?.let { action -> onAction(action) } },
            onPostClick = { _, _ -> node.actions["onPostClick"]?.let { action -> onAction(action) } }
        )
    }

    // 13. 视频缩略图辅助助手 (FeedLineVideoThumbnailHelper)
    SduiComponentRegistry.register("FeedLineVideoThumbnailHelper") { _, _ -> }
}

@Preview(showBackground = true)
@Composable
fun FeedLineSduiRegistryPreview() {
    registerFeedLineSduiComponents()
}
