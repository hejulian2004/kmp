/**
 * @File: AnalyticsTracker.kt
 * @Package: org.example.project.core.analytics
 * @Description: 全局数据埋点追踪器接口与配置数据模型
 * @Author: 何聚敛
 * @Date: 2026-08-10
 */
package org.example.project.core.analytics

/**
 * 数据埋点追踪器统一接口
 */
interface AnalyticsTracker {
    /**
     * 上报事件
     * 
     * @param eventName 事件名称
     * @param params 附加参数键值对
     */
    fun trackEvent(eventName: String, params: Map<String, Any> = emptyMap())

    /**
     * 强制刷新/提交缓冲区中的埋点数据
     */
    fun flush() {}
}

/**
 * 埋点全局初始化配置实体
 * 
 * @param platformName 宿主平台名称（如 Android / iOS）
 * @param appVersion 应用版本号
 * @param deviceId 设备唯一标识符
 * @param maxLogRetentionCount 本地日志最大留存条数（默认1000条）
 * @param trackers 挂载的具体上报渠道组件列表
 */
data class AnalyticsConfig(
    val platformName: String,
    val appVersion: String,
    val deviceId: String,
    val maxLogRetentionCount: Int = 1000,
    val trackers: List<AnalyticsTracker> = emptyList()
)
