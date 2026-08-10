/**
 * @File: AnalyticsTracker.kt
 * @Package: org.example.project.domain.analytics.feedline
 * @Description: 旧版本埋点接口向全局 core.analytics 模块的兼容桥接定义
 * @Author: 何聚敛
 * @Date: 2026-08-10
 */
package org.example.project.domain.analytics.feedline

import org.example.project.core.analytics.AnalyticsEvents as CoreEvents
import org.example.project.core.analytics.AnalyticsParams as CoreParams
import org.example.project.core.analytics.AnalyticsTracker as CoreTracker

typealias AnalyticsTracker = CoreTracker

object AnalyticsEvents {
    const val OPEN_FEED = CoreEvents.FEED_OPEN
    const val REFRESH_FEED = CoreEvents.FEED_REFRESH
    const val CREATE_POST = CoreEvents.POST_CREATE
    const val DELETE_POST = CoreEvents.POST_DELETE
    const val LIKE_POST = CoreEvents.POST_LIKE
    const val UNLIKE_POST = CoreEvents.POST_UNLIKE
    const val ADD_COMMENT = CoreEvents.COMMENT_ADD
    const val DELETE_COMMENT = CoreEvents.COMMENT_DELETE
    const val ENTER_SCREEN = CoreEvents.ENTER_SCREEN
}

object AnalyticsParams {
    const val POST_ID = CoreParams.POST_ID
    const val USER_ID = CoreParams.USER_ID
    const val COMMENT_ID = CoreParams.COMMENT_ID
    const val SCREEN_NAME = CoreParams.SCREEN_NAME
    const val MEDIA_COUNT = CoreParams.MEDIA_COUNT
    const val HAS_TEXT = CoreParams.HAS_TEXT
}
