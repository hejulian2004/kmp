/**
 * @File: LogAnalyticsTrackerTest.kt
 * @Package: org.example.project.core.analytics
 * @Description: LogAnalyticsTracker 日志上报追踪器单元测试
 * @Author: 何聚敛
 * @Date: 2026-08-18
 */
package org.example.project.core.analytics

import kotlin.test.Test
import kotlin.test.assertTrue

class LogAnalyticsTrackerTest {

    @Test
    fun testTrackEventWithAndWithoutParamsDoesNotCrash() {
        val tracker = LogAnalyticsTracker()

        // 验证空参数上报无异常
        tracker.trackEvent("test_empty_event", emptyMap())

        // 验证有参数上报无异常
        tracker.trackEvent(
            eventName = "test_params_event",
            params = mapOf("key1" to "val1", "key2" to 100, "key3" to true)
        )

        // 验证 flush 无异常
        tracker.flush()

        assertTrue(true, "LogAnalyticsTracker 应正常执行不抛出任何异常")
    }
}
