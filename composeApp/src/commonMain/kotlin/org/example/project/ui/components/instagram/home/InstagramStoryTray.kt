/**
 * @File: InstagramStoryTray.kt
 * @Package: org.example.project.ui.components.instagram.home
 * @Description: Instagram首页Story快拍顶部横向滚动栏组件（含彩虹渐变圈、加号故事、LIVE直播提示）
 * @Author: 何聚敛
 * @Date: 2026-07-29
 */
package org.example.project.ui.components.instagram.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import org.example.project.data.repository.instagram.createFakeInstagramStories
import org.example.project.domain.model.instagram.InstagramPost
import org.example.project.ui.theme.instagram.InstagramBlue
import org.example.project.ui.theme.instagram.InstagramDarkGray
import org.example.project.ui.theme.instagram.InstagramLiveGradientEnd
import org.example.project.ui.theme.instagram.InstagramLiveGradientStart
import org.example.project.ui.theme.instagram.InstagramStoryPink
import org.example.project.ui.theme.instagram.InstagramStoryPurple
import org.example.project.ui.theme.instagram.InstagramStoryYellow
import org.example.project.ui.theme.instagram.InstagramTheme
import kotlinproject.composeapp.generated.resources.Res
import kotlinproject.composeapp.generated.resources.ins_live
import kotlinproject.composeapp.generated.resources.ins_your_story
import org.jetbrains.compose.resources.stringResource

/**
 * Instagram首页顶部Story快拍横向滑动行组件
 *
 * 包含：  
 * - 个人“Your story”快拍入口（含加号Badge）
 * - 好友快拍入口（根据未读状态展示彩虹渐变圈或灰色圈，支持LIVE直播标识）
 *
 * @param stories快拍数据列表（使用统一InstagramPost实体）
 * @param onStoryClick点击某个快拍回调
 * @param onAddStoryClick点击发布个人快拍回调
 * @param modifier外部Modifier修饰符
 */
@Composable
fun InstagramStoryTray(
    stories: List<InstagramPost>,
    onStoryClick: (InstagramPost) -> Unit = {},
    onAddStoryClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(
            items = stories,
            key = { story -> story.id }
        ) { story ->
            if (story.postUser.userId == "u_me") {
                AddStoryItem(
                    story = story,
                    onAddStoryClick = onAddStoryClick
                )
            } else {
                StoryItem(
                    story = story,
                    onStoryClick = { onStoryClick(story) }
                )
            }
        }
    }
}

/**
 * 个人发布Story快拍加号项组件
 *
 * @param story当前用户的快拍信息
 * @param onAddStoryClick点击加号事件回调
 */
@Composable
private fun AddStoryItem(
    story: InstagramPost,
    onAddStoryClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(72.dp)
            .clickable { onAddStoryClick() }
    ) {
        Box(
            modifier = Modifier.size(70.dp),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = story.postUser.avatarUrl,
                contentDescription = story.postUser.username,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(62.dp)
                    .clip(CircleShape)
                    .border(1.dp, Color.LightGray.copy(alpha = 0.5f), CircleShape)
            )

            // 暗灰色加号Badge图标（对齐最新原生Instagram）
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = (-2).dp, y = (-2).dp)
                    .clip(CircleShape)
                    .background(InstagramDarkGray)
                    .border(2.dp, MaterialTheme.colorScheme.background, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Story",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = stringResource(Res.string.ins_your_story),
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * 好友Story快拍圆环项组件
 *
 * @param story帖子快拍实体
 * @param onStoryClick点击事件回调
 */
@Composable
private fun StoryItem(
    story: InstagramPost,
    onStoryClick: () -> Unit
) {
    val isUnread = story.unreadNotificationCount > 0
    val isLive = story.audioTitle == "LIVE"

    val storyBorderBrush = if (isUnread) {
        Brush.linearGradient(
            colors = listOf(
                InstagramStoryPurple,
                InstagramStoryPink,
                InstagramStoryYellow
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color.LightGray.copy(alpha = 0.6f),
                Color.LightGray.copy(alpha = 0.6f)
            )
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(72.dp)
            .clickable { onStoryClick() }
    ) {
        Box(
            modifier = Modifier.size(70.dp),
            contentAlignment = Alignment.Center
        ) {
            // 彩虹/灰色渐变圆环外边框
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(storyBorderBrush)
                    .padding(2.5.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.background)
                        .padding(2.dp)
                ) {
                    AsyncImage(
                        model = story.postUser.avatarUrl,
                        contentDescription = story.postUser.username,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                    )
                }
            }

            // 可选LIVE直播标识Badge
            if (isLive) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = 2.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(InstagramLiveGradientStart, InstagramLiveGradientEnd)
                            )
                        )
                        .padding(horizontal = 4.dp, vertical = 1.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(Res.string.ins_live),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = story.postUser.username,
            fontSize = 11.sp,
            fontWeight = if (isUnread) FontWeight.SemiBold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
fun InstagramStoryTrayPreview() {
    InstagramTheme {
        InstagramStoryTray(
            stories = createFakeInstagramStories()
        )
    }
}
