/**
 * @File: AppMockConfigTest.kt
 * @Package: org.example.project.core.config
 * @Description: AppMockConfig全局假数据配置中心单元测试套件
 * @Author: 何聚敛
 * @Date: 2026-08-18
 */
package org.example.project.core.config

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AppMockConfigTest {

    @BeforeTest
    fun setUp() {
        AppMockConfig.resetToDefaults()
    }

    @AfterTest
    fun tearDown() {
        AppMockConfig.resetToDefaults()
    }

    @Test
    fun testDefaultMockConfigState() {
        assertTrue(AppMockConfig.isMockEnabled, "默认全局Mock开关应处于开启状态")
        assertTrue(AppMockConfig.isFeedLineMockEnabled)
        assertTrue(AppMockConfig.isInstagramMockEnabled)
        assertTrue(AppMockConfig.isAirbnbMockEnabled)
        assertTrue(AppMockConfig.isWeChatMpMockEnabled)

        assertTrue(AppMockConfig.isMockActiveFor(MockModule.FEEDLINE))
        assertTrue(AppMockConfig.isMockActiveFor(MockModule.INSTAGRAM))
        assertTrue(AppMockConfig.isMockActiveFor(MockModule.AIRBNB))
        assertTrue(AppMockConfig.isMockActiveFor(MockModule.WECHAT_MP))
        assertEquals(500L, AppMockConfig.mockNetworkDelayMs)
    }

    @Test
    fun testGlobalMockSwitchDisablesAllModules() {
        AppMockConfig.isMockEnabled = false

        assertFalse(AppMockConfig.isMockActiveFor(MockModule.FEEDLINE))
        assertFalse(AppMockConfig.isMockActiveFor(MockModule.INSTAGRAM))
        assertFalse(AppMockConfig.isMockActiveFor(MockModule.AIRBNB))
        assertFalse(AppMockConfig.isMockActiveFor(MockModule.WECHAT_MP))
    }

    @Test
    fun testPerModuleMockSwitches() {
        AppMockConfig.isMockEnabled = true
        AppMockConfig.isWeChatMpMockEnabled = false

        assertTrue(AppMockConfig.isMockActiveFor(MockModule.FEEDLINE))
        assertTrue(AppMockConfig.isMockActiveFor(MockModule.INSTAGRAM))
        assertTrue(AppMockConfig.isMockActiveFor(MockModule.AIRBNB))
        assertFalse(AppMockConfig.isMockActiveFor(MockModule.WECHAT_MP), "WeChatMp 单独关闭后应返回 false")

        AppMockConfig.isFeedLineMockEnabled = false
        assertFalse(AppMockConfig.isMockActiveFor(MockModule.FEEDLINE))
    }

    @Test
    fun testResetToDefaultsRestoresAll() {
        AppMockConfig.isMockEnabled = false
        AppMockConfig.isAirbnbMockEnabled = false
        AppMockConfig.mockNetworkDelayMs = 1200L

        AppMockConfig.resetToDefaults()

        assertTrue(AppMockConfig.isMockEnabled)
        assertTrue(AppMockConfig.isAirbnbMockEnabled)
        assertEquals(500L, AppMockConfig.mockNetworkDelayMs)
    }
}
