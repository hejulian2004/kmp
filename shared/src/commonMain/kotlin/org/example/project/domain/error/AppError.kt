/**
 * @File: AppError.kt
 * @Package: org.example.project.domain.error
 * @Description: 业务领域统一密封错误契约与网络错误映射器
 * @Author: 何聚敛
 * @Date: 2026-07-30
 */
package org.example.project.domain.error

import kotlinx.coroutines.CancellationException
import org.example.project.core.network.error.NetworkError
import org.example.project.core.network.error.NetworkExceptionMapper

/**
 * [AppError]
 * 业务领域层强类型密封错误模型。
 * 屏蔽底层网络细节，向UI/ViewModel层暴露业务可读的错误类型。
 */
sealed interface AppError {
    /** 无网络连接或DNS解析失败 */
    data object NetworkUnavailable : AppError

    /** 请求超时或读写超时 */
    data object Timeout : AppError

    /** 401登录凭据已过期或无效 */
    data object LoginExpired : AppError

    /** 403权限不足无权访问 */
    data object PermissionDenied : AppError

    /** 404请求资源不存在 */
    data object NotFound : AppError

    /** 429请求过于频繁被限流 */
    data class RateLimited(val retryAfterSeconds: Long? = null) : AppError

    /** 409与412并发/版本冲突 */
    data class DataConflict(val statusCode: Int, val serverVersion: String? = null) : AppError

    /** 5xx服务器内部故障 */
    data class ServerFailure(val code: Int) : AppError

    /** 客户端版本过低强制更新 */
    data class UpdateRequired(val requiredBuild: Long) : AppError

    /** 未知领域错误 */
    data class Unknown(val message: String? = null, val cause: Throwable? = null) : AppError
}

/**
 * 将底层 [NetworkError] 转为领域契约 [AppError]
 * 
 * @receiver 底层网络错误模型
 * @return 返回转换后的领域错误 AppError
 */
fun NetworkError.toAppError(): AppError {
    return when (this) {
        is NetworkError.NoConnection -> AppError.NetworkUnavailable
        is NetworkError.Timeout -> AppError.Timeout
        is NetworkError.Unauthorized -> AppError.LoginExpired
        is NetworkError.Forbidden -> AppError.PermissionDenied
        is NetworkError.NotFound -> AppError.NotFound
        is NetworkError.RateLimited -> AppError.RateLimited(retryAfterSeconds)
        is NetworkError.Conflict -> AppError.DataConflict(statusCode, serverVersion)
        is NetworkError.Server -> AppError.ServerFailure(code)
        is NetworkError.Serialization -> AppError.Unknown("数据解析失败: $message", null)
        is NetworkError.UpdateRequired -> AppError.UpdateRequired(requiredBuild)
        is NetworkError.Unknown -> AppError.Unknown(message, cause)
    }
}

/**
 * 将任意 Throwable 转换为领域契约 [AppError]
 * 注意：保留 CancellationException 重新抛出约束！
 * 
 * @receiver 捕获到的异常对象
 * @return 返回转换后的 AppError
 * @throws CancellationException 协程取消异常原样重新抛出
 */
fun Throwable.toAppError(): AppError {
    if (this is CancellationException) {
        throw this
    }
    val networkError = NetworkExceptionMapper.mapException(this)
    return networkError.toAppError()
}
