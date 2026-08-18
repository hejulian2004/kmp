/**
 * @File: SduiCoreTest.kt
 * @Package: org.example.project.core.sdui
 * @Description: SDUI核心数据模型、Kotlin DSL Builder与热更新降级仓库单元测试
 * @Author: 何聚敛
 * @Date: 2026-08-11
 */
package org.example.project.core.sdui

import kotlinx.coroutines.test.runTest
import org.example.project.core.sdui.builder.sduiLayout
import org.example.project.core.sdui.builder.toJson
import org.example.project.core.sdui.repository.SduiLayoutRepositoryImpl
import org.example.project.core.analytics.AnalyticsConfig
import org.example.project.core.analytics.AppAnalyticsManager
import org.example.project.core.analytics.LogAnalyticsTracker
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SduiCoreTest {

    @BeforeTest
    fun setUp() {
        if (!AppAnalyticsManager.isInitialized) {
            AppAnalyticsManager.init(
                AnalyticsConfig(
                    platformName = "Test",
                    appVersion = "1.0.0",
                    deviceId = "test_device",
                    trackers = listOf(LogAnalyticsTracker())
                )
            )
        }
    }

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
    fun testSduiLayoutRepositoryNativeFallbackWhenNoHotUpdate() {
        val repository = SduiLayoutRepositoryImpl()
        repository.clearDiskCache("feedline")

        // 1. 无本地热更JSON时，默认返回 null（UI层自动降级使用全量原生 Compose UI）
        val nativeNode = repository.getLayout("feedline")
        assertNull(nativeNode)

        // 2. 模拟从服务端成功下载并保存热更JSON至本地磁盘
        val hotUpdateServerJson = """
            {
                "componentType": "Card",
                "properties": { "title": "服务端热更下发标题" }
            }
        """.trimIndent()
        repository.saveDiskCache("feedline", hotUpdateServerJson)

        // 3. 验证后续默认使用本地已保存的热更JSON
        val cachedNode = repository.getLayout("feedline")
        assertNotNull(cachedNode)
        assertEquals("Card", cachedNode.componentType)
        assertEquals("服务端热更下发标题", cachedNode.properties["title"])
    }

    @Test
    fun testSduiLayoutRepositoryNetworkFailureFallback() = runTest {
        val repository = SduiLayoutRepositoryImpl()
        repository.clearDiskCache("feedline")

        // 1. 首次检查网络（默认网络失败且无本地热更缓存） -> 返回 null
        val nodeFirstTime = repository.fetchLayoutFromNetwork("feedline")
        assertNull(nodeFirstTime)

        // 2. 保存服务端热更JSON到本地
        val serverHotUpdateJson = """
            {
                "componentType": "Banner",
                "properties": { "title": "已保存的本地热更标题" }
            }
        """.trimIndent()
        repository.saveDiskCache("feedline", serverHotUpdateJson)

        // 3. 再次请求网络失败 -> 自动降级并优先使用上一步已保存的本地热更JSON
        val nodeSecondTime = repository.fetchLayoutFromNetwork("feedline")
        assertNotNull(nodeSecondTime)
        assertEquals("Banner", nodeSecondTime.componentType)
        assertEquals("已保存的本地热更标题", nodeSecondTime.properties["title"])
    }

    @Test
    fun testAirbnbSduiLayoutNativeFallback() = runTest {
        val repository = SduiLayoutRepositoryImpl()
        repository.clearDiskCache("airbnb")

        // 无热更JSON时直接返回 null，由 Airbnb 主页面直接绘制全量 Compose 原生UI
        val node = repository.getLayout("airbnb")
        assertNull(node)
    }
}
