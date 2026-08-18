/**
 * @File: AndroidVideoCache.kt
 * @Package: org.example.project.ui.core.video
 * @Description: Android平台Media3视频播放缓存单例管理器（集成SimpleCache与512MB LRU磁盘淘汰策略）
 * @Author: 何聚敛
 * @Date: 2026-08-18
 */
package org.example.project.ui.core.video

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import java.io.File

/**
 * Android 平台视频缓存单例。
 * 
 * 职责：
 * 1. 持有全应用唯一的 Media3 SimpleCache 实例。
 * 2. 配置 512MB 物理磁盘最大配额与 LeastRecentlyUsedCacheEvictor 淘汰策略。
 * 3. 构造统一的 CacheDataSource.Factory 供 ExoPlayer 流式读取与自动缓存。
 */
@OptIn(UnstableApi::class)
object AndroidVideoCache {

    /**
     * 视频缓存最大容量 (512MB)
     */
    const val MAX_CACHE_SIZE_BYTES: Long = 512L * 1024 * 1024

    private const val CACHE_DIR_NAME = "video_cache"

    @Volatile
    private var simpleCacheInstance: SimpleCache? = null

    @Volatile
    private var databaseProviderInstance: StandaloneDatabaseProvider? = null

    private val lock = Any()

    /**
     * 获取全应用唯一的 SimpleCache 实例。
     */
    fun getCache(context: Context): SimpleCache {
        return simpleCacheInstance ?: synchronized(lock) {
            simpleCacheInstance ?: run {
                val appContext = context.applicationContext
                val cacheDir = File(appContext.cacheDir, CACHE_DIR_NAME)
                if (!cacheDir.exists()) {
                    cacheDir.mkdirs()
                }

                val dbProvider = databaseProviderInstance ?: StandaloneDatabaseProvider(appContext).also {
                    databaseProviderInstance = it
                }

                val evictor = LeastRecentlyUsedCacheEvictor(MAX_CACHE_SIZE_BYTES)
                SimpleCache(cacheDir, evictor, dbProvider).also {
                    simpleCacheInstance = it
                }
            }
        }
    }

    /**
     * 构建供 ExoPlayer 使用的 CacheDataSource.Factory。
     */
    fun createCacheDataSourceFactory(context: Context): CacheDataSource.Factory {
        val cache = getCache(context)
        val upstreamFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(15000)
            .setReadTimeoutMs(15000)

        return CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(upstreamFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    }

    /**
     * 清理所有已缓存的视频。
     */
    fun clearCache(context: Context) {
        synchronized(lock) {
            val cache = simpleCacheInstance
            if (cache != null) {
                val keys = cache.keys
                for (key in keys) {
                    cache.removeResource(key)
                }
            }
        }
    }
}
