/**
 * @File: SduiLayoutRepositoryImpl.kt
 * @Package: org.example.project.core.sdui.repository
 * @Description: SDUI动态布局配置契约与三级降级缓存仓库实现
 * @Author: 何聚敛
 * @Date: 2026-08-04
 */
package org.example.project.core.sdui.repository

import kotlinx.serialization.json.Json
import org.example.project.core.sdui.model.SduiNode

/**
 * SDUI布局配置数据仓库契约
 */
interface SduiLayoutRepository {
    /**
     * 获取指定模块的 SDUI 节点树（具备三级降级容灾机制）
     *
     * @param module 模块标识（如 "feedline", "instagram"）
     * @param bundledFallbackJson 本地内置的兜底 JSON 字符串
     * @return 解析完成的 SduiNode 根节点
     */
    fun getLayout(module: String, bundledFallbackJson: String): SduiNode

    /**
     * 写入/更新本地磁盘 DSL 缓存
     *
     * @param module 模块标识
     * @param jsonContent JSON 字符串
     */
    fun saveDiskCache(module: String, jsonContent: String)
}

/**
 * 三级降级容灾仓库实现
 * 缓存顺序：内存缓存 -> 磁盘缓存 -> 内置 Asset 兜底
 */
class SduiLayoutRepositoryImpl : SduiLayoutRepository {
    private val memoryCache = mutableMapOf<String, SduiNode>()
    private val diskCacheStore = mutableMapOf<String, String>() // 内存模拟磁盘 Key-Value 存储

    private val jsonFormatter = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    override fun getLayout(module: String, bundledFallbackJson: String): SduiNode {
        // 1. 优先查内存缓存
        memoryCache[module]?.let { return it }

        // 2. 查找磁盘缓存
        val diskJson = diskCacheStore[module]
        if (!diskJson.isNullOrBlank()) {
            try {
                val node = jsonFormatter.decodeFromString<SduiNode>(diskJson)
                memoryCache[module] = node
                return node
            } catch (_: Exception) {
                // 磁盘缓存解析失败时自动穿透到内置兜底
            }
        }

        // 3. 使用内置 Asset DSL 进行终极兜底
        return try {
            val fallbackNode = jsonFormatter.decodeFromString<SduiNode>(bundledFallbackJson)
            memoryCache[module] = fallbackNode
            fallbackNode
        } catch (e: Exception) {
            // 若内置 JSON 格式非法，返回极简降级容器
            SduiNode(componentType = "Column")
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
}
