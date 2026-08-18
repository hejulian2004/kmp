/**
 * @File: AppNetworkInitializer.kt
 * @Package: org.example.project.core.network.client
 * @Description: KMP全局网络架构单例初始化与依赖组装助手
 * @Author: 何聚敛
 * @Date: 2026-08-18
 */
package org.example.project.core.network.client

import kotlin.concurrent.Volatile
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.example.project.core.network.auth.TokenRefresher
import org.example.project.core.network.auth.TokenStore
import org.example.project.core.network.auth.createPlatformSecureStorage

/**
 * [AppNetworkInitializer]
 * 全局网络层单例初始化器。
 * 负责在各平台启动时组装原生SecureStorage、TokenStore、TokenRefresher及NetworkContainer。
 */
object AppNetworkInitializer {
    @Volatile
    private var _container: NetworkContainer? = null
    private val initMutex = Mutex()

    /**
     * 获取全局唯一 [NetworkContainer] 实例。
     * 若未事先显式调用 [init]，将抛出异常。
     *
     * @throws IllegalStateException 网络层尚未显式初始化时抛出异常。
     */
    val container: NetworkContainer
        get() = checkNotNull(_container) {
            "Network尚未初始化！必须在应用冷启动阶段显式调用AppNetworkInitializer.init(context)方可使用。"
        }

    /**
     * 判断网络架构是否已完成初始化。
     */
    val isInitialized: Boolean
        get() = _container != null

    /**
     * 显式初始化网络架构单例。
     *
     * @param context 平台相关Context(Android需传入ApplicationContext，iOS传入null)
     */
    suspend fun init(context: Any? = null) {
        if (_container != null) return
        initMutex.withLock {
            if (_container != null) return@withLock
            _container = createDefaultContainer(context)
        }
    }

    /**
     * 仅供单元测试重置初始化状态使用。
     */
    internal fun resetForTesting() {
        _container = null
    }

    private fun createDefaultContainer(context: Any?): NetworkContainer {
        val secureStorage = createPlatformSecureStorage(context)
        val tokenStore = TokenStore(secureStorage)
        val tokenRefresher = TokenRefresher(tokenStore)
        val factory = NetworkClientFactoryImpl(
            tokenStore = tokenStore,
            tokenRefresher = tokenRefresher
        )
        return DefaultNetworkContainer(factory)
    }
}
