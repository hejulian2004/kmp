/**
 * @File: VideoPlayer.android.kt
 * @Package: org.example.project.ui.components.feedline
 * @Description: Android平台视频播放组件实现
 * @Author: 何聚敛
 * @Date: 2026-07-22
 */
package org.example.project.ui.components.feedline

import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import org.example.project.ui.core.video.AndroidVideoCache
import java.io.File

/**
 * 将视频播放地址转换为Media3可读取的Uri，兼容应用持久化的绝对文件路径和平台Uri。
 */
internal fun videoUriForPlayback(videoUrl: String): Uri {
    val uri = Uri.parse(videoUrl)
    return if (!videoUrl.contains("://") && File(videoUrl).isAbsolute) {
        Uri.fromFile(File(videoUrl))
    } else {
        uri
    }
}

@OptIn(UnstableApi::class)
@Composable
actual fun VideoPlayer(
    videoUrl: String,
    modifier: Modifier
) {
    val context = LocalContext.current

    val exoPlayer = remember(videoUrl) {
        val cacheDataSourceFactory = AndroidVideoCache.createCacheDataSourceFactory(context)
        val mediaSourceFactory = DefaultMediaSourceFactory(cacheDataSourceFactory)
        
        ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()
            .apply {
                val mediaItem = MediaItem.fromUri(videoUriForPlayback(videoUrl))
                setMediaItem(mediaItem)
                repeatMode = Player.REPEAT_MODE_ALL
                playWhenReady = true
                prepare()
            }
    }

    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer.stop()
            exoPlayer.release()
        }
    }

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
        },
        update = { playerView ->
            if (playerView.player != exoPlayer) {
                playerView.player = exoPlayer
            }
        },
        modifier = modifier
    )
}
