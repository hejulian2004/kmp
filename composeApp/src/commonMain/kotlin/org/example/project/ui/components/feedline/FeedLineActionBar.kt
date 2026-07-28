/**
 * @File: FeedLineActionBar.kt
 * @Package: org.example.project.ui.components.feedline
 * @Description: 帖子底部的点赞与评论操作栏组件
 * @Author: 何聚敛
 * @Date: 2026-07-20
 */
package org.example.project.ui.components.feedline

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CommentBank
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.domain.model.feedline.FeedLinePost
import org.example.project.domain.model.feedline.FeedLineUser
import org.example.project.platform.currentTimeMillis
import org.example.project.utils.TimeUtils
import org.example.project.data.repository.feedline.generateUUID
import org.example.project.ui.theme.feedline.FeedLineActionMenuDarkGray
import org.example.project.ui.theme.feedline.FeedLineDangerRed
import org.example.project.ui.theme.feedline.FeedLineSurfaceWhite
import org.example.project.ui.theme.feedline.FeedLineTextMuted
import org.example.project.ui.theme.feedline.FeedLineTextSecondary
import org.jetbrains.compose.resources.stringResource
import kotlinproject.composeapp.generated.resources.Res
import kotlinproject.composeapp.generated.resources.feedline_comment
import kotlinproject.composeapp.generated.resources.feedline_delete
import kotlinproject.composeapp.generated.resources.feedline_like
import kotlinproject.composeapp.generated.resources.feedline_unlike

@Composable
fun FeedActionBar(
    modifier: Modifier = Modifier,
    post: FeedLinePost,
    currentUser: FeedLineUser,
    onLikeClick: () -> Unit,
    onAddCommentClick: () -> Unit,
    onDeletePostClick: (FeedLinePost) -> Unit,
    currentTime: Long
) {

    var isShowMore by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .height(35.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = showTime(post.createTime, currentTime),
            fontSize = 12.sp,
            color = FeedLineTextSecondary,
            modifier = Modifier.align(Alignment.CenterStart).padding(end = 8.dp)
        )

        AnimatedVisibility(
            visible = isShowMore,
            modifier = Modifier.align(Alignment.CenterEnd)
                .padding(end = 25.dp),
            enter = expandHorizontally(
                expandFrom = Alignment.End
            ) + fadeIn(),
            exit = shrinkHorizontally(
                shrinkTowards = Alignment.End
            ) + fadeOut()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(
                        color = FeedLineActionMenuDarkGray,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 15.dp, vertical = 8.dp)
            ){
                if (post.postUser.id == currentUser.id) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            onDeletePostClick(post)
                            isShowMore = false
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(Res.string.feedline_delete),
                            modifier = Modifier.size(16.dp),
                            tint = FeedLineSurfaceWhite
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(Res.string.feedline_delete),
                            fontSize = 14.sp,
                            color = FeedLineSurfaceWhite
                        )
                        Spacer(modifier = Modifier.width(15.dp))
                        VerticalDivider(
                            thickness = 0.5.dp,
                            color = FeedLineTextMuted
                        )
                        Spacer(modifier = Modifier.width(15.dp))
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Icon(
                        imageVector =  Icons.Default.Favorite,
                        contentDescription = if(post.isLiked) stringResource(Res.string.feedline_unlike) else stringResource(Res.string.feedline_like),
                        modifier = Modifier
                            .size(16.dp),
                        tint = if(post.isLiked) FeedLineDangerRed else FeedLineSurfaceWhite
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        modifier = Modifier
                            .clickable {
                                onLikeClick()
                                isShowMore = false
                            },
                        text = if(post.isLiked) stringResource(Res.string.feedline_unlike) else stringResource(Res.string.feedline_like),
                        fontSize = 14.sp,
                        color = FeedLineSurfaceWhite
                    )
                    Spacer(modifier = Modifier.width(15.dp))
                    VerticalDivider(
                        thickness = 0.5.dp,
                        color = FeedLineTextMuted
                    )
                    Spacer(modifier = Modifier.width(15.dp))
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Icon(
                        imageVector = Icons.Default.CommentBank,
                        contentDescription = stringResource(Res.string.feedline_comment),
                        modifier = Modifier.size(16.dp),
                        tint = FeedLineSurfaceWhite
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        modifier = Modifier
                            .clickable {
                                onAddCommentClick()
                                isShowMore = false
                            },
                        text = stringResource(Res.string.feedline_comment),
                        fontSize = 14.sp,
                        color = FeedLineSurfaceWhite
                    )
                }
            }
        }
        Icon(
            imageVector = Icons.Default.MoreHoriz,
            contentDescription = "更多",
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .clickable {
                    isShowMore = !isShowMore
                }
                .size(24.dp),
            tint = FeedLineTextSecondary
        )
    }
}

fun showTime(postTime: Long, currentTime: Long): String {
    val timeGap = currentTime - postTime
    return if (timeGap < 60_000L) { //一分钟以内
        "刚刚"
    } else if (timeGap < 3_600_000L) { //一小时以内
        "${timeGap / 60_000L}分钟前"
    } else if (timeGap < 86_400_000L) { //一天以内
        "${timeGap / 3_600_000L}小时前"
    } else if (timeGap < 604_800_000L) { //一周以内
        "${timeGap / 86_400_000L}天前"
    } else {
        TimeUtils.formatTime(postTime, "yyyy-MM-dd")
    }
}

@Preview(showBackground = true)
@Composable
private fun FeedActionBarPreview() {
    val uuid = generateUUID()
    FeedActionBar(
        post = FeedLinePost(
            id = uuid,
            postUser = FeedLineUser(
                id = uuid,
                name = "何聚敛",
                avatarUrl = "https://i.pravatar.cc/300"
            ),
            content = "这是一条测试朋友圈内容",
        ),
        currentUser = FeedLineUser(
            id = uuid,
            name = "何聚敛",
            avatarUrl = "https://i.pravatar.cc/300"
        ),
        onLikeClick = { },
        onAddCommentClick = { },
        onDeletePostClick = { },
        currentTime = currentTimeMillis()
    )
}


