/**
 * @File: SduiLayoutRepositoryImpl.kt
 * @Package: org.example.project.core.sdui.repository
 * @Description: SDUI动态布局配置契约与本地磁盘缓存/服务端热更新仓库实现 (基于 FileStorage 与非阻塞协程并发防护)
 * @Author: 何聚敛
 * @Date: 2026-08-18
 */
package org.example.project.core.sdui.repository

import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import org.example.project.core.network.client.AppNetworkInitializer
import org.example.project.core.network.client.NetworkContainer
import org.example.project.core.network.config.ApiEndpoints
import org.example.project.core.sdui.model.SduiNode
import org.example.project.core.storage.api.FileStorage
import org.example.project.core.storage.api.StorageArea
import org.example.project.core.storage.api.StoragePath
import org.example.project.core.storage.api.WriteMode
import org.example.project.core.storage.client.AppStorageInitializer

/**
 * SDUI布局配置数据仓库契约
 */
interface SduiLayoutRepository {
    /**
     * 同步获取内存中已缓存的 SDUI 节点树（零 I/O，只读内存）。
     *
     * @param module 模块标识（如 "airbnb", "feedline", "instagram", "wechat_mp"）
     * @return 解析完成的 SduiNode 根节点，或无内存缓存时返回 null
     */
    fun getCachedLayout(module: String): SduiNode?

    /**
     * 异步从本地磁盘加载热更 JSON 节点树并写入内存缓存。
     *
     * @param module 模块标识
     * @return 解析完成的 SduiNode 根节点，或无磁盘缓存时返回 null
     */
    suspend fun loadDiskCache(module: String): SduiNode?

    /**
     * 异步从网络层检查并下载服务端热更 JSON。
     * 若服务端有热更：下载 JSON 并持久化保存至本地磁盘与内存缓存；
     * 若网络失败或无热更：安全降级读取本地磁盘已有的热更缓存（若有），若本地亦无热更则返回 null。
     *
     * @param module 模块标识
     * @return 解析完成的 SduiNode 根节点，或无热更时返回 null
     */
    suspend fun fetchLayoutFromNetwork(module: String): SduiNode?

    /**
     * 异步写入/更新本地磁盘 DSL 缓存（保存服务端下载的热更 JSON）并刷新内存缓存。
     *
     * @param module 模块标识
     * @param jsonContent 服务端热更 JSON 字符串
     */
    suspend fun saveDiskCache(module: String, jsonContent: String)

    /**
     * 异步清空指定模块的本地磁盘缓存与内存缓存。
     */
    suspend fun clearDiskCache(module: String)

    /**
     * 同步清空所有模块的内存缓存（供测试与重置使用）。
     */
    fun clearMemoryCache()
}

/**
 * 本地缓存与服务端热更新降级仓库实现
 *
 * @param fileStorage 文件存储实例（若为 null 则使用全局 AppStorageInitializer 单例）
 * @param networkContainer 网络依赖容器（若为 null 则使用全局 AppNetworkInitializer 单例）
 */
class SduiLayoutRepositoryImpl(
    private val fileStorage: FileStorage? = null,
    private val networkContainer: NetworkContainer? = null
) : SduiLayoutRepository {

    companion object {
        /** 全局默认单例 */
        val Instance: SduiLayoutRepository by lazy { SduiLayoutRepositoryImpl() }
    }

    private val cacheMutex = Mutex()
    private var memoryCache: Map<String, SduiNode> = emptyMap()

    private val jsonFormatter = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val activeFileStorage: FileStorage
        get() = fileStorage ?: AppStorageInitializer.container.fileStorage

    private val activeNetworkContainer: NetworkContainer
        get() = networkContainer ?: AppNetworkInitializer.container

    private fun getSduiPath(module: String) = StoragePath("sdui/$module/layout.json")

    override fun getCachedLayout(module: String): SduiNode? {
        return memoryCache[module]
    }

    override suspend fun loadDiskCache(module: String): SduiNode? {
        val path = getSduiPath(module)
        val diskJson = runCatching {
            if (activeFileStorage.exists(StorageArea.PERSISTENT, path)) {
                activeFileStorage.read(StorageArea.PERSISTENT, path).decodeToString()
            } else null
        }.getOrNull()

        if (!diskJson.isNullOrBlank()) {
            try {
                val node = jsonFormatter.decodeFromString<SduiNode>(diskJson)
                cacheMutex.withLock {
                    memoryCache = memoryCache + (module to node)
                }
                return node
            } catch (_: Exception) {
                // 磁盘缓存损坏时忽略
            }
        }

        return null
    }

    override suspend fun fetchLayoutFromNetwork(module: String): SduiNode? {
        return try {
            val client = activeNetworkContainer.publicClient
            val responseText = client.get("${ApiEndpoints.Sdui.GET_LAYOUT}/$module").body<String>()
            if (responseText.isBlank()) {
                return loadDiskCache(module)
            }
            val node = jsonFormatter.decodeFromString<SduiNode>(responseText)

            // 服务端热更下载成功后异步持久化至本地磁盘与内存
            saveDiskCache(module, responseText)
            node
        } catch (_: Exception) {
            // 网络异常时安全降级至本地磁盘缓存
            loadDiskCache(module)
        }
    }

    override suspend fun saveDiskCache(module: String, jsonContent: String) {
        try {
            val node = jsonFormatter.decodeFromString<SduiNode>(jsonContent)
            cacheMutex.withLock {
                memoryCache = memoryCache + (module to node)
            }
            val path = getSduiPath(module)
            runCatching {
                activeFileStorage.write(
                    area = StorageArea.PERSISTENT,
                    path = path,
                    data = jsonContent.encodeToByteArray(),
                    mode = WriteMode.ATOMIC
                )
            }
        } catch (_: Exception) {
            // 非法 JSON 格式忽略
        }
    }

    override suspend fun clearDiskCache(module: String) {
        cacheMutex.withLock {
            memoryCache = memoryCache - module
        }
        val path = getSduiPath(module)
        runCatching {
            activeFileStorage.delete(StorageArea.PERSISTENT, path)
        }
    }

    override fun clearMemoryCache() {
        memoryCache = emptyMap()
    }
}
