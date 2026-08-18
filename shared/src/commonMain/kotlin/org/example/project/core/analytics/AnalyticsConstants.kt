/**
 * @File: AnalyticsConstants.kt
 * @Package: org.example.project.core.analytics
 * @Description: 全局数据埋点事件与参数统一定义文件，包含所有业务模块的事件常量与参数键名
 * @Author: 何聚敛
 * @Date: 2026-08-11
 */
package org.example.project.core.analytics

/**
 * 全局业务模块及其专有埋点常量层级定义 (AnalyticsModules)
 */
object AnalyticsModules {
    // 通用模块标识别名
    const val FEEDLINE = FeedLine.ID
    const val INSTAGRAM = Instagram.ID
    const val AIRBNB = Airbnb.ID
    const val WECHAT_MP = WeChatMp.ID

    /**
     * WeChatMp (微信公众号) 模块埋点常量组
     */
    object WeChatMp {
        const val ID = "wechat_mp"

        object Events {
            const val MP_OPEN = "wechat_mp_open"
            const val MP_REFRESH = "wechat_mp_refresh"
            const val MP_LOAD_MORE = "wechat_mp_load_more"
            const val ARTICLE_CLICK = "wechat_article_click"
            const val ACCOUNT_CLICK = "wechat_account_click"
            const val DISLIKE_CLICK = "wechat_dislike_click"
            const val DISLIKE_SUBMIT = "wechat_dislike_submit"
            const val SEARCH_CLICK = "wechat_search_click"
            const val PROFILE_CLICK = "wechat_profile_click"
        }
    }

    /**
     * FeedLine (朋友圈) 模块埋点常量组
     */
    object FeedLine {
        const val ID = "feedline"

        object Events {
            const val OPEN_FEED = "feed_open"
            const val REFRESH_FEED = "feed_refresh"
            const val LOAD_MORE = "feed_load_more"
            const val POST_CREATE = "post_create"
            const val POST_DELETE = "post_delete"
            const val POST_LIKE = "post_like"
            const val POST_UNLIKE = "post_unlike"
            const val ADD_COMMENT = "comment_add"
            const val DELETE_COMMENT = "comment_delete"
            const val MEDIA_PREVIEW = "media_preview"
            const val USER_PROFILE_VIEW = "user_profile_view"
            const val NOTIFICATION_BAR_CLICK = "notification_bar_click"
            const val NOTIFICATION_CLEAR = "notification_clear"
            const val POST_TEXT_ONLY_ENTER = "post_text_only_enter"
            const val MEDIA_SELECT = "media_select"
            const val POST_CANCEL = "post_cancel"
        }
    }

    /**
     * Instagram (图文社群) 模块埋点常量组
     */
    object Instagram {
        const val ID = "instagram"

        object Events {
            const val HOME_OPEN = "insta_home_open"
            const val STORY_VIEW = "insta_story_view"
            const val POST_BOOKMARK = "insta_post_bookmark"
            const val POST_UNBOOKMARK = "insta_post_unbookmark"
            const val SHARE_CLICK = "insta_share_click"
            const val PROFILE_VIEW = "insta_profile_view"
        }
    }

    /**
     * Airbnb (民宿预订) 模块埋点常量组
     */
    object Airbnb {
        const val ID = "airbnb"

        object Events {
            const val SEARCH = "airbnb_search"
            const val FILTER_CHANGE = "airbnb_filter_change"
            const val LISTING_DETAIL = "airbnb_listing_detail"
            const val WISHLIST_TOGGLE = "airbnb_wishlist_toggle"
            const val BOOKING_SUBMIT = "airbnb_booking_submit"
        }
    }
}

/**
 * 全局埋点事件名称规范定义 (AnalyticsEvents)
 * 兼容平铺层级调用与统一映射
 */
object AnalyticsEvents {

    // ==========================================
    // 1. 应用生命周期与全局导航事件
    // ==========================================
    const val APP_LAUNCH = "app_launch"                 // 应用冷启动
    const val APP_FOREGROUND = "app_foreground"         // 应用切回前台
    const val APP_BACKGROUND = "app_background"         // 应用退至后台
    const val ENTER_SCREEN = "enter_screen"             // 进入/展示页面 (屏幕曝光)
    const val LEAVE_SCREEN = "leave_screen"             // 离开页面

