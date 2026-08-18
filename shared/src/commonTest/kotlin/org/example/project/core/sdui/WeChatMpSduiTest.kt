/**
 * @File: WeChatMpSduiTest.kt
 * @Package: org.example.project.core.sdui
 * @Description: 微信公众号SDUI版本常量、布局DSL与三级容灾缓存单元测试
 * @Author: 何聚敛
 * @Date: 2026-08-18
 */
package org.example.project.core.sdui

import kotlinx.coroutines.test.runTest
import org.example.project.core.sdui.builder.sduiLayout
import org.example.project.core.sdui.builder.toJson
import org.example.project.core.sdui.config.SduiVersionConfig
import org.example.project.core.sdui.repository.SduiLayoutRepositoryImpl
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WeChatMpSduiTest {

    @Test
    fun testSduiVersionConfigConstants() {
        assertEquals("v1.0.0", SduiVersionConfig.MODULE_WECHAT_MP_VERSION)
        assertEquals("v1.0.0", SduiVersionConfig.WECHAT_MP_TOPBAR_VERSION)
        assertEquals("v1.0.0", SduiVersionConfig.WECHAT_MP_FREQUENTLY_READ_VERSION)
        assertEquals("v1.0.0", SduiVersionConfig.WECHAT_MP_FEATURED_BANNER_VERSION)
        assertEquals("v1.0.0", SduiVersionConfig.WECHAT_MP_HORIZONTAL_CARD_VERSION)
        assertEquals("v1.0.0", SduiVersionConfig.WECHAT_MP_WATERFALL_CARD_VERSION)
        assertEquals("v1.0.0", SduiVersionConfig.WECHAT_MP_DISLIKE_SHEET_VERSION)
    }

    @Test
    fun testWeChatMpSduiLayoutBuilder() {
        val node = sduiLayout("LazyVerticalStaggeredGrid") {
            prop("columns", "2")
            node("WeChatMpTopBar") {
                prop("title", "公众号")
                action("onSearchClick", "NAVIGATE", mapOf("target" to "search"))
            }
            node("WeChatMpFrequentlyReadBar") {
                prop("title", "常读")
            }
            node("WeChatMpFeaturedBannerCard") {
                prop("version", "v1.0.0")
            }
        }

        val json = node.toJson()
        assertNotNull(json)
        assertTrue(json.contains("LazyVerticalStaggeredGrid"))
        assertTrue(json.contains("WeChatMpTopBar"))
        assertTrue(json.contains("WeChatMpFrequentlyReadBar"))
        assertTrue(json.contains("WeChatMpFeaturedBannerCard"))
        assertTrue(json.contains("NAVIGATE"))
    }

    @Test
    fun testWeChatMpSduiNativeFallbackWhenNoHotUpdate() {
        val repository = SduiLayoutRepositoryImpl()
        repository.clearDiskCache("wechat_mp")

        // 1. 无本地热更时，默认返回 null（回退至原生 Compose UI）
        val nativeLayout = repository.getLayout("wechat_mp")
        assertNull(nativeLayout)

        // 2. 保存模拟的热更 JSON
        val hotUpdateJson = """
            {
                "componentType": "LazyVerticalStaggeredGrid",
                "properties": { "version": "v1.1.0" },
                "children": [
                    { "componentType": "WeChatMpTopBar", "properties": { "title": "热更公众号" } }
                ]
            }
        """.trimIndent()
        repository.saveDiskCache("wechat_mp", hotUpdateJson)

        // 3. 读取本地已保存的热更布局
        val cachedLayout = repository.getLayout("wechat_mp")
        assertNotNull(cachedLayout)
        assertEquals("LazyVerticalStaggeredGrid", cachedLayout.componentType)
        assertEquals("v1.1.0", cachedLayout.properties["version"])
    }

    @Test
    fun testWeChatMpSduiNetworkFetchFallback() = runTest {
        val repository = SduiLayoutRepositoryImpl()
        repository.clearDiskCache("wechat_mp")

        // 首次无网络无缓存 -> 返回 null
        val layout = repository.fetchLayoutFromNetwork("wechat_mp")
        assertNull(layout)
    }
}
