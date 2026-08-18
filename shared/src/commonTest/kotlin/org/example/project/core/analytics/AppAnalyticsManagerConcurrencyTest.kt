/**
 * @File: AppAnalyticsManagerConcurrencyTest.kt
 * @Package: org.example.project.core.analytics
 * @Description: AppAnalyticsManager多协程并发安全与快照隔离单元测试
 * @Author: 何聚敛
 * @Date: 2026-08-18
 */
package org.example.project.core.analytics

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class AppAnalyticsManagerConcurrencyTest {

    private class RecordingTracker : AnalyticsTracker {
        val events = mutableListOf<Pair<String, Map<String, Any>>>()
        val flushCount = mutableListOf<Long>()

        override fun trackEvent(eventName: String, params: Map<String, Any>) {
            events.add(Pair(eventName, params))
        }

        override fun flush() {
            flushCount.add(1L)
        }
    }

    @BeforeTest
    fun setUp() {
        AppAnalyticsManager.resetForTesting()
    }

    @AfterTest
    fun tearDown() {
        AppAnalyticsManager.resetForTesting()
    }

    @Test
    fun testUninitializedAccessThrows() {
        assertFailsWith<IllegalStateException> {
            AppAnalyticsManager.trackEvent("test_event")
        }
        assertFailsWith<IllegalStateException> {
            AppAnalyticsManager.setUserContext("user_1")
        }
        assertFailsWith<IllegalStateException> {
            AppAnalyticsManager.flush()
        }
    }

    @Test
    fun testConcurrentTrackEventAndContextMutation() = runTest {
        val recordingTracker = RecordingTracker()
        AppAnalyticsManager.init(
            AnalyticsConfig(
                platformName = "TestPlatform",
                appVersion = "1.0.0",
                deviceId = "device_test",
                trackers = listOf(recordingTracker)
            )
        )

        val jobs = (1..50).map { index ->
            async(Dispatchers.Default) {
                AppAnalyticsManager.setUserContext("user_$index", "role_$index")
                AppAnalyticsManager.trackEvent("event_$index", mapOf("index" to index))
                AppAnalyticsManager.trackScreenView("screen_$index")
                AppAnalyticsManager.flush()
            }
        }

        jobs.awaitAll()
        assertTrue(AppAnalyticsManager.isInitialized)
    }

    @Test
    fun testDuplicateInitThrows() {
        AppAnalyticsManager.init(
            AnalyticsConfig(
                platformName = "TestPlatform",
                appVersion = "1.0.0",
                deviceId = "device_test",
                trackers = emptyList()
            )
        )

        assertFailsWith<IllegalStateException> {
            AppAnalyticsManager.init(
                AnalyticsConfig(
                    platformName = "TestPlatform2",
                    appVersion = "2.0.0",
                    deviceId = "device_test2",
                    trackers = emptyList()
                )
            )
        }
    }
}
