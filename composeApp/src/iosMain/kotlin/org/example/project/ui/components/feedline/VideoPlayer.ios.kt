/**
 * @File: VideoPlayer.ios.kt
 * @Package: org.example.project.ui.components.feedline
 * @Description: iOS平台AVPlayer视频播放组件实现（本轮不启用自动持久视频预加载）
 * @Author: 何聚敛
 * @Date: 2026-07-22
 */
package org.example.project.ui.components.feedline

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitViewController
import kotlinx.cinterop.ExperimentalForeignApi
import org.example.project.ui.core.video.AppVideoCacheManager
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.pause
import platform.AVFoundation.play
import platform.AVKit.AVPlayerViewController
import platform.Foundation.NSURL

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun VideoPlayer(
    videoUrl: String,
    modifier: Modifier
) {
    var resolvedUrl by remember(videoUrl) { mutableStateOf(videoUrl) }

    LaunchedEffect(videoUrl) {
        resolvedUrl = AppVideoCacheManager.getPlayableVideoUrl(videoUrl)
    }

    val player = remember(resolvedUrl) {
        val url = if (resolvedUrl.startsWith("http://") || resolvedUrl.startsWith("https://")) {
            NSURL.URLWithString(resolvedUrl)
        } else {
            NSURL.fileURLWithPath(resolvedUrl)
        }
        url?.let { AVPlayer.playerWithURL(it) }
    }

    DisposableEffect(player) {
        player?.play()
        onDispose {
            player?.pause()
        }
    }

    UIKitViewController(
        factory = {
            AVPlayerViewController().apply {
                this.player = player
                showsPlaybackControls = true
            }
        },
        modifier = modifier,
        properties = UIKitInteropProperties(),
        update = { controller ->
            controller.player = player
        }
    )
}
