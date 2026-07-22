/**
 * @File: AnalyticsTracker.kt
 * @Package: org.example.project.domain.analytics.feedline
 * @Description: 数据埋点追踪器接口定义
 * @Author: 何聚敛
 * @Date: 2026-07-20
 */
package org.example.project.domain.analytics.feedline

interface AnalyticsTracker {
    /**
     * 上报事件
     * @param eventName 事件名称
     * @param params 附加参数键值对
     */
    fun trackEvent(eventName: String, params: Map<String, Any> = emptyMap())
}

/**
 * 埋点事件名称常量定义
 */
object AnalyticsEvents {
    const val OPEN_FEED = "open_feed"             // 打开朋友圈
    const val REFRESH_FEED = "refresh_feed"       // 刷新数据
    const val CREATE_POST = "create_post"         // 发布朋友圈动态
    const val DELETE_POST = "delete_post"         // 删除动态
    const val LIKE_POST = "like_post"             // 点赞
    const val UNLIKE_POST = "unlike_post"         // 取消点赞
    const val ADD_COMMENT = "add_comment"         // 添加评论
    const val DELETE_COMMENT = "delete_comment"   // 删除评论
    const val ENTER_SCREEN = "enter_screen"       // 进入页面
}

/**
 * 埋点事件参数名常量定义
 */
object AnalyticsParams {
    const val POST_ID = "post_id"                 // 动态ID
    const val USER_ID = "user_id"                 // 用户ID
    const val COMMENT_ID = "comment_id"           // 评论ID
    const val SCREEN_NAME = "screen_name"         // 页面名称
    const val MEDIA_COUNT = "media_count"         // 包含媒体（图片/视频）的数量
    const val HAS_TEXT = "has_text"               // 是否包含文字内容
}



