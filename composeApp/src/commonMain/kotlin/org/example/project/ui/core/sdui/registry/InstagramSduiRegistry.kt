/**
 * @File: InstagramSduiRegistry.kt
 * @Package: org.example.project.ui.core.sdui.registry
 * @Description: Instagram模块全量SDUI动态组件适配与集中注册入口
 * @Author: 何聚敛
 * @Date: 2026-08-05
 */
package org.example.project.ui.core.sdui.registry

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.example.project.data.repository.instagram.createFakeInstagramPosts
import org.example.project.domain.model.instagram.ContentThumbnailData
import org.example.project.domain.model.instagram.InstagramPost
import org.example.project.domain.model.instagram.PostType
import org.example.project.domain.model.instagram.ProfileUser
import org.example.project.ui.components.instagram.InstagramImagePreview
import org.example.project.ui.components.instagram.InstagramProfileInfoSection
import org.example.project.ui.components.instagram.InstagramTabSwitchLayout
import org.example.project.ui.components.instagram.InstagramTopBar
import org.example.project.ui.components.instagram.InstagramUserCard
import org.example.project.ui.components.instagram.content.InstagramContentThumbnail
import org.example.project.ui.components.instagram.content.InstagramGridContent
import org.example.project.ui.components.instagram.home.InstagramCommentBar
import org.example.project.ui.components.instagram.home.InstagramConfirmDialog
import org.example.project.ui.components.instagram.home.InstagramHomeTopBar
import org.example.project.ui.components.instagram.home.InstagramPostHeader
import org.example.project.ui.components.instagram.home.InstagramPostItem
import org.example.project.ui.components.instagram.home.InstagramPostItemActionBar
import org.example.project.ui.components.instagram.home.InstagramPostItemContentPanel
import org.example.project.ui.components.instagram.home.InstagramPostMediaCarousel
import org.example.project.ui.components.instagram.home.InstagramStoryTray
import org.example.project.ui.core.sdui.SduiComponentRegistry

/**
 * 集中注册Instagram模块下所有16个可热更组件
 */
