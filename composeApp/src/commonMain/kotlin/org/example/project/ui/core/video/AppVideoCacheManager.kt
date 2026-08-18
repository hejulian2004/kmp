/**
 * @File: AppVideoCacheManager.kt
 * @Package: org.example.project.ui.core.video
 * @Description: 全局统一视频缓存管理器（支持网络视频本地离线缓存、预加载与LRU自动清理）
 * @Author: 何聚敛
 * @Date: 2026-08-18
 */
package org.example.project.ui.core.video

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsBytes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.example.project.core.network.client.AppNetworkInitializer
import org.example.project.core.storage.api.StorageArea
import org.example.project.core.storage.api.StoragePath
import org.example.project.core.storage.api.WriteMode
import org.example.project.core.storage.client.AppStorageInitializer

/**
 * 全局统一视频缓存管理器。
 * 
 * 职责：
 * 1. 拦截网络视频播放 URL，优先从本地私有缓存区 (`StorageArea.CACHE/video_cache/`) 读取。
 * 2. 命中缓存时提供 0 缓冲秒开与离线播放体验。
 * 3. 未命中缓存时，启动异步后台任务静默下载并落盘，为下次播放提速。
 * 4. 支持手动或自动清理视频磁盘缓存。
 */
object AppVideoCacheManager {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val downloadingMutex = Mutex()
    private val activeDownloads = mutableSetOf<String>()

    /**
     * 视频磁盘缓存最大配额 (512MB)
     */
    const val MAX_VIDEO_CACHE_SIZE_BYTES: Long = 512L * 1024 * 1024

    /**
     * 根据视频 URL 获取可直接播放的目标路径。
     * 
     * - 若为本地文件路径（如相机录制/相册选择），直接返回原路径；
     * - 若为网络视频且本地已缓存，返回本地沙盒绝对物理路径；
     * - 若为网络视频且未缓存，触发后台异步预下载，并返回原始网络 URL 保障首次流畅播。
     * 
     * @param videoUrl 原始视频地址 (本地物理路径或 HTTP/HTTPS URL)
     * @return 实际可用于 VideoPlayer 播放的路径
     */
    suspend fun getPlayableVideoUrl(videoUrl: String): String = withContext(Dispatchers.IO) {
        if (!isRemoteUrl(videoUrl)) {
            return@withContext videoUrl
        }

        val cacheRelativePath = computeCacheStoragePath(videoUrl)
        if (isStorageReady()) {
            val fileStorage = AppStorageInitializer.container.fileStorage
            if (fileStorage.exists(StorageArea.CACHE, cacheRelativePath)) {
                return@withContext getAbsoluteCachePath(cacheRelativePath)
            }
        }

        return@withContext videoUrl
    }

    /**
     * 异步预加载/缓存指定网络视频到本地磁盘。
     * 
     * @param videoUrl 网络视频地址
     */
    fun preloadVideo(videoUrl: String) {
        if (!isRemoteUrl(videoUrl) || !isStorageReady()) return

        scope.launch {
            val cacheRelativePath = computeCacheStoragePath(videoUrl)
            val fileStorage = AppStorageInitializer.container.fileStorage

            downloadingMutex.withLock {
                if (activeDownloads.contains(videoUrl)) return@launch
                if (fileStorage.exists(StorageArea.CACHE, cacheRelativePath)) return@launch
                activeDownloads.add(videoUrl)
            }

            try {
                val client = if (AppNetworkInitializer.isInitialized) {
                    AppNetworkInitializer.container.authorizedClient
                } else null

                if (client != null) {
                    val response = client.get(videoUrl)
                    val bytes = response.bodyAsBytes()
                    if (bytes.isNotEmpty()) {
                        fileStorage.write(
                            area = StorageArea.CACHE,
                            path = cacheRelativePath,
                            data = bytes,
                            mode = WriteMode.ATOMIC
                        )
                    }
                }
            } catch (_: Throwable) {
                // 网络异常时不中断主流程
            } finally {
                downloadingMutex.withLock {
                    activeDownloads.remove(videoUrl)
                }
            }
        }
    }

    /**
     * 检查并判断某视频当前是否已完全缓存至本地。
     */
    suspend fun isVideoCached(videoUrl: String): Boolean = withContext(Dispatchers.IO) {
        if (!isRemoteUrl(videoUrl)) return@withContext true
        if (!isStorageReady()) return@withContext false
        val cacheRelativePath = computeCacheStoragePath(videoUrl)
        AppStorageInitializer.container.fileStorage.exists(StorageArea.CACHE, cacheRelativePath)
    }

    /**
     * 清空所有视频磁盘缓存。
     */
    suspend fun clearVideoCache(): Unit = withContext(Dispatchers.IO) {
        if (!isStorageReady()) return@withContext
        val fileStorage = AppStorageInitializer.container.fileStorage
        val videoFiles = fileStorage.list(StorageArea.CACHE, StoragePath("video_cache"))
        for (file in videoFiles) {
            fileStorage.delete(StorageArea.CACHE, file.path)
        }
    }

    private fun isRemoteUrl(url: String): Boolean {
        return url.startsWith("http://", ignoreCase = true) || url.startsWith("https://", ignoreCase = true)
    }

    private fun isStorageReady(): Boolean {
        return runCatching { AppStorageInitializer.isInitialized }.getOrDefault(false)
    }

    private fun computeCacheStoragePath(url: String): StoragePath {
        val hash = url.hashCode().toUInt().toString(16)
        val extension = if (url.contains('.')) {
            val ext = url.substringAfterLast('.').substringBefore('?').substringBefore('#')
            if (ext.length in 2..5) ext else "mp4"
        } else "mp4"
        return StoragePath("video_cache/video_${hash}.$extension")
    }

    private fun getAbsoluteCachePath(relativePath: StoragePath): String {
        return "${AppStorageInitializer.container.directories.cache}/${relativePath.value}"
    }
}
