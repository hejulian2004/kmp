/**
 * @File: SduiCoreTest.kt
 * @Package: org.example.project.core.sdui
 * @Description: SDUI核心数据模型、Kotlin DSL Builder与三级降级仓库单元测试
 * @Author: 何聚敛
 * @Date: 2026-08-05
 */
package org.example.project.core.sdui

import kotlinx.coroutines.test.runTest
import org.example.project.core.sdui.builder.sduiLayout
import org.example.project.core.sdui.builder.toJson
import org.example.project.core.sdui.repository.SduiLayoutRepositoryImpl
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SduiCoreTest {

    @Test
    fun testSduiLayoutBuilderToJson() {
        val node = sduiLayout("Column") {
            prop("padding", "12")
            node("Text") {
                prop("text", "测试动态标题")
            }
            node("Button") {
                prop("text", "确认操作")
                action("onClick", "NAVIGATE", mapOf("target" to "detail"))
            }
        }

        val json = node.toJson()
        assertNotNull(json)
        assertTrue(json.contains("Column"))
        assertTrue(json.contains("测试动态标题"))
        assertTrue(json.contains("NAVIGATE"))
    }

    @Test
    fun testSduiLayoutRepositoryThreeLevelFallback() {
        val repository = SduiLayoutRepositoryImpl()
        repository.clearDiskCache("feedline")
        val bundledJson = """
            {
                "componentType": "Column",
                "properties": { "title": "原生打包默认标题" },
                "children": [
                    { "componentType": "Text", "properties": { "text": "原生打包默认文案" } }
                ]
            }
        """.trimIndent()

        // 1. 无本地缓存时，默认使用原生打包内置UI模板
        val nativeNode = repository.getLayout("feedline", bundledJson)
        assertEquals("Column", nativeNode.componentType)
        assertEquals("原生打包默认标题", nativeNode.properties["title"])

        // 2. 模拟从服务端成功下载并保存热更JSON至本地磁盘
        val hotUpdateServerJson = """
            {
                "componentType": "Card",
                "properties": { "title": "服务端热更下发标题" }
            }
        """.trimIndent()
        repository.saveDiskCache("feedline", hotUpdateServerJson)

        // 3. 验证后续默认使用本地已保存的热更JSON
        val cachedNode = repository.getLayout("feedline", bundledJson)
        assertEquals("Card", cachedNode.componentType)
        assertEquals("服务端热更下发标题", cachedNode.properties["title"])
    }

    @Test
    fun testSduiLayoutRepositoryNetworkFailureFallback() = runTest {
        val repository = SduiLayoutRepositoryImpl()
        repository.clearDiskCache("feedline")
        val bundledJson = """
            {
                "componentType": "Column",
                "properties": { "title": "原生打包默认标题" }
            }
        """.trimIndent()

        // 1. 首次检查网络（默认网络失败且无本地缓存） -> 自动使用原生打包默认UI模板
        val nodeFirstTime = repository.fetchLayoutFromNetwork("feedline", bundledJson)
        assertEquals("Column", nodeFirstTime.componentType)
        assertEquals("原生打包默认标题", nodeFirstTime.properties["title"])

        // 2. 保存服务端热更JSON到本地
        val serverHotUpdateJson = """
            {
                "componentType": "Banner",
                "properties": { "title": "已保存的本地热更标题" }
            }
        """.trimIndent()
        repository.saveDiskCache("feedline", serverHotUpdateJson)

        // 3. 再次请求网络失败 -> 自动降级并优先使用上一步已保存的本地热更JSON
        val nodeSecondTime = repository.fetchLayoutFromNetwork("feedline", bundledJson)
        assertEquals("Banner", nodeSecondTime.componentType)
        assertEquals("已保存的本地热更标题", nodeSecondTime.properties["title"])
    }
}
