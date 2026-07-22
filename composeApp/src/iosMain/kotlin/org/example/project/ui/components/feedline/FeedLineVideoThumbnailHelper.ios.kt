/**
 * @File: FeedLineVideoThumbnailHelper.ios.kt
 * @Package: org.example.project.ui.components.feedline
 * @Description: 朋友圈视频缩略图加载iOS端具体实现
 * @Author: 何聚敛
 * @Date: 2026-07-22
 */
package org.example.project.ui.components.feedline

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.skia.Image
import platform.AVFoundation.AVAsset
import platform.AVFoundation.AVAssetImageGenerator
import platform.CoreMedia.CMTimeMake
import platform.Foundation.NSURL
import platform.UIKit.UIImagePNGRepresentation
import platform.posix.memcpy

@OptIn(ExperimentalForeignApi::class)
actual suspend fun loadVideoThumbnail(videoUrl: String): ImageBitmap? {
    return withContext(Dispatchers.Default) {
        try {
            val url = if (videoUrl.startsWith("http://") || videoUrl.startsWith("https://")) {
                NSURL.URLWithString(videoUrl)
            } else {
                NSURL.fileURLWithPath(videoUrl)
            } ?: return@withContext null

            val asset = AVAsset.assetWithURL(url)
            val generator = AVAssetImageGenerator.assetImageGeneratorWithAsset(asset)
            generator.appliesPreferredTrackTransform = true

            val cgImage = generator.copyCGImageAtTime(CMTimeMake(0, 1), null, null) ?: return@withContext null
            val uiImage = platform.UIKit.UIImage.imageWithCGImage(cgImage)
            val nsData = UIImagePNGRepresentation(uiImage) ?: return@withContext null
            val bytes = ByteArray(nsData.length.toInt()).apply {
                usePinned { pinned ->
                    memcpy(pinned.addressOf(0), nsData.bytes, nsData.length)
                }
            }
            Image.makeFromEncoded(bytes).toComposeImageBitmap()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
