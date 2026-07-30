/**
 * @File: NetworkContainer.kt
 * @Package: org.example.project.core.network.client
 * @Description: 全局网络依赖容器接口契约
 * @Author: 何聚敛
 * @Date: 2026-07-30
 */
package org.example.project.core.network.client

import io.ktor.client.HttpClient

/**
 * [NetworkContainer]
 * 管理全局 HttpClient 实例生命周期的依赖容器契约。
 */
interface NetworkContainer {
    /** 未鉴权公共客户端 */
    val publicClient: HttpClient

    /** Bearer Token 鉴权客户端 */
    val authorizedClient: HttpClient

    /** 独立上传客户端 (可选) */
    val uploadClient: HttpClient?

    /** WebSocket 长连接客户端 (可选) */
    val realtimeClient: HttpClient?

    /** 关闭容器并释放所有底层引擎与连接池资源 */
    fun close()
}
