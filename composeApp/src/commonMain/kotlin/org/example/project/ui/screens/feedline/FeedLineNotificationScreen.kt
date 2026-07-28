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
import androidx.compose.material3.CircularProgressIndicator
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
import org.example.project.presentation.state.UiState
import org.example.project.presentation.state.feedline.Screen
import org.example.project.presentation.viewmodel.feedline.FeedLineViewModel
import org.example.project.ui.components.feedline.Avatar
import org.example.project.ui.components.feedline.VideoThumbnail
import org.example.project.ui.theme.feedline.FeedLineBackgroundGray
import org.example.project.ui.theme.feedline.FeedLineLinkBlue
import org.example.project.utils.TimeUtils
import kotlinproject.composeapp.generated.resources.Res
import kotlinproject.composeapp.generated.resources.back
import kotlinproject.composeapp.generated.resources.feedline_clear
import kotlinproject.composeapp.generated.resources.feedline_commented_your_post
import kotlinproject.composeapp.generated.resources.feedline_liked_your_post
import kotlinproject.composeapp.generated.resources.feedline_no_notifications
import kotlinproject.composeapp.generated.resources.feedline_notification_title
import kotlinproject.composeapp.generated.resources.feedline_title
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    viewModel: FeedLineViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val notificationsState = uiState.notificationsState
    // 获取所有未删除的已读和未读通知
    val notifications = (notificationsState as? UiState.Success)?.data?.filter { !it.isDelete } ?: emptyList()

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = FeedLineBackgroundGray)
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
                            contentDescription = stringResource(Res.string.back),
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(Res.string.feedline_title),
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                    }
                }

                // 标题
                Text(
                    text = stringResource(Res.string.feedline_notification_title),
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
                            text = stringResource(Res.string.feedline_clear),
                            fontSize = 16.sp,
                            color = FeedLineLinkBlue,
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
            when (notificationsState) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(40.dp))
                    }
                }
                is UiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = notificationsState.message, color = Color.Red)
                    }
                }
                is UiState.Success, is UiState.Idle -> {
                    if (notifications.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(Res.string.feedline_no_notifications),
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

        // 中间：用户名、动作 and 时间
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = notification.user.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = FeedLineLinkBlue
            )

            Spacer(modifier = Modifier.height(4.dp))

            if (notification.isLikeNotification) {
                Text(
                    text = stringResource(Res.string.feedline_liked_your_post),
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            } else {
                Text(
                    text = notification.comment?.content ?: stringResource(Res.string.feedline_commented_your_post),
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
                .background(FeedLineBackgroundGray)
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
