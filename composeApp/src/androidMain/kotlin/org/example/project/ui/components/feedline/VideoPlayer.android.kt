/**
 * @File: VideoPlayer.android.kt
 * @Package: org.example.project.ui.components.feedline
 * @Description: Android平台视频播放组件实现
 * @Author: 何聚敛
 * @Date: 2026-07-22
 */
package org.example.project.ui.components.feedline

import android.widget.VideoView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import org.example.project.ui.core.video.AppVideoCacheManager

@Composable
actual fun VideoPlayer(
    videoUrl: String,
    modifier: Modifier
) {
    var resolvedUrl by remember(videoUrl) { mutableStateOf(videoUrl) }

    LaunchedEffect(videoUrl) {
        resolvedUrl = AppVideoCacheManager.getPlayableVideoUrl(videoUrl)
    }

    AndroidView(
        factory = { context ->
            VideoView(context).apply {
                val mediaController = android.widget.MediaController(context)
                mediaController.setAnchorView(this)
                setMediaController(mediaController)
                setVideoPath(resolvedUrl)
                setOnPreparedListener { mp ->
                    mp.isLooping = true
                    start()
                }
            }
        },
        update = { view ->
            // 缓存就绪或视频更新
        },
        modifier = modifier
    )
}
