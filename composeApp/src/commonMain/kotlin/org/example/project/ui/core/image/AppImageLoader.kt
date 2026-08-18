/**
 * @File: AppImageLoader.kt
 * @Package: org.example.project.ui.core.image
 * @Description: 全局统一图片加载器与多级缓存配置（集成LRU内存缓存、256MB物理磁盘持久缓存与Ktor网络引擎）
 * @Author: 何聚敛
 * @Date: 2026-08-18
 */
package org.example.project.ui.core.image

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.memory.MemoryCache
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade
import io.github.vinceglb.filekit.coil.addPlatformFileSupport
import okio.Path.Companion.toPath
import org.example.project.core.storage.client.AppStorageInitializer

/**
 * 构建全应用统一的 Coil 3 ImageLoader 实例。
 * 
 * 包含：
 * 1. 25% 动态 LRU 内存缓存 (MemoryCache)。
 * 2. 256MB 持久化磁盘文件缓存池 (DiskCache)。
 * 3. Ktor 3 跨平台网络图片引擎。
 * 4. FileKit 跨平台本地文件系统支持。
 * 5. 优雅渐变淡入动画 (Crossfade)。
 */
fun createAppImageLoader(context: PlatformContext): ImageLoader {
    val diskCacheDirectory = runCatching {
        if (AppStorageInitializer.isInitialized) {
            "${AppStorageInitializer.container.directories.cache}/image_cache".toPath()
        } else {
            null
        }
    }.getOrNull()

    return ImageLoader.Builder(context)
        .memoryCache {
            MemoryCache.Builder()
                .maxSizePercent(context, percent = 0.25)
                .build()
        }
        .apply {
            if (diskCacheDirectory != null) {
                diskCache {
                    DiskCache.Builder()
                        .directory(diskCacheDirectory)
                        .maxSizeBytes(256L * 1024 * 1024) // 256MB 磁盘缓存
                        .build()
                }
            }
        }
        .components {
            add(KtorNetworkFetcherFactory())
            addPlatformFileSupport()
        }
        .crossfade(true)
        .build()
}
