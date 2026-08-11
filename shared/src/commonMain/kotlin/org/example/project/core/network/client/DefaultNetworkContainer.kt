/**
 * @File: DefaultNetworkContainer.kt
 * @Package: org.example.project.core.network.client
 * @Description: NetworkContainer依赖容器的具体实现
 * @Author: 何聚敛
 * @Date: 2026-07-30
 */
package org.example.project.core.network.client

import io.ktor.client.HttpClient

/**
 * [DefaultNetworkContainer]
 * 网络依赖容器实现，懒加载或提前构建各个HttpClient并管理销毁。
 * 
 * @param factory网络客户端工厂
 */
class DefaultNetworkContainer(
    private val factory: NetworkClientFactory
) : NetworkContainer {

    override val publicClient: HttpClient by lazy {
        factory.createPublicClient()
    }

    override val authorizedClient: HttpClient by lazy {
        factory.createAuthorizedClient()
    }

    override val uploadClient: HttpClient? by lazy {
        factory.createUploadClient()
    }

    override val realtimeClient: HttpClient? by lazy {
        factory.createRealtimeClient()
    }

    override fun close() {
        publicClient.close()
        authorizedClient.close()
        uploadClient?.close()
        realtimeClient?.close()
    }
}
