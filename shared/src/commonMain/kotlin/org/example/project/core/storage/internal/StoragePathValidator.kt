/**
 * @File: StoragePathValidator.kt
 * @Package: org.example.project.core.storage.internal
 * @Description: 逻辑存储相对路径安全校验与规范化校验器
 * @Author: 何聚敛
 * @Date: 2026-08-12
 */
package org.example.project.core.storage.internal

import org.example.project.core.storage.api.StorageError
import org.example.project.core.storage.api.StorageException
import org.example.project.core.storage.api.StoragePath

/**
 * 逻辑路径安全校验器。
 * 
 * 严格防护目录穿越、路径逃逸与绝对路径绕过。
 */
object StoragePathValidator {

    /**
     * 校验并规范化逻辑相对路径。
     * 
     * @param path 待校验的逻辑相对路径
     * @return 规范化后的相对路径字符串 (统一以 "/" 分隔，去除前后余量空格)
     * @throws StorageException 若路径非法或存在越界逃逸风险抛出
     */
    fun validateAndNormalize(path: StoragePath): String {
        val rawValue = path.value.trim()

        // 包含空字符的路径直接判定为非法
        if (rawValue.contains('\u0000')) {
            throw StorageException(StorageError.InvalidPath, "Path contains null character: ${path.value}")
        }

        // 统一分隔符为斜杠
        val normalized = rawValue.replace('\\', '/')

        // 检查是否为绝对路径 (Unix 绝对路径 '/' 或 Windows 盘符如 'C:')
        if (normalized.startsWith("/") || isWindowsDrivePath(normalized)) {
            throw StorageException(StorageError.InvalidPath, "Absolute paths are strictly forbidden: ${path.value}")
        }

        // 按 "/" 拆分段并检查穿越段 ".."
        val segments = normalized.split('/').filter { it.isNotEmpty() }
        for (segment in segments) {
            if (segment == "..") {
                throw StorageException(StorageError.InvalidPath, "Path traversal ('..') is strictly forbidden: ${path.value}")
            }
        }

        return segments.joinToString("/")
    }

    /**
     * 判断相对路径是否合法。
     */
    fun isValid(path: StoragePath): Boolean {
        return try {
            validateAndNormalize(path)
            true
        } catch (_: StorageException) {
            false
        }
    }

    private fun isWindowsDrivePath(path: String): Boolean {
        if (path.length >= 2) {
            val first = path[0]
            val second = path[1]
            return (first in 'a'..'z' || first in 'A'..'Z') && second == ':'
        }
        return false
    }
}
