/**
 * @File: CommonNetworkConfig.kt
 * @Package: org.example.project.core.network.config
 * @Description: Ktor HttpClient公共配置与规范约束参数定义
 * @Author: 何聚敛
 * @Date: 2026-07-30
 */
package org.example.project.core.network.config

import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.observer.ResponseObserver
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.example.project.core.network.error.ForceUpdateException

/**
 * 网络配置常量定义
 */
object NetworkConstants {
    /** 默认Request Timeout: 20秒 */
    const val DEFAULT_REQUEST_TIMEOUT_MS = 20_000L

    /** 默认Socket Timeout: 10秒(必须小于Request Timeout) */
    const val DEFAULT_SOCKET_TIMEOUT_MS = 10_000L

    /** 默认Connect Timeout: 6秒(Android生效) */
    const val DEFAULT_CONNECT_TIMEOUT_MS = 6_000L

    /** 幂等GET请求最大自动重试次数: 2次 */
    const val MAX_RETRIES = 2

    /** 逻辑总重试预算时间: 25秒 */
    const val RETRY_BUDGET_MS = 25_000L

    /** 最小App构建号Header */
    const val HEADER_MIN_APP_BUILD_REQUIRED = "X-Min-App-Build-Required"

    /** 脱敏Header列表 */
    val SENSITIVE_HEADERS = listOf("Authorization", "Cookie", "Set-Cookie")
}

/**
 * [CommonNetworkConfig]
 * 为客户端配置ContentNegotiation JSON、日志脱敏、重试与超时防护规则。
 */
object CommonNetworkConfig {

    /**
     * 配置公共 Json 序列化选项
     */
    val defaultJson = Json {
        prettyPrint = false
        isLenient = true
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    /**
     * 应用通用 Ktor 客户端插件配置
     * 
     * @param currentAppBuild 当前应用 Build 号，用于检测 ForceUpdate
     */
    fun HttpClientConfig<*>.applyCommonDefaults(
        currentAppBuild: Long = 100L
    ) {
        // 1. JSON 序列化插件
        install(ContentNegotiation) {
            json(defaultJson)
        }

        // 2. 日志与 Header 脱敏配置
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    var sanitized = message
                    for (header in NetworkConstants.SENSITIVE_HEADERS) {
                        if (sanitized.contains(header, ignoreCase = true)) {
                            sanitized = sanitized.replace(
                                Regex("(?i)($header:\\s*)([^\\r\\n]+)"),
                                "$1******"
                            )
                        }
                    }
                    println("[KtorClient] $sanitized")
                }
            }
            level = LogLevel.HEADERS
        }

        // 3. HttpRequestRetry 插件 (架构约束：必须安装在 HttpTimeout 之前)
        install(HttpRequestRetry) {
            maxRetries = NetworkConstants.MAX_RETRIES
            // 规则：仅幂等方法重试 (GET, HEAD, OPTIONS) 且匹配可重试状态码: 408, 429, 500, 502, 503, 504
            retryIf { request, response ->
                val method = request.method
                val isIdempotent = method == HttpMethod.Get || method == HttpMethod.Head || method == HttpMethod.Options
                if (!isIdempotent) return@retryIf false

                val code = response.status.value
                code == 408 || code == 429 || (code in 500..504)
            }
            exponentialDelay(base = 2.0, maxDelayMs = 4000L)
        }

        // 4. HttpTimeout 插件 (架构约束：必须安装在 HttpRequestRetry 之后)
        install(HttpTimeout) {
            requestTimeoutMillis = NetworkConstants.DEFAULT_REQUEST_TIMEOUT_MS
            socketTimeoutMillis = NetworkConstants.DEFAULT_SOCKET_TIMEOUT_MS
            connectTimeoutMillis = NetworkConstants.DEFAULT_CONNECT_TIMEOUT_MS
        }

        // 5. 检查 X-Min-App-Build-Required 响应头
        install(ResponseObserver) {
            onResponse { response ->
                val minBuildHeader = response.headers[NetworkConstants.HEADER_MIN_APP_BUILD_REQUIRED]
                val requiredBuild = minBuildHeader?.toLongOrNull()
                if (requiredBuild != null && currentAppBuild < requiredBuild) {
                    throw ForceUpdateException(requiredBuild)
                }
            }
        }
    }
}