    // ==========================================
    // 2. FeedLine (微信朋友圈) 模块事件
    // ==========================================
    const val FEED_OPEN = AnalyticsModules.FeedLine.Events.OPEN_FEED
    const val FEED_REFRESH = AnalyticsModules.FeedLine.Events.REFRESH_FEED
    const val FEED_LOAD_MORE = AnalyticsModules.FeedLine.Events.LOAD_MORE
    const val POST_CREATE = AnalyticsModules.FeedLine.Events.POST_CREATE
    const val POST_DELETE = AnalyticsModules.FeedLine.Events.POST_DELETE
    const val POST_LIKE = AnalyticsModules.FeedLine.Events.POST_LIKE
    const val POST_UNLIKE = AnalyticsModules.FeedLine.Events.POST_UNLIKE
    const val COMMENT_ADD = AnalyticsModules.FeedLine.Events.ADD_COMMENT
    const val COMMENT_DELETE = AnalyticsModules.FeedLine.Events.DELETE_COMMENT
    const val MEDIA_PREVIEW = AnalyticsModules.FeedLine.Events.MEDIA_PREVIEW
    const val USER_PROFILE_VIEW = AnalyticsModules.FeedLine.Events.USER_PROFILE_VIEW
    const val NOTIFICATION_BAR_CLICK = AnalyticsModules.FeedLine.Events.NOTIFICATION_BAR_CLICK
    const val NOTIFICATION_CLEAR = AnalyticsModules.FeedLine.Events.NOTIFICATION_CLEAR
    const val POST_TEXT_ONLY_ENTER = AnalyticsModules.FeedLine.Events.POST_TEXT_ONLY_ENTER
    const val MEDIA_SELECT = AnalyticsModules.FeedLine.Events.MEDIA_SELECT
    const val POST_CANCEL = AnalyticsModules.FeedLine.Events.POST_CANCEL

    // 兼容别名
    const val OPEN_FEED = FEED_OPEN
    const val REFRESH_FEED = FEED_REFRESH
    const val CREATE_POST = POST_CREATE
    const val DELETE_POST = POST_DELETE
    const val LIKE_POST = POST_LIKE
    const val UNLIKE_POST = POST_UNLIKE
    const val ADD_COMMENT = COMMENT_ADD
    const val DELETE_COMMENT = COMMENT_DELETE

    // ==========================================
    // 3. Instagram (图文社群) 模块事件
    // ==========================================
    const val INSTA_HOME_OPEN = AnalyticsModules.Instagram.Events.HOME_OPEN
    const val INSTA_STORY_VIEW = AnalyticsModules.Instagram.Events.STORY_VIEW
    const val INSTA_POST_BOOKMARK = AnalyticsModules.Instagram.Events.POST_BOOKMARK
    const val INSTA_POST_UNBOOKMARK = AnalyticsModules.Instagram.Events.POST_UNBOOKMARK
    const val INSTA_SHARE_CLICK = AnalyticsModules.Instagram.Events.SHARE_CLICK
    const val INSTA_PROFILE_VIEW = AnalyticsModules.Instagram.Events.PROFILE_VIEW

    // ==========================================
    // 4. Airbnb (民宿预订) 模块事件
    // ==========================================
    const val AIRBNB_SEARCH = AnalyticsModules.Airbnb.Events.SEARCH
    const val AIRBNB_FILTER_CHANGE = AnalyticsModules.Airbnb.Events.FILTER_CHANGE
    const val AIRBNB_LISTING_DETAIL = AnalyticsModules.Airbnb.Events.LISTING_DETAIL
    const val AIRBNB_WISHLIST_TOGGLE = AnalyticsModules.Airbnb.Events.WISHLIST_TOGGLE
    const val AIRBNB_BOOKING_SUBMIT = AnalyticsModules.Airbnb.Events.BOOKING_SUBMIT

