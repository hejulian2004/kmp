/**
 * @File: AppAnalyticsManager.kt
 * @Package: org.example.project.core.analytics
 * @Description: KMP全局数据埋点应用单例管理器，提供统一事件分发、全局属性注入与多渠道Tracker挂载能力
 * @Author: 何聚敛
 * @Date: 2026-08-10
 */
package org.example.project.core.analytics

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlin.time.Clock

/**
 * 全局应用数据埋点单例管理器 (App Singleton)
 */
object AppAnalyticsManager : AnalyticsTracker {
    private var isInitialized = false
    private var config: AnalyticsConfig? = null
    private val trackers = mutableListOf<AnalyticsTracker>()
    private val globalParams = mutableMapOf<String, Any>()
    private val scope = CoroutineScope(Dispatchers.IO)

    /**
     * 显式初始化埋点单例
     * 
     * @param config 初始化配置对象
     */
    fun init(config: AnalyticsConfig) {
        if (isInitialized) return
        isInitialized = true
        this.config = config
        
        globalParams["platform"] = config.platformName
        globalParams["app_version"] = config.appVersion
        globalParams["device_id"] = config.deviceId

        trackers.clear()
        trackers.addAll(config.trackers)
    }

    /**
     * 设置当前登录用户上下文信息
     * 
     * @param userId 用户 ID
     * @param userRole 用户角色
     */
    fun setUserContext(userId: String?, userRole: String? = null) {
        if (userId != null) {
            globalParams[AnalyticsParams.USER_ID] = userId
            userRole?.let { globalParams["user_role"] = it }
        } else {
            globalParams.remove(AnalyticsParams.USER_ID)
            globalParams.remove("user_role")
        }
    }

    /**
     * 页面曝光/进入页面事件统一代理
     * 
     * @param screenName 页面名称
     * @param extraParams 额外扩展参数
     */
    fun trackScreenView(screenName: String, extraParams: Map<String, Any> = emptyMap()) {
        val params = extraParams.toMutableMap()
        params[AnalyticsParams.SCREEN_NAME] = screenName
        trackEvent(AnalyticsEvents.ENTER_SCREEN, params)
    }

    /**
     * 上报基础事件
     * 
     * @param eventName 事件名称
     * @param params 自定义扩展参数
     */
    override fun trackEvent(eventName: String, params: Map<String, Any>) {
        if (!isInitialized) {
            println("AppAnalyticsManager 警告: 埋点单例尚未初始化，事件 [$eventName] 使用默认控制台保底分发")
        }

        val fullParams = globalParams.toMutableMap().apply {
            putAll(params)
            put("timestamp", Clock.System.now().toEpochMilliseconds())
        }

        val targetTrackers = if (trackers.isNotEmpty()) {
            trackers.toList()
        } else {
            listOf(LogAnalyticsTracker())
        }

        scope.launch {
            targetTrackers.forEach { tracker ->
                runCatching {
                    tracker.trackEvent(eventName, fullParams)
                }.onFailure { ex ->
                    println("AppAnalyticsManager 错误: Tracker [${tracker::class.simpleName}] 分发失败: ${ex.message}")
                }
            }
        }
    }

    override fun flush() {
        trackers.forEach { 
            runCatching { it.flush() }
        }
    }
}
