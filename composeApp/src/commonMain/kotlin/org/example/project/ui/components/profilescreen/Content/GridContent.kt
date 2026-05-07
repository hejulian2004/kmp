package org.example.project.ui.components.profilescreen.Content

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter

@Composable
fun GridContent(
    modifier: Modifier = Modifier,
    posts: List<ContentThumbnailData> = emptyList(),
    isLoadingMore: Boolean = false,
    hasMore: Boolean = true,
    minItemWidth: Dp = 100.dp,
    itemSpacing: Dp = 2.dp,
    emptyContent: @Composable () -> Unit = {},
    onLoadMore: () -> Unit = {},
    onItemClick: (id: String) -> Unit = {},
    onItemLongClick: (id: String) -> Unit = {},
) {
    if (posts.isEmpty()) {
        Box(modifier = modifier) { emptyContent() }
        return
    }
    val listState = rememberLazyGridState()

    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            val total = listState.layoutInfo.totalItemsCount
            hasMore && !isLoadingMore && lastVisible >= total - 3
        }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) onLoadMore()
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minItemWidth),
        state = listState,
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(itemSpacing),
        verticalArrangement = Arrangement.spacedBy(itemSpacing),
    ) {
        items(
            items = posts,
            key = { it.id },
        ) { post ->
            ContentThumbnail(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                data = post,
                onClick = { onItemClick(post.id) },
                onLongClick = { onItemLongClick(post.id) },
            )
        }

        if (isLoadingMore) {
            item(
                key = "loading_footer",
                span = { GridItemSpan(maxLineSpan) }
            ) {
                LoadingFooter()
            }
        }
    }
}

@Composable
private fun LoadingFooter(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            strokeWidth = 2.dp,
            color = Color(0xFF999999),
        )
    }
}

@Composable
fun PostEmptyState(
    modifier: Modifier = Modifier,
    title: String = "发布你的第一个帖子",
    subtitle: String = "让这个空间充满爱。",
    actionLabel: String = "创建",
    onAction: () -> Unit = {},
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        Column(
            modifier = Modifier.align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
            OutlinedButton(
                onClick = onAction,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(width = 120.dp, height = 48.dp),
            ) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
fun ReelsEmptyState(
    modifier: Modifier = Modifier,
    title: String = "与世界分享精彩时刻",
    actionLabel: String = "创建首条Reels",
    onAction: () -> Unit = {},
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        Column(
            modifier = Modifier.align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            OutlinedButton(
                onClick = onAction,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
            ) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
fun TaggedEmptyState(
    modifier: Modifier = Modifier,
    title: String = "与世界分享精彩时刻",
    subtitle: String = "标记你的照片和视频都展示在这里",
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}


@Preview
@Composable
fun GridContentPreview(){
    GridContent()
}