fun registerInstagramSduiComponents() {
    val defaultUser = ProfileUser(userId = "default", username = "currentUser", avatarUrl = "", signature = "", postCount = "0", followerCount = "0", followingCount = "0")
    val fakePosts = createFakeInstagramPosts()
    val samplePost = fakePosts.firstOrNull() ?: InstagramPost(id = "1", postUser = defaultUser, content = "")

    // 1. 全局顶部导航栏 (InstagramTopBar)
    SduiComponentRegistry.register("InstagramTopBar") { node, onAction ->
        val title = node.properties["title"] ?: "Instagram"
        InstagramTopBar(
            title = title,
            onTitlePress = {
                node.actions["onTitlePress"]?.let { action -> onAction(action) }
            }
        )
    }

    // 2. 首页导航栏 (InstagramHomeTopBar)
    SduiComponentRegistry.register("InstagramHomeTopBar") { node, onAction ->
        InstagramHomeTopBar(
            onCreatePostClick = { node.actions["onCreatePostClick"]?.let { action -> onAction(action) } },
            onNotificationClick = { node.actions["onNotificationClick"]?.let { action -> onAction(action) } }
        )
    }

    // 3. 快拍栏 (InstagramStoryTray)
    SduiComponentRegistry.register("InstagramStoryTray") { node, onAction ->
        InstagramStoryTray(
            stories = emptyList(),
            onStoryClick = { node.actions["onStoryClick"]?.let { action -> onAction(action) } }
        )
    }

    // 4. 动态Feed单项 (InstagramPostItem)
    SduiComponentRegistry.register("InstagramPostItem") { node, onAction ->
        InstagramPostItem(
            post = samplePost,
            currentUser = defaultUser,
            onLikeClick = { node.actions["onLikeClick"]?.let { action -> onAction(action) } },
            onAddCommentClick = { node.actions["onCommentClick"]?.let { action -> onAction(action) } }
        )
    }

    // 5. Post Header (InstagramPostHeader)
    SduiComponentRegistry.register("InstagramPostHeader") { node, onAction ->
        InstagramPostHeader(
            post = samplePost,
            currentUser = defaultUser,
            onAvatarClick = { node.actions["onAvatarClick"]?.let { action -> onAction(action) } },
            onNameClick = { node.actions["onNameClick"]?.let { action -> onAction(action) } },
            onDeletePostClick = { node.actions["onDeletePostClick"]?.let { action -> onAction(action) } }
        )
    }

    // 6. Post Action Bar (InstagramPostItemActionBar)
    SduiComponentRegistry.register("InstagramPostItemActionBar") { node, onAction ->
        InstagramPostItemActionBar(
            isLiked = samplePost.isLiked,
            isSaved = samplePost.isSaved,
            onLikeClick = { node.actions["onLikeClick"]?.let { action -> onAction(action) } },
            onAddCommentClick = { node.actions["onCommentClick"]?.let { action -> onAction(action) } },
            onShareClick = { node.actions["onShareClick"]?.let { action -> onAction(action) } },
            onSaveClick = { node.actions["onBookmarkClick"]?.let { action -> onAction(action) } }
        )
    }

    // 7. Post Content Panel (InstagramPostItemContentPanel)
    SduiComponentRegistry.register("InstagramPostItemContentPanel") { node, onAction ->
        InstagramPostItemContentPanel(
            post = samplePost,
            currentUser = defaultUser,
            onAddCommentClick = { node.actions["onViewAllCommentsClick"]?.let { action -> onAction(action) } }
        )
    }

    // 8. Media Carousel (InstagramPostMediaCarousel)
    SduiComponentRegistry.register("InstagramPostMediaCarousel") { node, onAction ->
        InstagramPostMediaCarousel(
            post = samplePost,
            onMediaClick = { node.actions["onMediaClick"]?.let { action -> onAction(action) } }
        )
    }

    // 9. Profile Info Section (InstagramProfileInfoSection)
    SduiComponentRegistry.register("InstagramProfileInfoSection") { node, onAction ->
        val username = node.properties["username"] ?: defaultUser.username
        val avatarUrl = node.properties["avatarUrl"] ?: defaultUser.avatarUrl
        val posts = node.properties["postCount"] ?: "0"
        val followers = node.properties["followerCount"] ?: "0"
        val following = node.properties["followingCount"] ?: "0"
        val signature = node.properties["signature"] ?: ""
        InstagramProfileInfoSection(
            avatarUrl = avatarUrl,
            username = username,
            postCount = posts,
            followerCount = followers,
            followingCount = following,
            signature = signature,
            onAvatarClick = { node.actions["onAvatarClick"]?.let { action -> onAction(action) } },
            onSignatureClick = { node.actions["onSignatureClick"]?.let { action -> onAction(action) } },
            onPostClick = { node.actions["onPostClick"]?.let { action -> onAction(action) } },
            onFollowerClick = { node.actions["onFollowerClick"]?.let { action -> onAction(action) } },
            onFollowingClick = { node.actions["onFollowingClick"]?.let { action -> onAction(action) } }
        )
    }

    // 10. User Card (InstagramUserCard)
    SduiComponentRegistry.register("InstagramUserCard") { node, onAction ->
        val username = node.properties["username"] ?: ""
        val avatarUrl = node.properties["avatarUrl"] ?: ""
        val extraInfo = node.properties["extraInfo"]
        InstagramUserCard(
            avatarUrl = avatarUrl,
            username = username,
            extraInfo = extraInfo,
            onDismiss = { node.actions["onDismiss"]?.let { action -> onAction(action) } },
            onClick = { node.actions["onClick"]?.let { action -> onAction(action) } }
        )
    }

    // 11. Tab Switch Layout (InstagramTabSwitchLayout)
    SduiComponentRegistry.register("InstagramTabSwitchLayout") { node, onAction ->
        InstagramTabSwitchLayout(
            tabs = emptyList(),
            selectedTabId = "grid",
            onTabSelected = { node.actions["onTabSelected"]?.let { action -> onAction(action) } },
            content = { Box {} }
        )
    }

    // 12. Image Preview (InstagramImagePreview)
    SduiComponentRegistry.register("InstagramImagePreview") { node, _ ->
        val imageUrl = node.properties["imageUrl"] ?: ""
        InstagramImagePreview(
            model = imageUrl
        )
    }

    // 13. Content Thumbnail (InstagramContentThumbnail)
    SduiComponentRegistry.register("InstagramContentThumbnail") { node, onAction ->
        val sampleData = ContentThumbnailData(id = "1", imageUrl = "", type = PostType.SINGLE)
        InstagramContentThumbnail(
            data = sampleData,
            onClick = { node.actions["onClick"]?.let { action -> onAction(action) } }
        )
    }

    // 14. Grid Content (InstagramGridContent)
    SduiComponentRegistry.register("InstagramGridContent") { node, onAction ->
        InstagramGridContent(
            onItemClick = { node.actions["onItemClick"]?.let { action -> onAction(action) } }
        )
    }

    // 15. Comment Bar (InstagramCommentBar)
    SduiComponentRegistry.register("InstagramCommentBar") { node, onAction ->
        val textValue = node.properties["value"] ?: ""
        val avatarUrl = node.properties["avatarUrl"] ?: ""
        InstagramCommentBar(
            avatarUrl = avatarUrl,
            value = textValue,
            onValueChange = {},
            onSendClick = { node.actions["onSendClick"]?.let { action -> onAction(action) } }
        )
    }

    // 16. Confirm Dialog (InstagramConfirmDialog)
    SduiComponentRegistry.register("InstagramConfirmDialog") { node, onAction ->
        val title = node.properties["title"] ?: "提示"
        val message = node.properties["message"] ?: "确定要进行此操作吗？"
        InstagramConfirmDialog(
            title = title,
            text = message,
            onConfirmClick = { node.actions["onConfirm"]?.let { action -> onAction(action) } },
            onDismissClick = { node.actions["onDismiss"]?.let { action -> onAction(action) } },
            onDismissRequest = { node.actions["onDismiss"]?.let { action -> onAction(action) } }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun InstagramSduiRegistryPreview() {
    registerInstagramSduiComponents()
}
