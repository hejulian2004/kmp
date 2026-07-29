/**
 * @File: InstagramPostMediaCarousel.kt
 * @Package: org.example.project.ui.components.instagram.home
 * @Description: Instagram Post动态帖子媒体图片与视频多图轮播组件（包含双击红心动画、右上方页码标识及下方居中多图圆点指示器）
 * @Author: 何聚敛
 * @Date: 2026-07-29
 */
package org.example.project.ui.components.instagram.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.example.project.data.repository.instagram.createFakeInstagramPosts
import org.example.project.domain.model.instagram.InstagramMedia
import org.example.project.domain.model.instagram.InstagramPost
import org.example.project.ui.theme.instagram.InstagramBlue
import org.example.project.ui.theme.instagram.InstagramOverlayDark
import org.example.project.ui.theme.instagram.InstagramTheme

/**
 * Post媒体图片/视频多图轮播组件
 *
 * @param post 帖子实体
 * @param pagerState 外部传入或内部记忆的PagerState状态
 * @param onMediaClick 点击当前媒体回调
 * @param onDoubleTapLike 双击媒体触发点赞回调
 * @param modifier 外部修饰符
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InstagramPostMediaCarousel(
    post: InstagramPost,
    pagerState: PagerState = rememberPagerState(pageCount = {
        post.mediaList.ifEmpty { listOf(InstagramMedia.Image("")) }.size
    }),
    onMediaClick: (Int) -> Unit = {},
    onDoubleTapLike: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showDoubleTapHeart by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val mediaList = post.mediaList.ifEmpty {
        listOf(InstagramMedia.Image("https://picsum.photos/seed/${post.id}/1080/1080"))
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { onMediaClick(pagerState.currentPage) },
                        onDoubleTap = {
                            onDoubleTapLike()
                            showDoubleTapHeart = true
                            coroutineScope.launch {
                                delay(800)
                                showDoubleTapHeart = false
                            }
                        }
                    )
                }
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth()
            ) { page ->
                val media = mediaList[page]
                val imageUrl = when (media) {
                    is InstagramMedia.Image -> media.url
                    is InstagramMedia.Video -> media.coverUrl ?: media.videoUrl
                }
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Post Media ${page + 1}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                )
            }

            // 双击浮现的大红心遮罩动画
            DoubleTapHeartOverlay(
                visible = showDoubleTapHeart,
                modifier = Modifier.align(Alignment.Center)
            )

            // 轮播图右上角页码指示器
            if (mediaList.size > 1) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .clip(CircleShape)
                        .background(InstagramOverlayDark)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${pagerState.currentPage + 1}/${mediaList.size}",
                        fontSize = 11.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // 多图轮播圆点指示器（紧贴媒体组件正下方居中显示）
        if (mediaList.size > 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp, bottom = 2.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(mediaList.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .size(if (isSelected) 6.dp else 5.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) InstagramBlue
                                else Color.Gray.copy(alpha = 0.4f)
                            )
                    )
                }
            }
        }
    }
}

/**
 * 双击点赞大红心遮罩动画组件
 */
@Composable
private fun DoubleTapHeartOverlay(
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(animationSpec = spring(dampingRatio = 0.5f)) + fadeIn(),
        exit = scaleOut() + fadeOut(),
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Filled.Favorite,
            contentDescription = "Like Animation",
            tint = Color.White.copy(alpha = 0.9f),
            modifier = Modifier.size(110.dp)
        )
    }
}

/**
 * Instagram Post媒体轮播组件Composable预览函数
 */
@OptIn(ExperimentalFoundationApi::class)
@Preview
@Composable
fun InstagramPostMediaCarouselPreview() {
    InstagramTheme {
        val fakePosts = createFakeInstagramPosts()
        if (fakePosts.isNotEmpty()) {
            InstagramPostMediaCarousel(
                post = fakePosts.first()
            )
        }
    }
}
