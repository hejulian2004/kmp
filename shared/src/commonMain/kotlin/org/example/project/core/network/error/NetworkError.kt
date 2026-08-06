/**
 * @File: NetworkError.kt
 * @Package: org.example.project.core.network.error
 * @Description: 网络层统一密封错误类型定义
 * @Author: 何聚敛
 * @Date: 2026-07-30
 */
package org.example.project.core.network.error

/**
 * [NetworkError]
 * 核心网络基础设施抛出的统一密封错误契约。
 * 覆盖网络无连接、超时、认证失败、并发冲突、服务器异常及强制更新等场景。
 */
sealed interface NetworkError {
    /** 网络连接不可用（无网、DNS解析失败等底层 IO 异常） */
    data object NoConnection : NetworkError

    /** 请求超时或数据读写 Socket 超时 */
    data object Timeout : NetworkError

    /** 401 身份凭据无效或已过期 */
    data object Unauthorized : NetworkError

    /** 403 资源无访问权限 */
    data object Forbidden : NetworkError

    /** 404 请求资源不存在 */
    data object NotFound : NetworkError

    /** 429 请求频率触发限流 */
    data class RateLimited(val retryAfterSeconds: Long? = null) : NetworkError

    /** 
     * 409 Conflict或412 Precondition Failed状态码。
     * 用于标识乐观锁版本冲突或条件前置失败。
     */
    data class Conflict(val statusCode: Int, val serverVersion: String? = null) : NetworkError

    /** 5xx 服务器内部异常 */
    data class Server(val code: Int) : NetworkError

    /** JSON 序列化/反序列化解析失败 */
    data class Serialization(val message: String) : NetworkError

    /** 客户端版本过低，由 `X-Min-App-Build-Required` 头校验触发 */
    data class UpdateRequired(val requiredBuild: Long) : NetworkError

    /** 未知网络错误 */
    data class Unknown(val message: String? = null, val cause: Throwable? = null) : NetworkError
}

/**
 * 自定义网络异常类，包裹 [NetworkError]
 */
class NetworkException(
    val error: NetworkError,
    cause: Throwable? = null
) : Exception("Network error: $error", cause)

/**
 * 强制升级异常，专用于触发ForceUpdate流程
 */
class ForceUpdateException(
    val requiredBuild: Long
) : Exception("Client build is outdated. Minimum required build: $requiredBuild")
