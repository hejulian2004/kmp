/**
 * @File: NetworkClientTest.kt
 * @Package: org.example.project.core.network
 * @Description: KMP 核心网络层单元测试集合（使用 Ktor MockEngine）
 * @Author: 何聚敛
 * @Date: 2026-07-30
 */
package org.example.project.core.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.example.project.core.network.auth.AuthTokens
import org.example.project.core.network.auth.InMemorySecureStorage
import org.example.project.core.network.auth.SessionManager
import org.example.project.core.network.auth.TokenRefresher
import org.example.project.core.network.auth.TokenStore
import org.example.project.core.network.client.DefaultNetworkContainer
import org.example.project.core.network.client.NetworkClientFactoryImpl
import org.example.project.core.network.error.NetworkError
import org.example.project.core.network.error.NetworkExceptionMapper
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class NetworkClientTest {

    @Test
    fun testGetRequestRetryBehavior() = runTest {
        var callCount = 0
        val mockEngine = MockEngine { _ ->
            callCount++
            // 模拟 500 持续失败
            respond(
                content = """{"error": "server error"}""",
                status = HttpStatusCode.InternalServerError,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val storage = InMemorySecureStorage()
        val tokenStore = TokenStore(storage)
        val tokenRefresher = TokenRefresher(tokenStore)
        val factory = NetworkClientFactoryImpl(tokenStore, tokenRefresher, customEngine = mockEngine)
        val container = DefaultNetworkContainer(factory)

        try {
            container.publicClient.get("https://example.com/api/test")
        } catch (_: Exception) {
        }

        // GET 幂等请求默认重试 2 次，首次 + 2次重试 = 总共 3 次请求
        assertEquals(3, callCount, "GET 幂等请求总尝试次数应为 3 次")
        container.close()
    }

    @Test
    fun testPostRequestNoRetryBehavior() = runTest {
        var callCount = 0
        val mockEngine = MockEngine { _ ->
            callCount++
            respond(
                content = """{"error": "server error"}""",
                status = HttpStatusCode.InternalServerError,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val storage = InMemorySecureStorage()
        val tokenStore = TokenStore(storage)
        val tokenRefresher = TokenRefresher(tokenStore)
        val factory = NetworkClientFactoryImpl(tokenStore, tokenRefresher, customEngine = mockEngine)
        val container = DefaultNetworkContainer(factory)

        try {
            container.publicClient.post("https://example.com/api/test")
        } catch (_: Exception) {
        }

        // POST 写请求禁止自动重试，应仅有 1 次请求
        assertEquals(1, callCount, "POST 写请求尝试次数应为 1 次 (零自动重试)")
        container.close()
    }

    @Test
    fun testCancellationExceptionRethrown() {
        val mapper = NetworkExceptionMapper
        val cancelException = CancellationException("Job was cancelled")

        assertFailsWith<CancellationException> {
            mapper.mapException(cancelException)
        }
    }

    @Test
    fun testConflictErrorMapping() = runTest {
        val mockEngine = MockEngine { request ->
            when (request.url.encodedPath) {
                "/409" -> respond(
                    content = """{"error": "conflict"}""",
                    status = HttpStatusCode.Conflict,
                    headers = headersOf("X-Server-Version", "v2.1")
                )
                "/412" -> respond(
                    content = """{"error": "precondition failed"}""",
                    status = HttpStatusCode.PreconditionFailed,
                    headers = headersOf("X-Server-Version", "v2.1")
                )
                else -> respond("", HttpStatusCode.OK)
            }
        }

        val storage = InMemorySecureStorage()
        val tokenStore = TokenStore(storage)
        val tokenRefresher = TokenRefresher(tokenStore)
        val factory = NetworkClientFactoryImpl(tokenStore, tokenRefresher, customEngine = mockEngine)
        val client = factory.createPublicClient()

        val resp409 = client.get("https://example.com/409")
        val error409 = NetworkExceptionMapper.mapHttpResponse(resp409)
        assertIs<NetworkError.Conflict>(error409)
        assertEquals(409, error409.statusCode)
        assertEquals("v2.1", error409.serverVersion)

        val resp412 = client.get("https://example.com/412")
        val error412 = NetworkExceptionMapper.mapHttpResponse(resp412)
        assertIs<NetworkError.Conflict>(error412)
        assertEquals(412, error412.statusCode)

        client.close()
    }

    @Test
    fun testRequestIdHeaderAdded() = runTest {
        var capturedRequestId: String? = null
        val mockEngine = MockEngine { request ->
            capturedRequestId = request.headers["X-Request-Id"]
            respond("OK", HttpStatusCode.OK)
        }

        val storage = InMemorySecureStorage()
        val tokenStore = TokenStore(storage)
        val tokenRefresher = TokenRefresher(tokenStore)
        val factory = NetworkClientFactoryImpl(tokenStore, tokenRefresher, customEngine = mockEngine)
        val client = factory.createPublicClient()

        client.get("https://example.com/api/ping")
        assertNotNull(capturedRequestId, "请求必须被注入 X-Request-Id Header")
        assertTrue(capturedRequestId!!.isNotEmpty(), "X-Request-Id 不应为空")

        client.close()
    }

    @Test
    fun testTokenStoreSaveAndLoad() = runTest {
        val storage = InMemorySecureStorage()
        val tokenStore = TokenStore(storage)

        val initialTokens = tokenStore.loadTokens()
        assertEquals(null, initialTokens)

        val newTokens = AuthTokens(accessToken = "access_123", refreshToken = "refresh_456")
        tokenStore.saveTokens(newTokens)

        val loadedTokens = tokenStore.loadTokens()
        assertNotNull(loadedTokens)
        assertEquals("access_123", loadedTokens.accessToken)
        assertEquals("refresh_456", loadedTokens.refreshToken)

        tokenStore.clearTokens()
        assertEquals(null, tokenStore.loadTokens())
    }

    @Test
    fun testSessionExpiredEventOnRefreshFailure() = runTest {
        val storage = InMemorySecureStorage()
        val tokenStore = TokenStore(storage)
        val oldTokens = AuthTokens("expired_access", "invalid_refresh")
        tokenStore.saveTokens(oldTokens)

        val tokenRefresher = TokenRefresher(tokenStore, refreshEndpoint = "https://example.com/api/v1/auth/refresh")
        val mockEngine = MockEngine {
            respond(
                content = """{"error": "invalid_grant"}""",
                status = HttpStatusCode.Unauthorized,
                headers = headersOf("Content-Type", "application/json")
            )
        }
        val publicClient = HttpClient(mockEngine)

        val result = tokenRefresher.refreshToken(publicClient, oldTokens)
        assertEquals(null, result)
        assertEquals(null, tokenStore.loadTokens(), "刷新失败后 TokenStore 必须清除旧凭据")

        val expiredEvent = SessionManager.sessionExpiredEvents.first()
        assertEquals(Unit, expiredEvent, "刷新失败后必须广播 Session 登出失效事件")

        publicClient.close()
    }
}
