/**
 * @File: AppNetworkInitializer.kt
 * @Package: org.example.project.core.network.client
 * @Description: KMP全局网络架构单例初始化与依赖组装助手
 * @Author: 何聚敛
 * @Date: 2026-07-30
 */
package org.example.project.core.network.client

import org.example.project.core.network.auth.TokenRefresher
import org.example.project.core.network.auth.TokenStore
import org.example.project.core.network.auth.createPlatformSecureStorage

/**
 * [AppNetworkInitializer]
 * 全局网络层单例初始化器。
 * 负责在各平台启动时组装原生SecureStorage、TokenStore、TokenRefresher及NetworkContainer。
 */
object AppNetworkInitializer {
    private var _container: NetworkContainer? = null

    /**
     * 获取全局唯一 [NetworkContainer] 实例。
     * 若未事先显式调用 [init]，将进行默认保底初始化。
     */
    val container: NetworkContainer
        get() {
            var instance = _container
            if (instance == null) {
                instance = createDefaultContainer(null)
                _container = instance
            }
            return instance
        }

    /**
     * 显式初始化网络架构单例。
     * 
     * @param context 平台相关Context(Android需传入ApplicationContext，iOS传入null)
     */
    fun init(context: Any? = null) {
        if (_container == null) {
            _container = createDefaultContainer(context)
        }
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
