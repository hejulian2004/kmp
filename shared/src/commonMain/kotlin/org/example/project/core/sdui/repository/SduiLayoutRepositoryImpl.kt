/**
 * @File: SduiLayoutRepositoryImpl.kt
 * @Package: org.example.project.core.sdui.repository
 * @Description: SDUI动态布局配置契约与本地缓存/服务端热更新仓库实现
 * @Author: 何聚敛
 * @Date: 2026-08-04
 */
package org.example.project.core.sdui.repository

import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.json.Json
import org.example.project.core.network.client.AppNetworkInitializer
import org.example.project.core.network.client.NetworkContainer
import org.example.project.core.network.config.ApiEndpoints
import org.example.project.core.sdui.model.SduiNode

/**
 * SDUI布局配置数据仓库契约
 */
interface SduiLayoutRepository {
    /**
     * 获取指定模块的 SDUI 节点树。
     * 默认优先读取本地缓存 JSON（服务端热更下载保存的 DSL），若无本地缓存则默认使用原生打包 UI 模板。
     *
     * @param module 模块标识（如 "feedline", "instagram"）
     * @param bundledFallbackJson 本地原生打包内置的默认 UI 模板 JSON
     * @return 解析完成的 SduiNode 根节点
     */
    fun getLayout(module: String, bundledFallbackJson: String): SduiNode

    /**
     * 从网络层检查并下载服务端热更 JSON。
     * 若服务端有热更：下载 JSON 并保存至本地磁盘缓存，后续默认使用该本地缓存 JSON；
     * 若网络失败或无热更：安全降级使用本地缓存 JSON 或原生打包默认 UI 模板。
     *
     * @param module 模块标识（如 "feedline", "instagram"）
     * @param bundledFallbackJson 本地原生打包内置的默认 UI 模板 JSON
     * @return 解析完成的 SduiNode 根节点
     */
    suspend fun fetchLayoutFromNetwork(module: String, bundledFallbackJson: String): SduiNode

    /**
     * 写入/更新本地磁盘 DSL 缓存（保存服务端下载的热更 JSON）
     *
     * @param module 模块标识
     * @param jsonContent 服务端热更 JSON 字符串
     */
    fun saveDiskCache(module: String, jsonContent: String)
}

/**
 * 本地缓存与服务端热更新降级仓库实现
 * 加载优先级：
 * 1. 本地缓存 JSON（若此前已成功下载并保存服务端热更）
 * 2. 原生打包内置 UI 模板（首次安装或无本地热更缓存时默认使用）
 *
 * @param networkContainer 网络依赖容器（若为 null 则自动获取全局 AppNetworkInitializer 单例）
 */
class SduiLayoutRepositoryImpl(
    private val networkContainer: NetworkContainer? = null
) : SduiLayoutRepository {

    companion object {
        /** 全局默认单例 */
        val Instance: SduiLayoutRepository by lazy { SduiLayoutRepositoryImpl() }
    }

    private val memoryCache = mutableMapOf<String, SduiNode>()
    private val diskCacheStore = mutableMapOf<String, String>() // 内存模拟磁盘 Key-Value 存储

    private val jsonFormatter = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val activeNetworkContainer: NetworkContainer
        get() = networkContainer ?: AppNetworkInitializer.container

    override fun getLayout(module: String, bundledFallbackJson: String): SduiNode {
        // 1. 优先查找内存缓存（已解析过的本地热更或模板节点）
        memoryCache[module]?.let { return it }

        // 2. 查找本地磁盘缓存（此前从服务端下载并保存的热更 JSON）
        val diskJson = diskCacheStore[module]
        if (!diskJson.isNullOrBlank()) {
            try {
                val node = jsonFormatter.decodeFromString<SduiNode>(diskJson)
                memoryCache[module] = node
                return node
            } catch (_: Exception) {
                // 磁盘缓存解析失败时穿透到原生打包默认 UI 模板
            }
        }

        // 3. 默认使用原生打包内置 Asset UI 模板
        return parseFallbackNode(module, bundledFallbackJson)
    }

    override suspend fun fetchLayoutFromNetwork(module: String, bundledFallbackJson: String): SduiNode {
        return try {
            val client = activeNetworkContainer.publicClient
            val responseText = client.get("${ApiEndpoints.Sdui.GET_LAYOUT}/$module").body<String>()
            val node = jsonFormatter.decodeFromString<SduiNode>(responseText)
            
            // 服务端有热更，下载成功后保存到本地磁盘与内存缓存，供后续默认读取使用
            diskCacheStore[module] = responseText
            memoryCache[module] = node
            node
        } catch (_: Exception) {
            // 网络请求失败或无热更时，安全降级使用本地缓存（若有）或原生打包默认 UI 模板
            getLayout(module, bundledFallbackJson)
        }
    }

    override fun saveDiskCache(module: String, jsonContent: String) {
        try {
            val node = jsonFormatter.decodeFromString<SduiNode>(jsonContent)
            diskCacheStore[module] = jsonContent
            memoryCache[module] = node
        } catch (_: Exception) {
            // 写入非法 JSON 自动忽略
        }
    }

    private fun parseFallbackNode(module: String, bundledFallbackJson: String): SduiNode {
        return try {
            val fallbackNode = jsonFormatter.decodeFromString<SduiNode>(bundledFallbackJson)
            memoryCache[module] = fallbackNode
            fallbackNode
        } catch (_: Exception) {
            // 若内置 JSON 格式非法，返回极简降级容器
            SduiNode(componentType = "Column")
        }
    }
}
