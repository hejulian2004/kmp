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
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@Composable
actual fun VideoPlayer(
    videoUrl: String,
    modifier: Modifier
) {
    AndroidView(
        factory = { context ->
            VideoView(context).apply {
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
        modifier = modifier
    )
}
