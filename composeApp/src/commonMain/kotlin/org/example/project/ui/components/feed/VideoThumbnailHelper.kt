/**
 * @File: VideoThumbnailHelper.kt
 * @Package: org.example.project.ui.components.feed
 * @Description: 视频封面帧提取的跨平台辅助工具类
 * @Date: 2026-07-20
 */
package org.example.project.ui.components.feed

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 原生获取视频第一帧的 Bitmap 缩略图工具方法
 */
suspend fun getVideoThumbnail(context: android.content.Context, videoUri: String): Bitmap? {
    return withContext(Dispatchers.IO) {
        val retriever = MediaMetadataRetriever()
        try {
            if (videoUri.startsWith("http://") || videoUri.startsWith("https://")) {
                retriever.setDataSource(videoUri, HashMap<String, String>())
            } else {
                retriever.setDataSource(context, Uri.parse(videoUri))
            }
            retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            try {
                retriever.release()
            } catch (e: Exception) {
                // Ignore
            }
        }
    }
}

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
    val context = LocalContext.current
    var thumbnail by remember(videoUrl) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(videoUrl) {
        val bmp = getVideoThumbnail(context, videoUrl)
        thumbnail = bmp
        if (bmp != null && bmp.width > 0 && bmp.height > 0) {
            onAspectRatioLoaded?.invoke(bmp.width.toFloat() / bmp.height.toFloat())
        }
    }

    if (thumbnail != null) {
        Image(
            bitmap = thumbnail!!.asImageBitmap(),
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



