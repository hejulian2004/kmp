/**
 * @File: MediaPersister.kt
 * @Package: org.example.project.ui.utils
 * @Description: 选择或拍摄媒体文件持久化助手（将临时URI与缓存文件复制至私有存储以保障重启后展示）
 * @Author: 何聚敛
 * @Date: 2026-08-18
 */
package org.example.project.ui.utils

import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.path
import io.github.vinceglb.filekit.readBytes
import io.github.vinceglb.filekit.source
import kotlinx.io.buffered
import org.example.project.core.storage.api.StorageArea
import org.example.project.core.storage.api.StoragePath
import org.example.project.core.storage.client.AppStorageInitializer
import org.example.project.data.repository.feedline.generateUUID
import org.example.project.domain.model.feedline.FeedLineMedia

/**
 * 将选择或拍摄的 PlatformFile 媒体持久化保存至应用私有持久化目录。
 * 
 * 解决 Android / iOS 上相册选取临时 URI 权限在软件重启后失效的问题。
 */
suspend fun persistPickedMedia(file: PlatformFile): FeedLineMedia {
    val fileName = file.name
    val isVideo = fileName.endsWith(".mp4", ignoreCase = true) ||
            fileName.endsWith(".mov", ignoreCase = true) ||
            fileName.endsWith(".mkv", ignoreCase = true)

    val extension = if (fileName.contains('.')) fileName.substringAfterLast('.') else if (isVideo) "mp4" else "jpg"
    val targetFileName = "${generateUUID()}.$extension"
    val relativePath = StoragePath("feedline/media/$targetFileName")

    val fileStorage = AppStorageInitializer.container.fileStorage
    val persistentRoot = AppStorageInitializer.container.directories.persistent
    val persistentPath = "$persistentRoot/feedline/media/$targetFileName"

    try {
        fileStorage.copyFile(
            area = StorageArea.PERSISTENT,
            path = relativePath,
            source = file.source().buffered(),
            bufferSizeBytes = 128 * 1024
        )
        return if (isVideo) {
            FeedLineMedia.Video(coverUrl = persistentPath, videoUrl = persistentPath)
        } else {
            FeedLineMedia.Image(url = persistentPath)
        }
    } catch (_: Throwable) {
        // 流式复制失败时继续回退
    }

    // 视频不再回退到readBytes()，避免大文件整块进入内存。
    if (isVideo) {
        val fallbackPath = file.path
        return FeedLineMedia.Video(coverUrl = fallbackPath, videoUrl = fallbackPath)
    }

    // 图片才允许使用小文件字节回退；视频路径禁止进入readBytes()。
    return try {
        val bytes = file.readBytes()
        if (bytes.isNotEmpty()) {
            fileStorage.write(
                area = StorageArea.PERSISTENT,
                path = relativePath,
                data = bytes
            )
            FeedLineMedia.Image(url = persistentPath)
        } else {
            val fallbackPath = file.path
            FeedLineMedia.Image(url = fallbackPath)
        }
    } catch (_: Throwable) {
        val fallbackPath = file.path
        FeedLineMedia.Image(url = fallbackPath)
    }
}
