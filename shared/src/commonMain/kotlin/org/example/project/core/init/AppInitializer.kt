/**
 * @File: AppInitializer.kt
 * @Package: org.example.project.core.init
 * @Description: 项目全局启动统一初始化器 (appInit)，集中编排网络架构、 Room 数据库与数据埋点单例的启动初始化链
 * @Author: 何聚敛
 * @Date: 2026-08-11
 */
package org.example.project.core.init

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.example.project.core.analytics.AnalyticsConfig
import org.example.project.core.analytics.AnalyticsEvents
import org.example.project.core.analytics.AppAnalyticsManager
import org.example.project.core.analytics.LogAnalyticsTracker
import org.example.project.core.database.getRoomDatabase
import org.example.project.core.network.client.AppNetworkInitializer
import org.example.project.core.sdui.repository.SduiLayoutRepositoryImpl

import org.example.project.core.analytics.AnalyticsParams
import org.example.project.platform.currentTimeMillis

/**
 * 全局应用启动初始化参数配置
 * 
 * @param context 平台上下文相关对象 (如 Android ApplicationContext)
 * @param platformName 宿主平台名称
 * @param appVersion 应用版本号
 * @param deviceId 设备唯一标识符
 * @param maxLogRetentionCount 本地日志保留最大条数
 */
data class AppInitParams(
    val context: Any? = null,
    val platformName: String = "Android",
    val appVersion: String = "1.0.0",
    val deviceId: String = "unknown_device",
    val maxLogRetentionCount: Int = 1000
)

/**
 * 全局应用启动统一初始化器 (AppInitializer / appInit)
 */
object AppInitializer {
    private var isInitialized = false
    private val initScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /**
     * 统一执行应用冷启动依赖链初始化（对齐大厂 APM 与数据埋点架构标准）。
     * 【主线程强同步完成】：1. 数据埋点单例 (最优先零依赖) -> 2. 网络架构核心 -> 3. Room 数据库 -> 4. 上报冷启动总结事件；
     * 【后台异步非阻塞】：5. 异步并发拉取与更新全量模块（Airbnb / FeedLine / Instagram）SDUI 热更 JSON。
     * 
     * @param params 应用启动初始化参数
     */
    fun init(params: AppInitParams) {
        if (isInitialized) return
        isInitialized = true

        val startTime = currentTimeMillis()

        // 1. 最高优先级强同步：最先初始化全局数据埋点单例，确保后续任何崩溃或故障均能被成功捕获与上报
        var isAnalyticsInitialized = false
        try {
            AppAnalyticsManager.init(
                AnalyticsConfig(
                    platformName = params.platformName,
                    appVersion = params.appVersion,
                    deviceId = params.deviceId,
                    maxLogRetentionCount = params.maxLogRetentionCount,
                    trackers = listOf(LogAnalyticsTracker())
                )
            )
            isAnalyticsInitialized = true
        } catch (e: Throwable) {
            println("[AppInitializer 错误] 全局埋点单例初始化失败: ${e.message}")
        }

        var isSuccess = true
        val errorMessages = mutableListOf<String>()

        // 2. 隔离初始化：网络架构单例
        try {
            AppNetworkInitializer.init(params.context)
        } catch (e: Throwable) {
            isSuccess = false
            val errorMsg = "网络层初始化失败: ${e.message ?: "未知异常"}"
            errorMessages.add(errorMsg)
            AppAnalyticsManager.trackEvent(
                AnalyticsEvents.INIT_SUB_ERROR,
                mapOf(
                    AnalyticsParams.SUB_MODULE to "network",
                    AnalyticsParams.ERROR_MSG to errorMsg
                )
            )
        }

        // 3. 隔离初始化：Room 本地数据库单例
        try {
            getRoomDatabase(params.context)
        } catch (e: Throwable) {
            isSuccess = false
            val errorMsg = "数据库初始化失败: ${e.message ?: "未知异常"}"
            errorMessages.add(errorMsg)
            AppAnalyticsManager.trackEvent(
                AnalyticsEvents.INIT_SUB_ERROR,
                mapOf(
                    AnalyticsParams.SUB_MODULE to "database",
                    AnalyticsParams.ERROR_MSG to errorMsg
                )
            )
        }

        // 4. 上报冷启动/全链条初始化总结事件
        val duration = currentTimeMillis() - startTime
        AppAnalyticsManager.trackEvent(
            AnalyticsEvents.APP_LAUNCH,
            buildMap {
                put(AnalyticsParams.IS_SUCCESS, isSuccess)
                put(AnalyticsParams.DURATION_MS, duration)
                if (!isSuccess && errorMessages.isNotEmpty()) {
                    put(AnalyticsParams.ERROR_MSG, errorMessages.joinToString("; "))
                }
            }
        )

        // 5. 后台并发异步拉取与下载全量模块 SDUI 热更 JSON 布局（隔离保护与细分报错上报）
        initScope.launch {
            prefetchHotUpdateLayouts()
        }
    }

    /**
     * 后台异步并发拉取与更新 SDUI 热更 JSON 布局
     */
    suspend fun prefetchHotUpdateLayouts(modules: List<String> = listOf("airbnb", "feedline", "instagram")) {
        val repository = SduiLayoutRepositoryImpl.Instance
        modules.forEach { module ->
            try {
                repository.fetchLayoutFromNetwork(module)
            } catch (e: Exception) {
                AppAnalyticsManager.trackEvent(
                    AnalyticsEvents.INIT_SUB_ERROR,
                    mapOf(
                        AnalyticsParams.SUB_MODULE to "sdui_prefetch",
                        AnalyticsParams.MODULE_NAME to module,
                        AnalyticsParams.ERROR_MSG to (e.message ?: "SDUI布局 [$module] 拉取失败")
                    )
                )
            }
        }
    }
}
