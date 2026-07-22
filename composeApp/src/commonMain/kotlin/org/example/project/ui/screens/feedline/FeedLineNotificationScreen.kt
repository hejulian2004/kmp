/**
 * @File: FeedLineNotificationScreen.kt
 * @Package: org.example.project.ui.screens.feedline
 * @Description: 互动消息通知列表界面的 Compose 视图
 * @Author: 何聚敛
 * @Date: 2026-07-22
 */
package org.example.project.ui.screens.feedline

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import org.example.project.data.repository.feedline.FeedRepositoryImpl
import org.example.project.domain.model.feedline.FeedLineMedia
import org.example.project.domain.model.feedline.FeedLineNotification
import org.example.project.domain.model.feedline.FeedLineUser
import org.example.project.presentation.intent.feedline.FeedIntent
import org.example.project.presentation.state.feedline.Screen
import org.example.project.presentation.viewmodel.feedline.FeedLineViewModel
import org.example.project.ui.components.feedline.Avatar
import org.example.project.ui.components.feedline.VideoThumbnail
import org.example.project.utils.TimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    viewModel: FeedLineViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    // 获取所有未删除的已读和未读通知
    val notifications = uiState.notifications.filter { !it.isDelete }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Color(0xFFF5F5F5))
                    .statusBarsPadding()
                    .height(56.dp)
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                // 返回按钮
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterStart),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .clickable {
                                viewModel.handleIntent(FeedIntent.NavigateTo(Screen.Feed))
                            }
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "朋友圈",
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                    }
                }

                // 标题
                Text(
                    text = "消息",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                // 清空按钮
                if (notifications.isNotEmpty()) {
                    TextButton(
                        onClick = {
                            viewModel.handleIntent(FeedIntent.ClearAllNotifications)
                        },
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Text(
                            text = "清空",
                            fontSize = 16.sp,
                            color = Color(0xFF576B95),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        },
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding)
        ) {
            if (notifications.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "暂无新消息",
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(
                        items = notifications,
                        key = { it.id }
                    ) { notification ->
                        NotificationItem(notification = notification)
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = Color.LightGray.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationItem(
    notification: FeedLineNotification,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左侧：头像
        Avatar(
            url = notification.user.avatarUrl,
            size = 42.dp,
            onClick = {}
        )

        Spacer(modifier = Modifier.width(12.dp))

        // 中间：用户名、动作和时间
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = notification.user.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF576B95)
            )

            Spacer(modifier = Modifier.height(4.dp))

            if (notification.isLikeNotification) {
                Text(
                    text = "赞了你的动态",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            } else {
                Text(
                    text = notification.comment?.content ?: "评论了你的动态",
                    fontSize = 14.sp,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = TimeUtils.formatTime(notification.createdTime),
                fontSize = 12.sp,
                color = Color.LightGray
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // 右侧：原始动态预览
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFFF5F5F5))
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            val media = notification.post.mediaList.firstOrNull()
            if (media != null) {
                if (media is FeedLineMedia.Image) {
                    if (media.url.isNotEmpty()) {
                        AsyncImage(
                            model = media.url,
                            contentDescription = "动态配图",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        PostTextPreview(text = notification.post.content)
                    }
                } else if (media is FeedLineMedia.Video) {
                    VideoThumbnail(
                        videoUrl = media.videoUrl,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            } else {
                PostTextPreview(text = notification.post.content)
            }
        }
    }
}

@Composable
private fun PostTextPreview(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        color = Color.Gray,
        maxLines = 3,
        overflow = TextOverflow.Ellipsis,
        lineHeight = 13.sp
    )
}

@Preview(showBackground = true)
@Composable
fun NotificationScreenPreview() {
    val fakeRepo = FeedRepositoryImpl()
    val fakeUser = FeedLineUser(id = "1", name = "测试用户", avatarUrl = "")
    val fakeViewModel = FeedLineViewModel(fakeRepo, fakeUser)
    NotificationScreen(viewModel = fakeViewModel)
}
