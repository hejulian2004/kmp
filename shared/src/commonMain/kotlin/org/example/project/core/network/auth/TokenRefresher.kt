/**
 * @File: TokenRefresher.kt
 * @Package: org.example.project.core.network.auth
 * @Description: 提供全局single-flight控制的Token刷新器
 * @Author: 何聚敛
 * @Date: 2026-07-30
 */
package org.example.project.core.network.auth

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable

/**
 * 刷新Token的Request Body
 */
@Serializable
private data class RefreshTokenRequest(
    val refreshToken: String
)

/**
 * 刷新Token的Response Body
 */
@Serializable
private data class RefreshTokenResponse(
    val accessToken: String,
    val refreshToken: String
)

/**
 * [TokenRefresher]
 * 负责Token过期时的刷新逻辑。
 * 包含单飞(Single-Flight)机制，并发401请求合并为一次网络调用。
 * 
 * @param tokenStore 本地Token凭据存储
 * @param refreshEndpoint 刷新接口的完整URL地址（默认"/api/v1/auth/refresh"）
 */
class TokenRefresher(
    private val tokenStore: TokenStore,
    private val refreshEndpoint: String = "/api/v1/auth/refresh"
) {
    private val mutex = Mutex()

    /**
     * 执行Token刷新（使用单独传入的publicClient避免401递归死锁）
     * 
     * @param publicClient 未挂载Auth插件的公共HttpClient
     * @param oldTokens 当前保存的凭据
     * @return 返回刷新后的AuthTokens，如果刷新失败或刷新接口返回错误则返回null
     */
    suspend fun refreshToken(publicClient: HttpClient, oldTokens: AuthTokens): AuthTokens? {
        return mutex.withLock {
            // 在获得锁之后，先检查本地保存的 Token 是否已经被并发的其他协程更新过
            val currentTokens = tokenStore.loadTokens()
            if (currentTokens != null && currentTokens.accessToken != oldTokens.accessToken) {
                // 已被其他并发请求刷新成功，直接复用最新凭据
                return@withLock currentTokens
            }

            try {
                val response = publicClient.post(refreshEndpoint) {
                    contentType(ContentType.Application.Json)
                    setBody(RefreshTokenRequest(oldTokens.refreshToken))
                }

                if (response.status.value in 200..299) {
                    val body = response.body<RefreshTokenResponse>()
                    val newTokens = AuthTokens(
                        accessToken = body.accessToken,
                        refreshToken = body.refreshToken
                    )
                    tokenStore.saveTokens(newTokens)
                    newTokens
                } else {
                    // 刷新失败，清空凭据强制要求用户重新登录
                    tokenStore.clearTokens()
                    null
                }
            } catch (e: Exception) {
                // 刷新过程发生异常，不清空 Token 保留重试机会，返回 null
                null
            }
        }
    }
}
