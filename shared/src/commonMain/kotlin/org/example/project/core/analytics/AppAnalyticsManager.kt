/**
 * @File: AppAnalyticsManager.kt
 * @Package: org.example.project.core.analytics
 * @Description: KMP全局数据埋点应用单例管理器，提供统一事件分发、全局属性注入与多渠道Tracker挂载能力（基于PlatformLock平台锁实现并发安全）
 * @Author: 何聚敛
 * @Date: 2026-08-18
 */
package org.example.project.core.analytics

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import org.example.project.core.concurrent.PlatformLock
import kotlin.time.Clock

/**
 * 全局应用数据埋点单例管理器 (App Singleton)
 */
object AppAnalyticsManager : AnalyticsTracker {
    private val lock = PlatformLock()
    private var _isInitialized = false

    val isInitialized: Boolean
        get() = lock.withLock { _isInitialized }

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
        lock.withLock {
            check(!_isInitialized) {
                "AppAnalyticsManager 已经初始化过，严禁重复初始化！"
            }
            this.config = config

            globalParams["platform"] = config.platformName
            globalParams["app_version"] = config.appVersion
            globalParams["device_id"] = config.deviceId

            trackers.clear()
            trackers.addAll(config.trackers)

            _isInitialized = true
        }
    }

    /**
     * 重置单例内部状态（仅供单元测试隔离使用）
     */
    internal fun resetForTest() {
        resetForTesting()
    }

    /**
     * 重置单例内部状态（仅供单元测试隔离使用）
     */
    internal fun resetForTesting() {
        lock.withLock {
            _isInitialized = false
            config = null
            globalParams.clear()
            trackers.clear()
        }
    }

    /**
     * 检查单例初始化状态，未初始化时直接抛出 IllegalStateException 报错
     */
    private fun checkInitialized() {
        val initialized = lock.withLock { _isInitialized }
        check(initialized) {
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
        lock.withLock {
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

        val (fullParams, targetTrackers) = lock.withLock {
            val combinedParams = globalParams.toMutableMap().apply {
                putAll(params)
                put("timestamp", Clock.System.now().toEpochMilliseconds())
            }
            val currentTrackers = if (trackers.isNotEmpty()) trackers.toList() else listOf(LogAnalyticsTracker())
            Pair(combinedParams, currentTrackers)
        }

        // 多渠道 Tracker 隔离分发，在持锁区域外异步上报
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
        val targetTrackers = lock.withLock { trackers.toList() }
        targetTrackers.forEach { tracker ->
            runCatching { tracker.flush() }
        }
    }
}
