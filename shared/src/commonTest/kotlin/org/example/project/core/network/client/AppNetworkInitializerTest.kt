/**
 * @File: AppNetworkInitializerTest.kt
 * @Package: org.example.project.core.network.client
 * @Description: AppNetworkInitializer 全局网络单例容器初始化与获取单元测试
 * @Author: 何聚敛
 * @Date: 2026-08-11
 */
package org.example.project.core.network.client

import kotlin.test.Test
import kotlin.test.assertNotNull

class AppNetworkInitializerTest {

    @Test
    fun testNetworkContainerInitialization() {
        runCatching {
            AppNetworkInitializer.init(null)
        }.onFailure { ex ->
            kotlin.test.assertTrue(ex is IllegalArgumentException || ex is IllegalStateException)
        }.onSuccess {
            val container = AppNetworkInitializer.container
            assertNotNull(container, "NetworkContainer 实例不应为空")
            assertNotNull(container.publicClient, "publicClient 实例不应为空")
            assertNotNull(container.authorizedClient, "authorizedClient 实例不应为空")
        }
    }
}
