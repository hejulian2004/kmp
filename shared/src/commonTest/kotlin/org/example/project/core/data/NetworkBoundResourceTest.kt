/**
 * @File: NetworkBoundResourceTest.kt
 * @Package: org.example.project.core.data
 * @Description: 全局通用数据同步管道 NetworkBoundResource 单元测试
 * @Author: 何聚敛
 * @Date: 2026-08-05
 */
package org.example.project.core.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NetworkBoundResourceTest {

    @Test
    fun testNetworkBoundResourceSuccessFlow() = runTest {
        val dbFlow = MutableStateFlow<String?>("本地旧数据")

        val states = networkBoundResource(
            key = "user_101",
            queryLocal = { dbFlow },
            fetchRemote = { "网络最新数据" },
            saveRemoteResult = { _, remote -> dbFlow.value = remote }
        ).take(2).toList()

        // 验证 1: 首个发射状态为 Loading 且带本地旧数据
        val loadingState = states[0] as ResourceState.Loading
        assertEquals("本地旧数据", loadingState.cachedData)

        // 验证 2: 最终发射状态为 Success 且为网络同步后的最新数据
        val finalState = states[1] as ResourceState.Success
        assertEquals("网络最新数据", finalState.data)
    }

    @Test
    fun testNetworkBoundResourceFailureFallback() = runTest {
        val dbFlow = MutableStateFlow<String?>("本地旧数据")

        val states = networkBoundResource(
            key = "user_101",
            queryLocal = { dbFlow },
            fetchRemote = { throw IllegalStateException("网络连接超时") },
            saveRemoteResult = { _, remote -> dbFlow.value = remote }
        ).take(3).toList()

        // 验证 1: 发射 Loading 且带旧数据
        val loadingState = states[0] as ResourceState.Loading
        assertEquals("本地旧数据", loadingState.cachedData)

        // 验证 2: 网络失败发射 Error 状态，且带降级旧数据
        val errorState = states[1] as ResourceState.Error
        assertEquals("本地旧数据", errorState.cachedData)

        // 验证 3: 降级后最终读取到的依然是本地旧数据（通过 Success 状态发回 UI）
        val finalState = states[2] as ResourceState.Success
        assertEquals("本地旧数据", finalState.data)
    }

    @Test
    fun testNetworkBoundResourceShouldFetchFalse() = runTest {
        val dbFlow = MutableStateFlow<String?>("本地有效数据")
        var networkCallCount = 0

        val states = networkBoundResource(
            key = "user_101",
            queryLocal = { dbFlow },
            fetchRemote = {
                networkCallCount++
                "网络数据"
            },
            saveRemoteResult = { _, remote -> dbFlow.value = remote },
            shouldFetch = { false }
        ).take(2).toList()

        assertEquals(0, networkCallCount)
        val finalState = states[1] as ResourceState.Success
        assertEquals("本地有效数据", finalState.data)
    }
}
