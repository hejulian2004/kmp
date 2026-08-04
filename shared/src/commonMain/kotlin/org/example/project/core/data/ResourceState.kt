/**
 * @File: ResourceState.kt
 * @Package: org.example.project.core.data
 * @Description: 项目全局通用响应式资源状态模型
 * @Author: 何聚敛
 * @Date: 2026-08-04
 */
package org.example.project.core.data

import org.example.project.domain.error.AppError

/**
 * 项目全局通用响应式资源状态封装
 */
sealed interface ResourceState<out T> {
    /**
     * 加载中状态
     * @param cachedData 本地旧缓存数据（可用于 UI 0ms 瞬间渲染）
     */
    data class Loading<T>(val cachedData: T? = null) : ResourceState<T>

    /**
     * 权威数据同步成功状态
     * @param data 最新权威数据
     */
    data class Success<T>(val data: T) : ResourceState<T>

    /**
     * 数据同步或网络请求失败状态
     * @param error 转换后的领域错误 AppError
     * @param cachedData 本地旧缓存降级数据（保证 UI 离线可用不崩溃）
     */
    data class Error<T>(val error: AppError, val cachedData: T? = null) : ResourceState<T>
}
