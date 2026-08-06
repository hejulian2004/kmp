/**
 * @File: DevMockEngine.kt
 * @Package: org.example.project.core.network.config
 * @Description: 无后端接口开发阶段使用的全局MockEngine虚拟网络引擎
 * @Author: 何聚敛
 * @Date: 2026-07-30
 */
package org.example.project.core.network.config

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.serialization.encodeToString
import org.example.project.core.network.config.createFakeFeedPosts

/**
 * [createDevMockEngine]
 * 创建在无后端真实接口开发阶段默认返回200 OK假数据的MockEngine。
 * 
 * @return返回配置好虚拟响应规则的MockEngine实例
 */
fun createDevMockEngine(): MockEngine {
    return MockEngine { request ->
        val urlPath = request.url.encodedPath
        when (urlPath) {
            ApiEndpoints.Auth.LOGIN, ApiEndpoints.Auth.REFRESH -> {
                respond(
                    content = """{
                        "accessToken": "mock_access_token_dev_88888",
                        "refreshToken": "mock_refresh_token_dev_99999"
                    }""".trimIndent(),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            }
            ApiEndpoints.FeedLine.GET_POSTS -> {
                val fakeJson = CommonNetworkConfig.defaultJson.encodeToString(createFakeFeedPosts())
                respond(
                    content = fakeJson,
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            }
            else -> {
                respond(
                    content = """{
                        "code": 200,
                        "message": "Mock开发环境模拟成功响应",
                        "data": null
                    }""".trimIndent(),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            }
        }
    }
}
