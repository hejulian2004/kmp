/**
 * @File: InstagramPostItemContentPanel.kt
 * @Package: org.example.project.ui.components.instagram.home
 * @Description: Instagram Post动态帖子信息内容面板组件（对齐最新Instagram UI，在ActionBar正下方直接粗体展示点赞次数、作者正文、评论及发布时间）
 * @Author: 何聚敛
 * @Date: 2026-07-29
 */
package org.example.project.ui.components.instagram.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.data.repository.instagram.createFakeInstagramPosts
import org.example.project.domain.model.instagram.InstagramComment
import org.example.project.domain.model.instagram.InstagramPost
import org.example.project.domain.model.instagram.ProfileUser
import org.example.project.platform.currentTimeMillis
import org.example.project.ui.theme.instagram.InstagramTheme
import kotlinproject.composeapp.generated.resources.Res
import kotlinproject.composeapp.generated.resources.ins_more
import org.jetbrains.compose.resources.stringResource

/**
 * Post信息内容面板组件
 *
 * 对应最新原生Instagram排布：
 * 1. Post作者用户名 + 正文文案与“展开”区域
 * 2. 评论区预览与热门评论（含删除入口）
 * 3. 相对发布时间戳
 *
 * @param post帖子聚合实体
 * @param currentUser当前登录用户
 * @param onAddCommentClick点击评论入口/查看全部评论回调
 * @param onCommentClick点击单条评论回调
 * @param onCommentUserClick点击评论作者回调
 * @param onDeleteCommentClick点击删除评论回调
 * @param modifier外部修饰符
 */
@Composable
fun InstagramPostItemContentPanel(
    post: InstagramPost,
    currentUser: ProfileUser,
    onAddCommentClick: () -> Unit = {},
    onCommentClick: (InstagramComment) -> Unit = {},
    onCommentUserClick: (ProfileUser) -> Unit = {},
    onDeleteCommentClick: (InstagramComment) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // 1. Post正文文案与“更多”展开区域
        if (post.content.isNotBlank()) {
            var isCaptionExpanded by remember { mutableStateOf(false) }
            val isLongContent = post.content.length > 35
            val moreText = stringResource(Res.string.ins_more)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 2.dp)
            ) {
                val annotatedCaption = buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(post.postUser.username + " ")
                    }
                    if (!isCaptionExpanded && isLongContent) {
                        append(post.content.take(30) + "... ")
                        withStyle(SpanStyle(color = Color.Gray)) {
                            append(moreText)
                        }
                    } else {
                        append(post.content)
                    }
                }

                Text(
                    text = annotatedCaption,
                    fontSize = 13.sp,
                    lineHeight = 17.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = if (isCaptionExpanded) Int.MAX_VALUE else 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.clickable(enabled = !isCaptionExpanded && isLongContent) {
                        isCaptionExpanded = true
                    }
                )
            }
        }



        // 4. 相对发布时间戳
        Text(
            text = formatTimeAgo(post.createTime),
            fontSize = 10.sp,
            color = Color.Gray,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
        )
    }
}



/**
 * 计算相对时间的辅助工具函数
 *
 * @param createTime发布时间毫秒值
 * @return格式化的相对时间字符串(如2s ago, 5m ago, 2h ago, 1d ago)
 */
private fun formatTimeAgo(createTime: Long): String {
    val diffSec = ((currentTimeMillis() - createTime) / 1000).coerceAtLeast(1)
    return when {
        diffSec < 60 -> "${diffSec}s ago"
        diffSec < 3600 -> "${diffSec / 60}m ago"
        diffSec < 86400 -> "${diffSec / 3600}h ago"
        else -> "${diffSec / 86400}d ago"
    }
}

@Preview(showBackground = true)
@Composable
fun InstagramPostItemContentPanelPreview() {
    InstagramTheme {
        val fakePosts = createFakeInstagramPosts()
        if (fakePosts.isNotEmpty()) {
            InstagramPostItemContentPanel(
                post = fakePosts.first(),
                currentUser = ProfileUser("u_me", "hejulian", "", "", "", "", "")
            )
        }
    }
}
