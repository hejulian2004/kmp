/**
 * @File: ApiEndpoints.kt
 * @Package: org.example.project.core.network.config
 * @Description: 全局API接口路径与域名集中配置常量
 * @Author: 何聚敛
 * @Date: 2026-07-30
 */
package org.example.project.core.network.config

/**
 * [ApiEndpoints]
 * 全局统一API接口路径与BaseURL配置类。
 */
object ApiEndpoints {
    /** 基础API域名 */
    const val BASE_URL = "https://api.example.com"

    /** 认证模块接口路径 */
    object Auth {
        const val LOGIN = "/api/v1/auth/login"
        const val REFRESH = "/api/v1/auth/refresh"
        const val LOGOUT = "/api/v1/auth/logout"
    }

    /** 朋友圈(FeedLine)模块接口路径 */
    object FeedLine {
        const val GET_POSTS = "/api/v1/feed/posts"
        const val CREATE_POST = "/api/v1/feed/posts"
        const val LIKE_POST = "/api/v1/feed/posts/like"
        const val COMMENT = "/api/v1/feed/posts/comment"
    }

    /** Instagram模块接口路径 */
    object Instagram {
        const val HOME_FEED = "/api/v1/instagram/feed"
        const val STORIES = "/api/v1/instagram/stories"
    }

    /** SDUI动态组件热更模块接口路径 */
    object Sdui {
        const val GET_LAYOUT = "/api/v1/sdui/layout"
    }

    /** Airbnb 模块接口路径 */
    object Airbnb {
        const val GET_HOSTS = "/api/v1/airbnb/hosts"
        const val GET_PROPERTIES = "/api/v1/airbnb/properties"
        const val GET_REVIEWS = "/api/v1/airbnb/reviews"
        const val GET_GUIDES = "/api/v1/airbnb/guides"
    }
}
