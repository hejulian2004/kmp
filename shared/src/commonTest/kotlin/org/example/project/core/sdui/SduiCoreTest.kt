/**
 * @File: SduiCoreTest.kt
 * @Package: org.example.project.core.sdui
 * @Description: SDUI 核心数据模型、Kotlin DSL Builder 与三级降级仓库单元测试
 * @Author: 何聚敛
 * @Date: 2026-08-04
 */
package org.example.project.core.sdui

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
        val bundledJson = """
            {
                "componentType": "Column",
                "properties": { "title": "内置兜底标题" },
                "children": [
                    { "componentType": "Text", "properties": { "text": "兜底文案" } }
                ]
            }
        """.trimIndent()

        // 1. 无内存缓存无磁盘缓存，走内置兜底 JSON
        val fallbackNode = repository.getLayout("feedline", bundledJson)
        assertEquals("Column", fallbackNode.componentType)
        assertEquals("内置兜底标题", fallbackNode.properties["title"])

        // 2. 更新磁盘/动态缓存，验证优先读取最新更新
        val newJson = """
            {
                "componentType": "Card",
                "properties": { "title": "热更最新标题" }
            }
        """.trimIndent()
        repository.saveDiskCache("feedline", newJson)

        val updatedNode = repository.getLayout("feedline", bundledJson)
        assertEquals("Card", updatedNode.componentType)
        assertEquals("热更最新标题", updatedNode.properties["title"])
    }
}
