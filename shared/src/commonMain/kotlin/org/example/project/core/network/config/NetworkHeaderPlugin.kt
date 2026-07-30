/**
 * @File: NetworkHeaderPlugin.kt
 * @Package: org.example.project.core.network.config
 * @Description: Ktor自定义Plugin，为每个HTTP请求注入X-Request-Id跟踪ID
 * @Author: 何聚敛
 * @Date: 2026-07-30
 */
package org.example.project.core.network.config

import io.ktor.client.plugins.api.createClientPlugin
import kotlin.random.Random

/**
 * 随机UUID伪生成器（纯KMP无原生依赖）
 */
private fun generateRandomUUID(): String {
    val charPool = "0123456789abcdef"
    fun randomHex(length: Int) = (1..length)
        .map { charPool[Random.nextInt(charPool.length)] }
        .joinToString("")
    return "${randomHex(8)}-${randomHex(4)}-4${randomHex(3)}-a${randomHex(3)}-${randomHex(12)}"
}

/**
 * [RequestIdPlugin]
 * Ktor客户端插件：自动在请求头加入X-Request-Id。
 */
val RequestIdPlugin = createClientPlugin("RequestIdPlugin") {
    onRequest { request, _ ->
        if (!request.headers.contains("X-Request-Id")) {
            request.headers.append("X-Request-Id", generateRandomUUID())
        }
    }
}
