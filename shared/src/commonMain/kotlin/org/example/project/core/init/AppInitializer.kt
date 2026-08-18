/**
 * @File: AppInitializer.kt
 * @Package: org.example.project.core.init
 * @Description: 项目全局启动统一初始化器 (appInit)，具备可恢复初始化状态机与并发安全保障
 * @Author: 何聚敛
 * @Date: 2026-08-12
 */
package org.example.project.core.init

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.example.project.core.analytics.AnalyticsConfig
import org.example.project.core.analytics.AnalyticsEvents
import org.example.project.core.analytics.AnalyticsParams
import org.example.project.core.analytics.AppAnalyticsManager
import org.example.project.core.analytics.LogAnalyticsTracker
import org.example.project.core.database.AppDatabaseInitializer
import org.example.project.core.network.client.AppNetworkInitializer
import org.example.project.core.sdui.repository.SduiLayoutRepositoryImpl
import org.example.project.core.storage.client.AppStorageInitializer
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
 * 初始化状态枚举
 */
enum class AppInitState {
    NOT_INITIALIZED,
    INITIALIZING,
    INITIALIZED,
    FAILED
}

/**
 * 初始化失败模块详情
 */
data class AppInitFailure(
    val module: String,
    val message: String
)

/**
 * 初始化结果模型
 */
data class AppInitResult(
    val success: Boolean,
    val state: AppInitState,
    val failures: List<AppInitFailure>
)

/**
 * 全局应用启动统一初始化器 (AppInitializer / appInit)
 */
object AppInitializer {
    private var _state = AppInitState.NOT_INITIALIZED
    private val stateMutex = Mutex()
    private val initScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /**
     * 当前初始化状态
     */
    val state: AppInitState
        get() = _state

    /**
     * 判断是否已完成初始化
     */
    val isInitialized: Boolean
        get() = _state == AppInitState.INITIALIZED

    /**
     * 统一执行应用冷启动依赖链初始化（具备线程安全、错误捕获与状态恢复能力）。
     * 
     * @param params 应用启动初始化参数
     * @return AppInitResult 包含是否成功、当前状态及失败模块列表
     */
    suspend fun init(params: AppInitParams): AppInitResult {
        return stateMutex.withLock {
            if (_state == AppInitState.INITIALIZED) {
                return@withLock AppInitResult(
                    success = true,
                    state = AppInitState.INITIALIZED,
                    failures = emptyList()
                )
            }

            _state = AppInitState.INITIALIZING
            val failures = mutableListOf<AppInitFailure>()
            val startTime = currentTimeMillis()

            // 1. 数据埋点单例 (零依赖基础设施)
            if (!AppAnalyticsManager.isInitialized) {
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
                } catch (e: Throwable) {
                    println("[AppInitializer 错误] 数据埋点单例初始化失败: ${e.message}")
                }
            }

            // 2. 核心基础设施：文件存储架构
            try {
                AppStorageInitializer.init(params.context)
            } catch (e: Throwable) {
                val errorMsg = "文件存储架构初始化失败: ${e.message ?: "未知异常"}"
                failures.add(AppInitFailure("storage", errorMsg))
                runCatching {
                    AppAnalyticsManager.trackEvent(
                        AnalyticsEvents.INIT_SUB_ERROR,
                        mapOf(
                            AnalyticsParams.SUB_MODULE to "storage",
                            AnalyticsParams.ERROR_MSG to errorMsg
                        )
                    )
                }
            }

            // 3. 网络架构单例
            try {
                AppNetworkInitializer.init(params.context)
            } catch (e: Throwable) {
                val errorMsg = "网络层初始化失败: ${e.message ?: "未知异常"}"
                failures.add(AppInitFailure("network", errorMsg))
                runCatching {
                    AppAnalyticsManager.trackEvent(
                        AnalyticsEvents.INIT_SUB_ERROR,
                        mapOf(
                            AnalyticsParams.SUB_MODULE to "network",
                            AnalyticsParams.ERROR_MSG to errorMsg
                        )
                    )
                }
            }

            // 4. 核心基础设施：Room 本地数据库
            try {
                AppDatabaseInitializer.init(params.context)
            } catch (e: Throwable) {
                val errorMsg = "数据库初始化失败: ${e.message ?: "未知异常"}"
                failures.add(AppInitFailure("database", errorMsg))
                runCatching {
                    AppAnalyticsManager.trackEvent(
                        AnalyticsEvents.INIT_SUB_ERROR,
                        mapOf(
                            AnalyticsParams.SUB_MODULE to "database",
                            AnalyticsParams.ERROR_MSG to errorMsg
                        )
                    )
                }
            }

            val isSuccess = failures.isEmpty()
            val duration = currentTimeMillis() - startTime

            runCatching {
                AppAnalyticsManager.trackEvent(
                    AnalyticsEvents.APP_LAUNCH,
                    buildMap {
                        put(AnalyticsParams.IS_SUCCESS, isSuccess)
                        put(AnalyticsParams.DURATION_MS, duration)
                        if (!isSuccess && failures.isNotEmpty()) {
                            put(AnalyticsParams.ERROR_MSG, failures.joinToString("; ") { "${it.module}: ${it.message}" })
                        }
                    }
                )
            }

            if (isSuccess) {
                _state = AppInitState.INITIALIZED
                initScope.launch {
                    prefetchHotUpdateLayouts()
                }
                AppInitResult(
                    success = true,
                    state = AppInitState.INITIALIZED,
                    failures = emptyList()
                )
            } else {
                _state = AppInitState.FAILED
                AppInitResult(
                    success = false,
                    state = AppInitState.FAILED,
                    failures = failures
                )
            }
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
                runCatching {
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

    /**
     * 仅供单元测试重置初始化状态使用
     */
    internal fun resetForTesting() {
        _state = AppInitState.NOT_INITIALIZED
    }
}
