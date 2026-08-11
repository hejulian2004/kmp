/**
 * @File: SduiLayoutRepositoryImpl.kt
 * @Package: org.example.project.core.sdui.repository
 * @Description: SDUI动态布局配置契约与本地磁盘缓存/服务端热更新仓库实现 (使用 FileStorage 存储架构)
 * @Author: 何聚敛
 * @Date: 2026-08-12
 */
package org.example.project.core.sdui.repository

import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.runBlocking
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
     * 获取指定模块的SDUI热更节点树。
     * 若存在已下载的服务端热更JSON（内存/本地磁盘），返回解析完成的SduiNode；
     * 若无热更JSON，返回null（UI层接收到null时直接使用APK打包的原生Compose UI）。
     *
     * @param module 模块标识（如 "airbnb", "feedline", "instagram"）
     * @return 解析完成的SduiNode根节点，或无热更时返回null
     */
    fun getLayout(module: String): SduiNode?

    /**
     * 从网络层检查并下载服务端热更JSON（异步非阻塞执行）。
     * 若服务端有热更：下载JSON并保存至本地磁盘与内存缓存；
     * 若网络失败或无热更：安全降级使用本地磁盘已有的热更缓存（若有），若本地亦无热更则返回null。
     *
     * @param module 模块标识（如 "airbnb", "feedline", "instagram"）
     * @return 解析完成的SduiNode根节点，或无热更时返回null
     */
    suspend fun fetchLayoutFromNetwork(module: String): SduiNode?

    /**
     * 写入/更新本地磁盘DSL缓存（保存服务端下载的热更JSON）
     *
     * @param module 模块标识
     * @param jsonContent 服务端热更JSON字符串
     */
    fun saveDiskCache(module: String, jsonContent: String)

    /**
     * 清空指定模块的本地磁盘缓存
     */
    fun clearDiskCache(module: String)
}

/**
 * 本地缓存与服务端热更新降级仓库实现
 * 加载逻辑：
 * 1. 优先查找内存缓存/本地磁盘热更JSON（若此前已成功下载并保存服务端热更）
 * 2. 若无热更JSON，返回null，供UI层直接渲染APK打包的原生Compose UI
 *
 * @param fileStorage 文件存储实例（若为null则自动获取全局AppStorageInitializer单例）
 * @param networkContainer 网络依赖容器（若为null则自动获取全局AppNetworkInitializer单例）
 */
class SduiLayoutRepositoryImpl(
    private val fileStorage: FileStorage? = null,
    private val networkContainer: NetworkContainer? = null
) : SduiLayoutRepository {

    companion object {
        /** 全局默认单例 */
        val Instance: SduiLayoutRepository by lazy { SduiLayoutRepositoryImpl() }
    }

    private val memoryCache = mutableMapOf<String, SduiNode>()

    private val jsonFormatter = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val activeFileStorage: FileStorage
        get() = fileStorage ?: AppStorageInitializer.container.fileStorage

    private val activeNetworkContainer: NetworkContainer
        get() = networkContainer ?: AppNetworkInitializer.container

    private fun getSduiPath(module: String) = StoragePath("sdui/$module/layout.json")

    override fun getLayout(module: String): SduiNode? {
        // 1. 优先查找内存缓存
        memoryCache[module]?.let { return it }

        // 2. 查找本地磁盘持久化缓存（此前从服务端下载并保存的热更JSON）
        val path = getSduiPath(module)
        val diskJson = runCatching {
            runBlocking(Dispatchers.IO) {
                if (activeFileStorage.exists(StorageArea.PERSISTENT, path)) {
                    activeFileStorage.read(StorageArea.PERSISTENT, path).decodeToString()
                } else null
            }
        }.getOrNull()

        if (!diskJson.isNullOrBlank()) {
            try {
                val node = jsonFormatter.decodeFromString<SduiNode>(diskJson)
                memoryCache[module] = node
                return node
            } catch (_: Exception) {
                // 磁盘缓存解析失败忽略
            }
        }

        // 3. 无热更JSON时返回null，直接使用APK打包的原生Compose UI
        return null
    }

    override suspend fun fetchLayoutFromNetwork(module: String): SduiNode? {
        return try {
            val client = activeNetworkContainer.publicClient
            val responseText = client.get("${ApiEndpoints.Sdui.GET_LAYOUT}/$module").body<String>()
            if (responseText.isBlank()) {
                return getLayout(module)
            }
            val node = jsonFormatter.decodeFromString<SduiNode>(responseText)
            
            // 服务端有热更，下载成功后保存到本地磁盘与内存缓存
            saveDiskCache(module, responseText)
            node
        } catch (_: Exception) {
            // 网络请求失败或无热更时，安全降级使用本地磁盘已有的热更缓存（若有），若无则返回null
            getLayout(module)
        }
    }

    override fun saveDiskCache(module: String, jsonContent: String) {
        try {
            val node = jsonFormatter.decodeFromString<SduiNode>(jsonContent)
            memoryCache[module] = node
            val path = getSduiPath(module)
            runCatching {
                runBlocking(Dispatchers.IO) {
                    activeFileStorage.write(
                        area = StorageArea.PERSISTENT,
                        path = path,
                        data = jsonContent.encodeToByteArray(),
                        mode = WriteMode.ATOMIC
                    )
                }
            }
        } catch (_: Exception) {
            // 写入非法JSON自动忽略
        }
    }

    override fun clearDiskCache(module: String) {
        memoryCache.remove(module)
        val path = getSduiPath(module)
        runCatching {
            runBlocking(Dispatchers.IO) {
                activeFileStorage.delete(StorageArea.PERSISTENT, path)
            }
        }
    }
}
