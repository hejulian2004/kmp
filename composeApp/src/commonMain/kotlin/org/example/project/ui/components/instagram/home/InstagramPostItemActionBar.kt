/**
 * @File: InstagramPostItemActionBar.kt
 * @Package: org.example.project.ui.components.instagram.home
 * @Description: Instagram Post动态帖子快捷操作按钮工具栏组件（对齐最新Instagram UI，依次包含点赞、评论、转发、分享及收藏按钮）
 * @Author: 何聚敛
 * @Date: 2026-07-29
 */
package org.example.project.ui.components.instagram.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.example.project.ui.theme.instagram.InstagramBlue
import org.example.project.ui.theme.instagram.InstagramRed
import org.example.project.ui.theme.instagram.InstagramTheme

/**
 * Post快捷交互操作按钮工具栏组件
 *
 * 对应最新原生Instagram UI按键排布：
 * - 左侧：点赞(Heart)、评论(ChatBubble)、转发(Repost/Refresh)、分享(Send)
 * - 右侧：收藏(Bookmark)
 *
 * @param isLiked 是否已点赞
 * @param isSaved 是否已收藏
 * @param isReposted 是否已转发
 * @param onLikeClick 点赞按钮点击回调
 * @param onAddCommentClick 点击评论图标回调
 * @param onRepostClick 点击转发按钮回调
 * @param onShareClick 点击分享按钮回调
 * @param onSaveClick 点击收藏按钮回调
 * @param modifier 外部修饰符
 */
@Composable
fun InstagramPostItemActionBar(
    isLiked: Boolean,
    isSaved: Boolean,
    isReposted: Boolean = false,
    onLikeClick: () -> Unit = {},
    onAddCommentClick: () -> Unit = {},
    onRepostClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    onSaveClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. 带弹跳动画的点赞红心图标
        val likeScale by animateFloatAsState(
            targetValue = if (isLiked) 1.2f else 1.0f,
            animationSpec = spring(dampingRatio = 0.4f, stiffness = 500f)
        )
        IconButton(onClick = onLikeClick) {
            Icon(
                imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = "Like",
                tint = if (isLiked) InstagramRed else MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .size(24.dp)
                    .scale(likeScale)
            )
        }

        // 2. 评论气泡图标
        IconButton(onClick = onAddCommentClick) {
            Icon(
                imageVector = Icons.Outlined.ChatBubbleOutline,
                contentDescription = "Comment",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(23.dp)
            )
        }

        // 3. 转发图标（对齐最新Instagram双箭头转发/Repost/Retweet图标）
        IconButton(onClick = onRepostClick) {
            Icon(
                imageVector = if (isReposted) Icons.Filled.Repeat else Icons.Outlined.Repeat,
                contentDescription = "Repost",
                tint = if (isReposted) InstagramBlue else MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(23.dp)
            )
        }

        // 4. 分享图标
        IconButton(onClick = onShareClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.Send,
                contentDescription = "Share",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // 5. 收藏书签图标
        IconButton(onClick = onSaveClick) {
            Icon(
                imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                contentDescription = "Save",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * Instagram Post操作按钮栏组件Composable预览函数
 */
@Preview
@Composable
fun InstagramPostItemActionBarPreview() {
    InstagramTheme {
        InstagramPostItemActionBar(
            isLiked = true,
            isSaved = false,
            isReposted = false
        )
    }
}
