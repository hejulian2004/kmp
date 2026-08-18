/**
 * @File: AnalyticsConstantsTest.kt
 * @Package: org.example.project.core.analytics
 * @Description: 数据埋点常量值与事件名映射单元测试
 * @Author: 何聚敛
 * @Date: 2026-08-18
 */
package org.example.project.core.analytics

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class AnalyticsConstantsTest {

    @Test
    fun testModuleIdsConsistency() {
        assertEquals("feedline", AnalyticsModules.FEEDLINE)
        assertEquals("instagram", AnalyticsModules.INSTAGRAM)
        assertEquals("airbnb", AnalyticsModules.AIRBNB)
    }

    @Test
    fun testEventNameMappings() {
        assertEquals("feed_open", AnalyticsEvents.FEED_OPEN)
        assertEquals("post_create", AnalyticsEvents.POST_CREATE)
        assertEquals("insta_home_open", AnalyticsEvents.INSTA_HOME_OPEN)
        assertEquals("airbnb_search", AnalyticsEvents.AIRBNB_SEARCH)
        assertEquals("enter_screen", AnalyticsEvents.ENTER_SCREEN)
        assertEquals("init_sub_error", AnalyticsEvents.INIT_SUB_ERROR)
    }

    @Test
    fun testParamKeysConsistency() {
        assertEquals("screen_name", AnalyticsParams.SCREEN_NAME)
        assertEquals("user_id", AnalyticsParams.USER_ID)
        assertEquals("is_success", AnalyticsParams.IS_SUCCESS)
        assertEquals("error_msg", AnalyticsParams.ERROR_MSG)
    }
}
