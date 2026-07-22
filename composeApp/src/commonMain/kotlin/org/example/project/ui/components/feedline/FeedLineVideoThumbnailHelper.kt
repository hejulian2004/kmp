/**
 * @File: FeedLineVideoThumbnailHelper.kt
 * @Package: org.example.project.ui.components.feedline
 * @Description: 朋友圈视频缩略图加载辅助逻辑（KMP跨平台expect/actual声明）
 * @Author: 何聚敛
 * @Date: 2026-07-22
 */
package org.example.project.ui.components.feedline

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale

/**
 * 跨平台异步获取视频首帧缩略图
 */
expect suspend fun loadVideoThumbnail(videoUrl: String): ImageBitmap?

/**
 * 视频封面组件，自动异步加载并渲染视频的第一帧作为封面
 */
@Composable
fun VideoThumbnail(
    videoUrl: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    onAspectRatioLoaded: ((Float) -> Unit)? = null
) {
    var thumbnail by remember(videoUrl) { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(videoUrl) {
        val img = loadVideoThumbnail(videoUrl)
        thumbnail = img
        if (img != null && img.width > 0 && img.height > 0) {
            onAspectRatioLoaded?.invoke(img.width.toFloat() / img.height.toFloat())
        }
    }

    if (thumbnail != null) {
        Image(
            bitmap = thumbnail!!,
            contentDescription = "视频封面",
            modifier = modifier,
            contentScale = contentScale
        )
    } else {
        Box(
            modifier = modifier.background(Color(0xFFEFEFEF))
        )
    }
}
