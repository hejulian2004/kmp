/**
 * @File: AppAnalyticsManagerTest.kt
 * @Package: org.example.project.core.analytics
 * @Description: KMP全局埋点单例管理器 (AppAnalyticsManager) 单元测试套件
 * @Author: 何聚敛
 * @Date: 2026-08-18
 */
package org.example.project.core.analytics

import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AppAnalyticsManagerTest {

    private class FakeAnalyticsTracker : AnalyticsTracker {
        val events = mutableListOf<Pair<String, Map<String, Any>>>()
        var flushCalledCount = 0
        var shouldThrow = false

        override fun trackEvent(eventName: String, params: Map<String, Any>) {
            if (shouldThrow) {
                throw RuntimeException("Simulated tracker exception")
            }
            events.add(eventName to params)
        }

        override fun flush() {
            flushCalledCount++
        }
    }

    @BeforeTest
    fun setUp() {
        AppAnalyticsManager.resetForTest()
    }

    @AfterTest
    fun tearDown() {
        AppAnalyticsManager.resetForTest()
    }

    @Test
    fun testUninitializedAccessThrowsIllegalStateException() {
        assertFailsWith<IllegalStateException> {
            AppAnalyticsManager.trackEvent("test_event", emptyMap())
        }

        assertFailsWith<IllegalStateException> {
            AppAnalyticsManager.trackScreenView("HomeScreen")
        }

        assertFailsWith<IllegalStateException> {
            AppAnalyticsManager.setUserContext("user_123")
        }

        assertFailsWith<IllegalStateException> {
            AppAnalyticsManager.flush()
        }
    }

    @Test
    fun testInitTwiceThrowsIllegalStateException() {
        val config = AnalyticsConfig(
            platformName = "Android",
            appVersion = "1.0.0",
            deviceId = "device_test_001"
        )

        AppAnalyticsManager.init(config)

        assertFailsWith<IllegalStateException> {
            AppAnalyticsManager.init(config)
        }
    }

    private fun waitUntilTrackerEvent(tracker: FakeAnalyticsTracker, timeoutMs: Long = 1000L) {
        val start = kotlin.time.TimeSource.Monotonic.markNow()
        while (tracker.events.isEmpty() && start.elapsedNow().inWholeMilliseconds < timeoutMs) {
            // 短暂停顿轮询事件
            runCatching { Thread.sleep(5) }
        }
    }

    @Test
    fun testTrackEventWithGlobalParams() = runTest {
        val fakeTracker = FakeAnalyticsTracker()
        val config = AnalyticsConfig(
            platformName = "iOS",
            appVersion = "2.5.0",
            deviceId = "ios_dev_999",
            trackers = listOf(fakeTracker)
        )

        AppAnalyticsManager.init(config)
        AppAnalyticsManager.setUserContext(userId = "user_888", userRole = "vip")

        AppAnalyticsManager.trackEvent("button_click", mapOf("btn_id" to "submit_post"))
        waitUntilTrackerEvent(fakeTracker)

        assertEquals(1, fakeTracker.events.size)
        val (eventName, params) = fakeTracker.events.first()
        assertEquals("button_click", eventName)
        assertEquals("iOS", params["platform"])
        assertEquals("2.5.0", params["app_version"])
        assertEquals("ios_dev_999", params["device_id"])
        assertEquals("user_888", params[AnalyticsParams.USER_ID])
        assertEquals("vip", params["user_role"])
        assertEquals("submit_post", params["btn_id"])
        assertNotNull(params["timestamp"])
    }

    @Test
    fun testTrackScreenView() = runTest {
        val fakeTracker = FakeAnalyticsTracker()
        val config = AnalyticsConfig(
            platformName = "Desktop",
            appVersion = "1.0.0",
            deviceId = "mac_001",
            trackers = listOf(fakeTracker)
        )

        AppAnalyticsManager.init(config)
        AppAnalyticsManager.trackScreenView("FeedLineScreen", mapOf("tab" to "friends"))
        waitUntilTrackerEvent(fakeTracker)

        assertEquals(1, fakeTracker.events.size)
        val (eventName, params) = fakeTracker.events.first()
        assertEquals(AnalyticsEvents.ENTER_SCREEN, eventName)
        assertEquals("FeedLineScreen", params[AnalyticsParams.SCREEN_NAME])
        assertEquals("friends", params["tab"])
    }

    @Test
    fun testClearUserContext() = runTest {
        val fakeTracker = FakeAnalyticsTracker()
        val config = AnalyticsConfig(
            platformName = "Android",
            appVersion = "1.0.0",
            deviceId = "dev_1",
            trackers = listOf(fakeTracker)
        )

        AppAnalyticsManager.init(config)
        AppAnalyticsManager.setUserContext("user_100", "admin")
        AppAnalyticsManager.setUserContext(null) // 清除上下文

        AppAnalyticsManager.trackEvent("logout_event")
        waitUntilTrackerEvent(fakeTracker)

        val (_, params) = fakeTracker.events.first()
        assertTrue(!params.containsKey(AnalyticsParams.USER_ID))
        assertTrue(!params.containsKey("user_role"))
    }

    @Test
    fun testTrackerExceptionIsolation() = runTest {
        val failingTracker = FakeAnalyticsTracker().apply { shouldThrow = true }
        val successTracker = FakeAnalyticsTracker()

        val config = AnalyticsConfig(
            platformName = "Android",
            appVersion = "1.0.0",
            deviceId = "dev_2",
            trackers = listOf(failingTracker, successTracker)
        )

        AppAnalyticsManager.init(config)
        AppAnalyticsManager.trackEvent("safe_event")
        waitUntilTrackerEvent(successTracker)

        // failingTracker 抛出异常不应卡死主流程，successTracker 仍然能接收到事件
        assertEquals(1, successTracker.events.size)
        assertEquals("safe_event", successTracker.events.first().first)
    }

    @Test
    fun testFlushCallsAllTrackers() {
        val tracker1 = FakeAnalyticsTracker()
        val tracker2 = FakeAnalyticsTracker()

        val config = AnalyticsConfig(
            platformName = "Android",
            appVersion = "1.0.0",
            deviceId = "dev_3",
            trackers = listOf(tracker1, tracker2)
        )

        AppAnalyticsManager.init(config)
        AppAnalyticsManager.flush()

        assertEquals(1, tracker1.flushCalledCount)
        assertEquals(1, tracker2.flushCalledCount)
    }
}
