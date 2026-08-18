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

    return try {
        val bytes = file.readBytes()
        if (bytes.isNotEmpty()) {
            AppStorageInitializer.container.fileStorage.write(
                area = StorageArea.PERSISTENT,
                path = relativePath,
                data = bytes
            )
            val persistentPath = "${AppStorageInitializer.container.directories.persistent}/feedline/media/$targetFileName"
            if (isVideo) {
                FeedLineMedia.Video(coverUrl = persistentPath, videoUrl = persistentPath)
            } else {
                FeedLineMedia.Image(url = persistentPath)
            }
        } else {
            val fallbackPath = file.path ?: ""
            if (isVideo) {
                FeedLineMedia.Video(coverUrl = fallbackPath, videoUrl = fallbackPath)
            } else {
                FeedLineMedia.Image(url = fallbackPath)
            }
        }
    } catch (_: Throwable) {
        val fallbackPath = file.path ?: ""
        if (isVideo) {
            FeedLineMedia.Video(coverUrl = fallbackPath, videoUrl = fallbackPath)
        } else {
            FeedLineMedia.Image(url = fallbackPath)
        }
    }
}
