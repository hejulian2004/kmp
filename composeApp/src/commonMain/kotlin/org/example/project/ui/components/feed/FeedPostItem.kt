package org.example.project.ui.components.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.data.createFakePost
import org.example.project.domain.model.FeedComment
import org.example.project.domain.model.FeedPost
import org.example.project.domain.model.FeedUser
import java.util.UUID
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.viewinterop.AndroidView
import coil3.compose.AsyncImage
import org.example.project.domain.model.FeedMedia

@Composable
fun FeedPostItem(
    modifier: Modifier = Modifier,
    post: FeedPost,
    currentUser: FeedUser,
    onClick: (FeedPost) -> Unit,//整体被点击
    onNameClick: () -> Unit,//名字被点击
    onLikeClick: () -> Unit,//点赞
    onAddCommentClick: () -> Unit,//添加评论
    onCommentClick: (FeedComment) -> Unit, //点击评论
    onCommentUserClick: (FeedUser) -> Unit,
    onDeleteCommentClick: (FeedComment) -> Unit,//长按删除评论
    onDeletePostClick: (FeedPost) -> Unit,//删除帖子按钮
    onPostAvatarClick:() -> Unit,
    onLikedAvatarClick: (FeedUser) -> Unit,
    currentTime: Long
) {
    Row(
        modifier = modifier
            .background(Color.White)
            .fillMaxWidth()
            .padding(12.dp)
            .clickable{
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
                    .clickable{
                        onNameClick()
                    },
                text = post.postUser.name,
                fontSize = 18.sp,
                color = Color(0xFF576B95)
            )

            if(!post.content.isEmpty()){
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

            val validMediaList = post.mediaList?.filter {
                when (it) {
                    is FeedMedia.Image -> !it.url.isNullOrBlank()
                    is FeedMedia.Video -> !it.videoUrl.isNullOrBlank() || !it.coverUrl.isNullOrBlank()
                }
            } ?: emptyList()

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
                        color = Color(0xFFF3F3F3),
                        shape = RoundedCornerShape(4.dp)
                    )
            ) {
                FeedLikedUserAvatarBar(
                    likedUserList = post.likedUsers,
                    onUserClick = { user ->
                        onLikedAvatarClick(user)
                    }
                )

                if(post.likedUsers.isNotEmpty()&&post.commentsList.isNotEmpty()){
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
fun FeedPostItemPreview(){
    val uuid = UUID.randomUUID().toString()
    val user = FeedUser(
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
        currentTime = System.currentTimeMillis(),
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
            AndroidView(
                factory = { context ->
                    android.widget.VideoView(context).apply {
                        val mediaController = android.widget.MediaController(context)
                        mediaController.setAnchorView(this)
                        setMediaController(mediaController)
                        setVideoPath(videoUrl)
                        setOnPreparedListener { mp ->
                            mp.isLooping = true
                            start()
                        }
                    }
                },
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
    mediaList: List<FeedMedia>,
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
                .background(Color(0xFFF5F5F5))
                .clickable {
                    if (media is FeedMedia.Video) {
                        activeVideoUrl = media.videoUrl
                    } else if (media is FeedMedia.Image) {
                        activeImageUrl = media.url
                    }
                }
        ) {
            if (media is FeedMedia.Image) {
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
            } else if (media is FeedMedia.Video) {
                VideoThumbnail(
                    videoUrl = media.videoUrl,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    onAspectRatioLoaded = { loadedRatio ->
                        mediaAspectRatio = loadedRatio
                    }
                )
            }
            if (media is FeedMedia.Video) {
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
                                    .background(Color(0xFFF5F5F5))
                                    .clickable {
                                        if (media is FeedMedia.Video) {
                                            activeVideoUrl = media.videoUrl
                                        } else if (media is FeedMedia.Image) {
                                            activeImageUrl = media.url
                                        }
                                    }
                            ) {
                                if (media is FeedMedia.Image) {
                                    if (media.url.isNotEmpty()) {
                                        AsyncImage(
                                            model = media.url,
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                } else if (media is FeedMedia.Video) {
                                    VideoThumbnail(
                                        videoUrl = media.videoUrl,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                if (media is FeedMedia.Video) {
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
