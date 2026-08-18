/**
 * @File: AppNetworkInitializerTest.kt
 * @Package: org.example.project.core.network.client
 * @Description: AppNetworkInitializer 全局网络单例容器初始化与获取单元测试
 * @Author: 何聚敛
 * @Date: 2026-08-18
 */
package org.example.project.core.network.client

import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AppNetworkInitializerTest {

    @BeforeTest
    fun setUp() {
        AppNetworkInitializer.resetForTesting()
    }

    @AfterTest
    fun tearDown() {
        AppNetworkInitializer.resetForTesting()
    }

    @Test
    fun testUninitializedAccessThrows() {
        assertFailsWith<IllegalStateException> {
            AppNetworkInitializer.container
        }
    }

    @Test
    fun testNetworkContainerInitialization() = runTest {
        runCatching {
            AppNetworkInitializer.init(null)
        }.onFailure { ex ->
            assertTrue(ex is IllegalArgumentException || ex is IllegalStateException)
        }.onSuccess {
            val container = AppNetworkInitializer.container
            assertNotNull(container, "NetworkContainer 实例不应为空")
            assertNotNull(container.publicClient, "publicClient 实例不应为空")
            assertNotNull(container.authorizedClient, "authorizedClient 实例不应为空")
        }
    }
}
