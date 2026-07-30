/**
 * @File: NetworkExceptionMapper.kt
 * @Package: org.example.project.core.network.error
 * @Description: 网络层统一异常映射器，安全处理 Ktor/平台异常并转换成 NetworkError
 * @Author: 何聚敛
 * @Date: 2026-07-30
 */
package org.example.project.core.network.error

import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.RedirectResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException

/**
 * [NetworkExceptionMapper]
 * 负责将底层捕获的 Throwable 或 HttpResponse 状态码转换为规范统一的 [NetworkError]。
 */
object NetworkExceptionMapper {

    /**
     * 将 Exception 映射为 [NetworkError]
     * 
     * @param throwable 捕获到的异常
     * @return 返回映射后的 NetworkError
     * @throws CancellationException 如果是协程取消异常，必须原样重新抛出！
     */
    fun mapException(throwable: Throwable): NetworkError {
        // 关键约束：CancellationException 必须原样重新抛出
        if (throwable is CancellationException) {
            throw throwable
        }

        return when (throwable) {
            is NetworkException -> throwable.error
            is ForceUpdateException -> NetworkError.UpdateRequired(throwable.requiredBuild)
            is HttpRequestTimeoutException -> NetworkError.Timeout
            is SerializationException -> NetworkError.Serialization(throwable.message ?: "Serialization error")
            is ClientRequestException -> mapHttpResponse(throwable.response)
            is ServerResponseException -> mapHttpResponse(throwable.response)
            is RedirectResponseException -> mapHttpResponse(throwable.response)
            else -> {
                val className = throwable::class.simpleName ?: ""
                val msg = throwable.message ?: ""
                if (className.contains("Timeout", ignoreCase = true) || msg.contains("timeout", ignoreCase = true)) {
                    NetworkError.Timeout
                } else if (className.contains("IOException", ignoreCase = true) ||
                    className.contains("UnknownHostException", ignoreCase = true) ||
                    className.contains("ConnectException", ignoreCase = true) ||
                    className.contains("SocketException", ignoreCase = true)
                ) {
                    NetworkError.NoConnection
                } else {
                    NetworkError.Unknown(throwable.message, throwable)
                }
            }
        }
    }

    /**
     * 将 HTTP Response 状态码映射为 [NetworkError]
     * 
     * @param response HTTP 响应对象
     * @return 返回对应的 NetworkError
     */
    fun mapHttpResponse(response: HttpResponse): NetworkError {
        val status = response.status
        return when (status.value) {
            401 -> NetworkError.Unauthorized
            403 -> NetworkError.Forbidden
            404 -> NetworkError.NotFound
            409, 412 -> {
                val serverVersion = response.headers["X-Server-Version"]
                NetworkError.Conflict(status.value, serverVersion)
            }
            429 -> {
                val retryAfter = response.headers["Retry-After"]?.toLongOrNull()
                NetworkError.RateLimited(retryAfter)
            }
            in 500..599 -> NetworkError.Server(status.value)
            else -> NetworkError.Unknown("HTTP Status ${status.value}", null)
        }
    }
}
