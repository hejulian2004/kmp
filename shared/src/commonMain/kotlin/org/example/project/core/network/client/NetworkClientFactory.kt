/**
 * @File: NetworkClientFactory.kt
 * @Package: org.example.project.core.network.client
 * @Description: HttpClient客户端工厂接口契约
 * @Author: 何聚敛
 * @Date: 2026-07-30
 */
package org.example.project.core.network.client

import io.ktor.client.HttpClient

/**
 * [NetworkClientFactory]
 * 负责定义生产四种职责分离HttpClient的工厂契约接口。
 */
interface NetworkClientFactory {
    /** 创建无需鉴权的公共客户端 (PublicClient) */
    fun createPublicClient(): HttpClient

    /** 创建带 Bearer Token 自动注入与单飞刷新的鉴权客户端 (AuthorizedClient) */
    fun createAuthorizedClient(): HttpClient

    /** 创建适合文件上传的分片/独立超时客户端 (UploadClient) */
    fun createUploadClient(): HttpClient?

    /** 创建 WebSocket 长连接信令客户端 (RealtimeClient) */
    fun createRealtimeClient(): HttpClient?
}
