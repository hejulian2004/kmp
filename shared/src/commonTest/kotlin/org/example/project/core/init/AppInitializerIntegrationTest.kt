/**
 * @File: AppInitializerIntegrationTest.kt
 * @Package: org.example.project.core.init
 * @Description: 应用启动全链路集成测试（包含基础设施就绪、未初始化拦截、并发防护与重置能力）
 * @Author: 何聚敛
 * @Date: 2026-08-18
 */
package org.example.project.core.init

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import org.example.project.core.analytics.AppAnalyticsManager
import org.example.project.core.database.AppDatabaseInitializer
import org.example.project.core.network.client.AppNetworkInitializer
import org.example.project.core.storage.client.AppStorageInitializer
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AppInitializerIntegrationTest {

    @BeforeTest
    fun setUp() {
        AppInitializer.resetForTesting()
    }

    @AfterTest
    fun tearDown() {
        AppInitializer.resetForTesting()
    }

    @Test
    fun testUninitializedAccessThrowsIllegalStateException() {
        assertFailsWith<IllegalStateException> {
            AppNetworkInitializer.container
        }
        assertFailsWith<IllegalStateException> {
            AppDatabaseInitializer.database
        }
        assertFailsWith<IllegalStateException> {
            AppAnalyticsManager.trackEvent("test_uninit_event")
        }
    }

    @Test
    fun testSuccessfulInitialization() = runTest {
        val params = AppInitParams(
            context = null,
            platformName = "TestPlatform",
            appVersion = "1.0.0",
            deviceId = "test_dev_001"
        )
        val result = AppInitializer.init(params)

        assertTrue(result.success)
        assertEquals(AppInitState.INITIALIZED, result.state)
        assertTrue(AppInitializer.isInitialized)

        assertTrue(AppAnalyticsManager.isInitialized)
        assertTrue(AppStorageInitializer.isInitialized)
        assertTrue(AppNetworkInitializer.isInitialized)
        assertTrue(AppDatabaseInitializer.isInitialized)

        assertNotNull(AppStorageInitializer.container)
        assertNotNull(AppNetworkInitializer.container)
        assertNotNull(AppDatabaseInitializer.database)
    }

    @Test
    fun testReinitializationIsIdempotent() = runTest {
        val params = AppInitParams(platformName = "TestPlatform")
        val result1 = AppInitializer.init(params)
        assertTrue(result1.success)

        val result2 = AppInitializer.init(params)
        assertTrue(result2.success)
        assertEquals(AppInitState.INITIALIZED, result2.state)
    }

    @Test
    fun testConcurrentInitializationExactlyOnce() = runTest {
        val params = AppInitParams(
            platformName = "ConcurrentTestPlatform",
            appVersion = "2.0.0",
            deviceId = "concurrent_dev"
        )

        val deferredResults = (1..20).map {
            async(Dispatchers.Default) {
                AppInitializer.init(params)
            }
        }

        val results = deferredResults.awaitAll()
        assertTrue(results.all { it.success })
        assertEquals(AppInitState.INITIALIZED, AppInitializer.state)
        assertTrue(AppInitializer.isInitialized)
    }

    @Test
    fun testResetForTestingCleansAllSubsystems() = runTest {
        val params = AppInitParams(platformName = "TestPlatform")
        AppInitializer.init(params)
        assertTrue(AppInitializer.isInitialized)

        AppInitializer.resetForTesting()

        assertFalse(AppInitializer.isInitialized)
        assertEquals(AppInitState.NOT_INITIALIZED, AppInitializer.state)
        assertFalse(AppAnalyticsManager.isInitialized)
        assertFalse(AppStorageInitializer.isInitialized)
        assertFalse(AppNetworkInitializer.isInitialized)
        assertFalse(AppDatabaseInitializer.isInitialized)
    }
}
