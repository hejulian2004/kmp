/**
 * @File: AppAnalyticsManager.kt
 * @Package: org.example.project.core.analytics
 * @Description: KMP全局数据埋点应用单例管理器，提供统一事件分发、全局属性注入与多渠道Tracker挂载能力
 * @Author: 何聚敛
 * @Date: 2026-08-18
 */
package org.example.project.core.analytics

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlin.time.Clock

private inline fun <T> synchronized(lock: Any, block: () -> T): T = block()

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
     * 显式初始化埋点单例（强制唯一性，严禁重复初始化）
     * 
     * @param config 初始化配置对象
     * @throws IllegalStateException 若已初始化过则抛出异常，防止重复初始化
     */
    fun init(config: AnalyticsConfig) {
        synchronized(this) {
            check(!isInitialized) {
                "AppAnalyticsManager 已经初始化过，严禁重复初始化！"
            }
            this.config = config

            synchronized(globalParams) {
                globalParams["platform"] = config.platformName
                globalParams["app_version"] = config.appVersion
                globalParams["device_id"] = config.deviceId
            }

            synchronized(trackers) {
                trackers.clear()
                trackers.addAll(config.trackers)
            }

            isInitialized = true
        }
    }

    /**
     * 重置单例内部状态（仅供单元测试隔离使用）
     */
    internal fun resetForTest() {
        synchronized(this) {
            isInitialized = false
            config = null
            synchronized(globalParams) {
                globalParams.clear()
            }
            synchronized(trackers) {
                trackers.clear()
            }
        }
    }

    /**
     * 检查单例初始化状态，未初始化时直接抛出 IllegalStateException 报错
     */
    private fun checkInitialized() {
        check(isInitialized) {
            "AppAnalyticsManager 尚未初始化！必须首先在应用入口（如 AppInitializer.init()）中显式调用 AppAnalyticsManager.init(...) 方法完成初始化！"
        }
    }

    /**
     * 设置当前登录用户上下文信息
     * 
     * @param userId 用户 ID
     * @param userRole 用户角色
     */
    fun setUserContext(userId: String?, userRole: String? = null) {
        checkInitialized()
        synchronized(globalParams) {
            if (userId != null) {
                globalParams[AnalyticsParams.USER_ID] = userId
                userRole?.let { globalParams["user_role"] = it }
            } else {
                globalParams.remove(AnalyticsParams.USER_ID)
                globalParams.remove("user_role")
            }
        }
    }

    /**
     * 页面曝光/进入页面事件统一代理
     * 
     * @param screenName 页面名称
     * @param extraParams 额外扩展参数
     */
    fun trackScreenView(screenName: String, extraParams: Map<String, Any> = emptyMap()) {
        checkInitialized()
        val params = extraParams.toMutableMap()
        params[AnalyticsParams.SCREEN_NAME] = screenName
        trackEvent(AnalyticsEvents.ENTER_SCREEN, params)
    }

    /**
     * 上报基础事件（强制未初始化时直接报错，禁止默认初始化）
     * 
     * @param eventName 事件名称
     * @param params 自定义扩展参数
     */
    override fun trackEvent(eventName: String, params: Map<String, Any>) {
        checkInitialized()

        val fullParams = synchronized(globalParams) {
            globalParams.toMutableMap().apply {
                putAll(params)
                put("timestamp", Clock.System.now().toEpochMilliseconds())
            }
        }

        val targetTrackers = synchronized(trackers) {
            if (trackers.isNotEmpty()) trackers.toList() else listOf(LogAnalyticsTracker())
        }

        // 多渠道 Tracker 隔离分发，避免单渠道异常卡死主流程
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
        checkInitialized()
        val targetTrackers = synchronized(trackers) { trackers.toList() }
        targetTrackers.forEach { tracker ->
            runCatching { tracker.flush() }
        }
    }
}
