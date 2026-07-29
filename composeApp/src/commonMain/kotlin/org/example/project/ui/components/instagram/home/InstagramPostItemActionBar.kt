/**
 * @File: InstagramPostItemActionBar.kt
 * @Package: org.example.project.ui.components.instagram.home
 * @Description: Instagram Post动态帖子快捷操作按钮工具栏组件（对齐最新Instagram原生UI，图标旁直接紧跟点赞数、评论数、转发数、分享数）
 * @Author: 何聚敛
 * @Date: 2026-07-29
 */
package org.example.project.ui.components.instagram.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.ui.theme.instagram.InstagramBlue
import org.example.project.ui.theme.instagram.InstagramRed
import org.example.project.ui.theme.instagram.InstagramTheme
import kotlinproject.composeapp.generated.resources.Res
import kotlinproject.composeapp.generated.resources.ins_unit_wan
import kotlinproject.composeapp.generated.resources.ins_unit_wan_decimal
import org.jetbrains.compose.resources.stringResource

/**
 * Post快捷交互操作按钮与数字展示工具栏组件
 *
 * 对应最新原生Instagram UI排布：
 * - 点赞图标 + 20.1万 | 评论图标 + 1,249 | 转发图标 + 8,702 | 分享图标 + 2.1万 | 右侧收藏图标
 *
 * @param isLiked 是否已点赞
 * @param isSaved 是否已收藏
 * @param isReposted 是否已转发
 * @param likesCount 点赞次数
 * @param commentsCount 评论次数
 * @param repostCount 转发次数
 * @param shareCount 分享次数
 * @param isLikeCountHidden 是否隐藏点赞数
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
    likesCount: Long = 0,
    commentsCount: Long = 0,
    repostCount: Long = 0,
    shareCount: Long = 0,
    isLikeCountHidden: Boolean = false,
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
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. 点赞 (Heart Icon + Count)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onLikeClick
                )
                .padding(vertical = 4.dp, horizontal = 2.dp)
        ) {
            val likeScale by animateFloatAsState(
                targetValue = if (isLiked) 1.25f else 1.0f,
                animationSpec = spring(dampingRatio = 0.4f, stiffness = 500f)
            )
            Icon(
                imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = "Like",
                tint = if (isLiked) InstagramRed else MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .size(24.dp)
                    .scale(likeScale)
            )
            val likeText = if (!isLikeCountHidden) formatActionCount(likesCount) else ""
            if (likeText.isNotEmpty()) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = likeText,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        // 2. 评论 (ChatBubble Icon + Count)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onAddCommentClick
                )
                .padding(vertical = 4.dp, horizontal = 2.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.ChatBubbleOutline,
                contentDescription = "Comment",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(23.dp)
            )
            val commentText = formatActionCount(commentsCount)
            if (commentText.isNotEmpty()) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = commentText,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        // 3. 转发 (Repost Icon + Count)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onRepostClick
                )
                .padding(vertical = 4.dp, horizontal = 2.dp)
        ) {
            Icon(
                imageVector = if (isReposted) Icons.Filled.Repeat else Icons.Outlined.Repeat,
                contentDescription = "Repost",
                tint = if (isReposted) InstagramBlue else MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(23.dp)
            )
            val repostText = formatActionCount(repostCount)
            if (repostText.isNotEmpty()) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = repostText,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        // 4. 分享 (PaperPlane/Send Icon + Count)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onShareClick
                )
                .padding(vertical = 4.dp, horizontal = 2.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.Send,
                contentDescription = "Share",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(22.dp)
            )
            val shareText = formatActionCount(shareCount)
            if (shareText.isNotEmpty()) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = shareText,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // 5. 收藏 (Bookmark Icon)
        IconButton(
            onClick = onSaveClick,
            modifier = Modifier.size(32.dp)
        ) {
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
 * 格式化操作栏数字（如 20100 -> 20.1万, 1249 -> 1,249, 8702 -> 8,702）
 */
@Composable
private fun formatActionCount(count: Long): String {
    if (count <= 0) return ""
    return if (count >= 10_000) {
        val main = count / 10_000
        val remainder = (count % 10_000) / 1_000
        if (remainder > 0) {
            stringResource(Res.string.ins_unit_wan_decimal, main, remainder)
        } else {
            stringResource(Res.string.ins_unit_wan, main)
        }
    } else {
        val str = count.toString()
        val sb = StringBuilder()
        for (i in str.indices) {
            if (i > 0 && (str.length - i) % 3 == 0) {
                sb.append(',')
            }
            sb.append(str[i])
        }
        sb.toString()
    }
}

@Preview(showBackground = true)
@Composable
fun InstagramPostItemActionBarPreview() {
    InstagramTheme {
        InstagramPostItemActionBar(
            isLiked = true,
            isSaved = false,
            isReposted = false,
            likesCount = 20100,
            commentsCount = 1249,
            repostCount = 8702,
            shareCount = 21000
        )
    }
}