    // ==========================================
    // 5. WeChatMp (微信公众号) 模块事件
    // ==========================================
    const val WECHAT_MP_OPEN = AnalyticsModules.WeChatMp.Events.MP_OPEN
    const val WECHAT_MP_REFRESH = AnalyticsModules.WeChatMp.Events.MP_REFRESH
    const val WECHAT_MP_LOAD_MORE = AnalyticsModules.WeChatMp.Events.MP_LOAD_MORE
    const val WECHAT_ARTICLE_CLICK = AnalyticsModules.WeChatMp.Events.ARTICLE_CLICK
    const val WECHAT_ACCOUNT_CLICK = AnalyticsModules.WeChatMp.Events.ACCOUNT_CLICK
    const val WECHAT_DISLIKE_CLICK = AnalyticsModules.WeChatMp.Events.DISLIKE_CLICK
    const val WECHAT_DISLIKE_SUBMIT = AnalyticsModules.WeChatMp.Events.DISLIKE_SUBMIT
    const val WECHAT_SEARCH_CLICK = AnalyticsModules.WeChatMp.Events.SEARCH_CLICK
    const val WECHAT_PROFILE_CLICK = AnalyticsModules.WeChatMp.Events.PROFILE_CLICK

    // ==========================================
    // 6. 系统性能与网络监控事件
    // ==========================================
    const val NETWORK_ERROR = "network_error"           // API 网络请求失败
    const val SDUI_RENDER_ERROR = "sdui_render_error"   // SDUI 动态组件渲染失败
    const val PAGE_RENDER_TIME = "page_render_time"     // 页面加载首屏渲染耗时
    const val INIT_SUB_ERROR = "init_sub_error"         // 初始化过程中细分子模块报错
}

/**
 * 全局埋点参数键名规范定义 (AnalyticsParams)
 */
object AnalyticsParams {
    // 通用环境与路由参数
    const val SCREEN_NAME = "screen_name"               // 页面/屏幕名称
    const val MODULE_NAME = "module_name"               // 业务模块标识 (feedline / instagram / airbnb)
    const val SUB_MODULE = "sub_module"                 // 细分子模块标识 (analytics / network / database / sdui_prefetch)
    const val USER_ID = "user_id"                       // 用户唯一ID
    const val TARGET_USER_ID = "target_user_id"         // 目标用户ID
    const val CLICK_SOURCE = "click_source"             // 点击来源组件
    const val DURATION_MS = "duration_ms"               // 停留时长/耗时(毫秒)

    // 内容与业务实体参数
    const val POST_ID = "post_id"                       // 动态/帖文ID
    const val ARTICLE_ID = "article_id"                 // 公众号文章ID
    const val ACCOUNT_ID = "account_id"                 // 公众号ID
    const val DISLIKE_REASON = "dislike_reason"         // 不感兴趣原因
    const val COMMENT_ID = "comment_id"                 // 评论ID
    const val LISTING_ID = "listing_id"                 // 房源ID
    const val MEDIA_COUNT = "media_count"               // 包含媒体(图片/视频)数量
    const val HAS_TEXT = "has_text"                     // 是否包含文字内容
    const val UNREAD_COUNT = "unread_count"             // 未读通知数量
    const val CLEARED_COUNT = "cleared_count"           // 清空数量
    const val SOURCE_TYPE = "source_type"               // 媒体来源类型
    const val HAS_CONTENT = "has_content"               // 是否包含草稿内容

    // 搜索与筛选参数
    const val SEARCH_KEYWORD = "search_keyword"         // 搜索关键词
    const val FILTER_PARAMS = "filter_params"           // 筛选过滤条件

    // 异常监控与结果标志参数
    const val IS_SUCCESS = "is_success"                 // 操作/初始化是否成功 (Boolean)
    const val ERROR_CODE = "error_code"                 // 错误状态码
    const val ERROR_MSG = "error_msg"                   // 错误堆栈/简记信息
    const val SDUI_TEMPLATE_ID = "sdui_template_id"     // SDUI 模板ID
}
