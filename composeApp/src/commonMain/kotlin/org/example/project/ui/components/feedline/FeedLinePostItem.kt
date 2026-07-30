/**
 * @File: FeedLinePostItem.kt
 * @Package: org.example.project.ui.components.feedline
 * @Description: 朋友圈单条动态帖子卡片核心组件（支持KMP跨平台视频与图片展示）
 * @Author: 何聚敛
 * @Date: 2026-07-22
 */
package org.example.project.ui.components.feedline

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import org.example.project.core.network.config.createFakePost
import org.example.project.data.repository.feedline.generateUUID
import org.example.project.domain.model.feedline.FeedLineComment
import org.example.project.domain.model.feedline.FeedLineMedia
import org.example.project.domain.model.feedline.FeedLinePost
import org.example.project.domain.model.feedline.FeedLineUser
import org.example.project.platform.currentTimeMillis
import org.example.project.ui.theme.feedline.FeedLineBackgroundGray
import org.example.project.ui.theme.feedline.FeedLineCommentBackgroundGray
import org.example.project.ui.theme.feedline.FeedLineLinkBlue

/**
 * 跨平台视频播放控件契约
 */
@Composable
expect fun VideoPlayer(
    videoUrl: String,
    modifier: Modifier = Modifier
)

@Composable
fun FeedPostItem(
    modifier: Modifier = Modifier,
    post: FeedLinePost,
    currentUser: FeedLineUser,
    onClick: (FeedLinePost) -> Unit, //整体被点击
    onNameClick: () -> Unit, //名字被点击
    onLikeClick: () -> Unit, //点赞
    onAddCommentClick: () -> Unit, //添加评论
    onCommentClick: (FeedLineComment) -> Unit, //点击评论
    onCommentUserClick: (FeedLineUser) -> Unit,
    onDeleteCommentClick: (FeedLineComment) -> Unit, //长按删除评论
    onDeletePostClick: (FeedLinePost) -> Unit, //删除帖子按钮
    onPostAvatarClick: () -> Unit,
    onLikedAvatarClick: (FeedLineUser) -> Unit,
    currentTime: Long
) {
    Row(
        modifier = modifier
            .background(Color.White)
            .fillMaxWidth()
            .padding(12.dp)
            .clickable {
                onClick(post)
            }
    ) {
        Avatar(
            url = post.postUser.avatarUrl,
            size = 40.dp,
            onClick = {
                onPostAvatarClick()
            }
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                modifier = Modifier
                    .clickable {
                        onNameClick()
                    },
                text = post.postUser.name,
                fontSize = 18.sp,
                color = FeedLineLinkBlue
            )

            if (post.content.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                SelectionContainer {
                    Text(
                        text = post.content,
                        fontWeight = FontWeight.Normal,
                        fontSize = 17.sp,
                        color = Color.Black
                    )
                }
            }

            val validMediaList = post.mediaList.filter {
                when (it) {
                    is FeedLineMedia.Image -> !it.url.isNullOrBlank()
                    is FeedLineMedia.Video -> !it.videoUrl.isNullOrBlank() || !it.coverUrl.isNullOrBlank()
                }
            }

            if (validMediaList.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                PostMediaGrid(mediaList = validMediaList)
            }

            Spacer(modifier = Modifier.height(6.dp))

            FeedActionBar(
                post = post,
                currentUser = currentUser,
                onLikeClick = onLikeClick,
                onAddCommentClick = onAddCommentClick,
                onDeletePostClick = { post ->
                    onDeletePostClick(post)
                },
                currentTime = currentTime
            )

            Column(
                modifier = Modifier
                    .background(
                        color = FeedLineCommentBackgroundGray,
                        shape = RoundedCornerShape(4.dp)
                    )
            ) {
                FeedLikedUserNameBar(
                    likedUserList = post.likedUsers,
                    onUserClick = { user ->
                        onLikedAvatarClick(user)
                    }
                )

                if (post.likedUsers.isNotEmpty() && post.commentsList.isNotEmpty()) {
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = Color.LightGray
                    )
                }

                FeedCommentList(
                    currentUser = currentUser,
                    commentsList = post.commentsList,
                    onCommentClick = { comment ->
                        onCommentClick(comment)
                    },
                    onDeleteCommentClick = { comment ->
                        onDeleteCommentClick(comment)
                    },
                    onCommentUserClick = { user ->
                        onCommentUserClick(user)
                    }
                )

            }
        }
    }

}

