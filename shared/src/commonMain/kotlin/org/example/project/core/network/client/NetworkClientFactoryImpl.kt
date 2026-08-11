/**
 * @File: NetworkClientFactoryImpl.kt
 * @Package: org.example.project.core.network.client
 * @Description: NetworkClientFactory工厂具体实现类
 * @Author: 何聚敛
 * @Date: 2026-07-30
 */
package org.example.project.core.network.client

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.websocket.WebSockets
import org.example.project.core.network.auth.AuthTokens
import org.example.project.core.network.auth.TokenRefresher
import org.example.project.core.network.auth.TokenStore
import org.example.project.core.network.config.CommonNetworkConfig.applyCommonDefaults
import org.example.project.core.network.config.RequestIdPlugin

/**
 * [NetworkClientFactoryImpl]
 * 工厂的具体实现，根据规范要求创建隔离的HttpClient实例。
 * 
 * @param tokenStore本地凭据存储
 * @param tokenRefresher Token单飞刷新器
 * @param customEngine可选传入的引擎（用于MockEngine单元测试），为null时自动匹配平台默认引擎
 * @param currentAppBuild当前应用的逻辑构建号，用于客户端版本检测
 */
class NetworkClientFactoryImpl(
    private val tokenStore: TokenStore,
    private val tokenRefresher: TokenRefresher,
    private val customEngine: HttpClientEngine? = null,
    private val currentAppBuild: Long = 100L
) : NetworkClientFactory {

    private fun createClient(block: io.ktor.client.HttpClientConfig<*>.() -> Unit): HttpClient {
        return if (customEngine != null) {
            HttpClient(customEngine, block)
        } else {
            HttpClient(block)
        }
    }

    override fun createPublicClient(): HttpClient {
        return createClient {
            applyCommonDefaults(currentAppBuild)
            install(RequestIdPlugin)
        }
    }

    override fun createAuthorizedClient(): HttpClient {
        // 创建用于Token刷新的PublicClient，避免401递归死锁
        val publicClient = createPublicClient()

        return createClient {
            applyCommonDefaults(currentAppBuild)
            install(RequestIdPlugin)

            // 配置Ktor Auth插件
            install(Auth) {
                bearer {
                    loadTokens {
                        val tokens = tokenStore.loadTokens()
                        if (tokens != null) {
                            BearerTokens(tokens.accessToken, tokens.refreshToken)
                        } else {
                            null
                        }
                    }

                    refreshTokens {
                        val oldAccessToken = oldTokens?.accessToken ?: ""
                        val oldRefreshToken = oldTokens?.refreshToken ?: ""
                        val refreshed = tokenRefresher.refreshToken(
                            publicClient = publicClient,
                            oldTokens = AuthTokens(oldAccessToken, oldRefreshToken)
                        )

                        if (refreshed != null) {
                            BearerTokens(refreshed.accessToken, refreshed.refreshToken)
                        } else {
                            null
                        }
                    }

                    // 仅允许给官方与安全域名发送Bearer Token
                    sendWithoutRequest { request ->
                        request.url.host.endsWith("example.com") || request.url.host == "localhost" || request.url.host == "127.0.0.1"
                    }
                }
            }
        }
    }

    override fun createUploadClient(): HttpClient {
        return createClient {
            applyCommonDefaults(currentAppBuild)
            install(RequestIdPlugin)

            // 上传客户端规范约束：禁止自动重试写请求
            install(HttpRequestRetry) {
                maxRetries = 0
            }

            // 独立上传超时策略 (请求上限120秒)
            install(HttpTimeout) {
                requestTimeoutMillis = 120_000L
                socketTimeoutMillis = 30_000L
            }
        }
    }

    override fun createRealtimeClient(): HttpClient {
        return createClient {
            applyCommonDefaults(currentAppBuild)
            install(RequestIdPlugin)
            install(WebSockets)
        }
    }
}
