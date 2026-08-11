/**
 * @File: ResourceState.kt
 * @Package: org.example.project.core.data
 * @Description: 项目全局通用响应式资源状态模型
 * @Author: 何聚敛
 * @Date: 2026-08-05
 */
package org.example.project.core.data

import org.example.project.domain.error.AppError

/**
 * 项目全局通用响应式资源状态封装
 */
sealed interface ResourceState<out T> {
    /**
     * 加载中状态
     * @param cachedData本地旧缓存数据（可用于UI 0ms瞬间渲染）
     */
    data class Loading<T>(val cachedData: T? = null) : ResourceState<T>

    /**
     * 权威数据同步成功状态
     * @param data最新权威数据
     */
    data class Success<T>(val data: T) : ResourceState<T>

    /**
     * 数据同步或网络请求失败状态
     * @param error转换后的领域错误AppError
     * @param cachedData本地旧缓存降级数据（保证UI离线可用不崩溃）
     */
    data class Error<T>(val error: AppError, val cachedData: T? = null) : ResourceState<T>
}
