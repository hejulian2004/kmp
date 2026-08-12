/**
 * @File: StorageError.kt
 * @Package: org.example.project.core.storage.api
 * @Description: 统一存储错误类型密封类与存储异常封装
 * @Author: 何聚敛
 * @Date: 2026-08-12
 */
package org.example.project.core.storage.api

/**
 * 统一存储错误领域模型。
 */
sealed class StorageError {

    /** 目标文件或目录不存在 */
    data object NotFound : StorageError()

    /** 路径非法、跨越根目录或包含非法字符 */
    data object InvalidPath : StorageError()

    /** 无读写权限 */
    data object PermissionDenied : StorageError()

    /** 磁盘空间不足 */
    data object NoSpaceLeft : StorageError()

    /** 文件或目录已存在 */
    data object AlreadyExists : StorageError()

    /** 通用 IO 操作异常 */
    data class IoError(
        val message: String?
    ) : StorageError()

    /** 未知底座异常 */
    data class Unknown(
        val cause: Throwable
    ) : StorageError()
}

/**
 * 统一存储业务异常类。
 * 
 * @property error 具体的领域存储错误类型
 */
class StorageException(
    val error: StorageError,
    message: String? = null,
    cause: Throwable? = null
) : Exception(message ?: "Storage operation failed: $error", cause)
