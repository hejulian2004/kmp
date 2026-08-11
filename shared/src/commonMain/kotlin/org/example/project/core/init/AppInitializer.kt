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
     * 统一执行应用冷启动依赖链初始化。
     * 【主线程强同步完成】：1. 网络架构 -> 2. Room 数据库 -> 3. 数据埋点单例 -> 4. 上报冷启动事件；
     * 【后台异步非阻塞】：5. 异步并发拉取与更新全量模块（Airbnb / FeedLine / Instagram）SDUI 热更 JSON。
     * 
     * @param params 应用启动初始化参数
     */
    fun init(params: AppInitParams) {
        if (isInitialized) return
        isInitialized = true

        // 1. 强同步完成：优先初始化全局网络架构单例
        AppNetworkInitializer.init(params.context)

        // 2. 强同步完成：初始化 Room 本地数据库单例（同步准备就绪）
        val database = getRoomDatabase(params.context)

        // 3. 强同步完成：初始化全局数据埋点单例（同步准备就绪）
        AppAnalyticsManager.init(
            AnalyticsConfig(
                platformName = params.platformName,
                appVersion = params.appVersion,
                deviceId = params.deviceId,
                maxLogRetentionCount = params.maxLogRetentionCount,
                trackers = listOf(LogAnalyticsTracker())
            )
        )

        // 4. 强同步完成：上报冷启动埋点事件
        AppAnalyticsManager.trackEvent(AnalyticsEvents.APP_LAUNCH)

        // 5. 后台并发异步拉取与下载全量模块 SDUI 热更 JSON 布局（不阻塞主线程）
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
            } catch (_: Exception) { }
        }
    }
}