@Preview
@Composable
fun FeedPostItemPreview() {
    val uuid = generateUUID()
    val user = FeedLineUser(
        id = uuid,
        name = "何聚敛",
        avatarUrl = "https://i.pravatar.cc/300"
    )
    FeedPostItem(
        post = createFakePost(user),
        currentUser = user,
        onClick = { },
        onNameClick = {},
        onLikeClick = { },
        onAddCommentClick = { },
        onDeleteCommentClick = { },
        onDeletePostClick = { },
        onPostAvatarClick = { },
        onLikedAvatarClick = {},
        currentTime = currentTimeMillis(),
        onCommentClick = {},
        onCommentUserClick = {}
    )
}

@Composable
fun VideoPlayerDialog(
    videoUrl: String,
    onDismissRequest: () -> Unit
) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismissRequest,
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            VideoPlayer(
                videoUrl = videoUrl,
                modifier = Modifier.fillMaxSize()
            )

            IconButton(
                onClick = onDismissRequest,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "关闭",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun ImagePreviewDialog(
    imageUrl: String,
    onDismissRequest: () -> Unit
) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismissRequest,
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable { onDismissRequest() },
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "图片预览",
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                contentScale = ContentScale.Fit
            )

            IconButton(
                onClick = onDismissRequest,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "关闭",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun PostMediaGrid(
    mediaList: List<FeedLineMedia>,
    modifier: Modifier = Modifier
) {
    if (mediaList.isEmpty()) return

    var activeImageUrl by remember { mutableStateOf<String?>(null) }
    var activeVideoUrl by remember { mutableStateOf<String?>(null) }

    if (activeImageUrl != null) {
        ImagePreviewDialog(
            imageUrl = activeImageUrl!!,
            onDismissRequest = { activeImageUrl = null }
        )
    }

    if (activeVideoUrl != null) {
        VideoPlayerDialog(
            videoUrl = activeVideoUrl!!,
            onDismissRequest = { activeVideoUrl = null }
        )
    }

    if (mediaList.size == 1) {
        val media = mediaList.first()
        var mediaAspectRatio by remember(media) { mutableStateOf<Float?>(null) }
        val ratio = mediaAspectRatio?.coerceIn(0.5f, 2.0f) ?: 1.0f

        Box(
            modifier = modifier
                .fillMaxWidth(0.5f)
                .aspectRatio(ratio)
                .clip(RoundedCornerShape(4.dp))
                .background(FeedLineBackgroundGray)
                .clickable {
                    if (media is FeedLineMedia.Video) {
                        activeVideoUrl = media.videoUrl
                    } else if (media is FeedLineMedia.Image) {
                        activeImageUrl = media.url
                    }
                }
        ) {
            if (media is FeedLineMedia.Image) {
                if (media.url.isNotEmpty()) {
                    AsyncImage(
                        model = media.url,
                        contentDescription = null,
                        onSuccess = { state ->
                            val size = state.painter.intrinsicSize
                            if (size.width > 0f && size.height > 0f) {
                                mediaAspectRatio = size.width / size.height
                            }
                        },
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            } else if (media is FeedLineMedia.Video) {
                VideoThumbnail(
                    videoUrl = media.videoUrl,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    onAspectRatioLoaded = { loadedRatio ->
                        mediaAspectRatio = loadedRatio
                    }
                )
            }
            if (media is FeedLineMedia.Video) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "播放",
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.Center)
                )
            }
        }
    } else {
        val columns = if (mediaList.size == 4) 2 else 3
        val spacing = 4.dp
        val rows = mediaList.chunked(columns)

        Column(
            verticalArrangement = Arrangement.spacedBy(spacing),
            modifier = Modifier.fillMaxWidth()
        ) {
            rows.forEach { rowMedia ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(spacing),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    for (i in 0 until columns) {
                        if (i < rowMedia.size) {
                            val media = rowMedia[i]
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(FeedLineBackgroundGray)
                                    .clickable {
                                        if (media is FeedLineMedia.Video) {
                                            activeVideoUrl = media.videoUrl
                                        } else if (media is FeedLineMedia.Image) {
                                            activeImageUrl = media.url
                                        }
                                    }
                            ) {
                                if (media is FeedLineMedia.Image) {
                                    if (media.url.isNotEmpty()) {
                                        AsyncImage(
                                            model = media.url,
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                } else if (media is FeedLineMedia.Video) {
                                    VideoThumbnail(
                                        videoUrl = media.videoUrl,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                if (media is FeedLineMedia.Video) {
                                    Icon(
                                        imageVector = Icons.Filled.PlayArrow,
                                        contentDescription = "播放",
                                        tint = Color.White.copy(alpha = 0.8f),
                                        modifier = Modifier
                                            .size(32.dp)
                                            .align(Alignment.Center)
                                    )
                                }
                            }
                        } else {
                            Box(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}
