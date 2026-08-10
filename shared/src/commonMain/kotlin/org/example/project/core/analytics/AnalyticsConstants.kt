/**
 * @File: AnalyticsConstants.kt
 * @Package: org.example.project.core.analytics
 * @Description: 全局数据埋点事件与参数统一定义文件，包含所有业务模块的事件常量与参数键名
 * @Author: 何聚敛
 * @Date: 2026-08-10
 */
package org.example.project.core.analytics

/**
 * 全局埋点事件名称规范定义 (AnalyticsEvents)
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
    const val FEED_OPEN = "feed_open"                   // 打开朋友圈主页
    const val FEED_REFRESH = "feed_refresh"             // 下拉刷新动态列表
    const val FEED_LOAD_MORE = "feed_load_more"         // 上拉加载更多
    const val POST_CREATE = "post_create"               // 发布新动态
    const val POST_DELETE = "post_delete"               // 删除个人动态
    const val POST_LIKE = "post_like"                   // 点赞动态
    const val POST_UNLIKE = "post_unlike"               // 取消点赞
    const val COMMENT_ADD = "comment_add"               // 发表评论
    const val COMMENT_DELETE = "comment_delete"         // 删除评论
    const val MEDIA_PREVIEW = "media_preview"           // 点击预览动态大图/视频

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
    const val INSTA_HOME_OPEN = "insta_home_open"       // 打开 Instagram 首页
    const val INSTA_STORY_VIEW = "insta_story_view"     // 播放 Story 快拍
    const val INSTA_POST_BOOKMARK = "insta_post_bookmark" // 收藏帖文
    const val INSTA_POST_UNBOOKMARK = "insta_post_unbookmark" // 取消收藏
    const val INSTA_SHARE_CLICK = "insta_share_click"   // 点击分享按钮
    const val INSTA_PROFILE_VIEW = "insta_profile_view" // 查看用户个人主页

    // ==========================================
    // 4. Airbnb (民宿预订) 模块事件
    // ==========================================
    const val AIRBNB_SEARCH = "airbnb_search"           // 执行房源搜索
    const val AIRBNB_FILTER_CHANGE = "airbnb_filter_change" // 改变筛选条件（价格/人数/设施）
    const val AIRBNB_LISTING_DETAIL = "airbnb_listing_detail" // 进入房源详情页
    const val AIRBNB_WISHLIST_TOGGLE = "airbnb_wishlist_toggle" // 心愿单添加/移除
    const val AIRBNB_BOOKING_SUBMIT = "airbnb_booking_submit" // 提交预订订单

    // ==========================================
    // 5. 系统性能与网络监控事件
    // ==========================================
    const val NETWORK_ERROR = "network_error"           // API 网络请求失败
    const val SDUI_RENDER_ERROR = "sdui_render_error"   // SDUI 动态组件渲染失败
    const val PAGE_RENDER_TIME = "page_render_time"     // 页面加载首屏渲染耗时
}

/**
 * 全局埋点参数键名规范定义 (AnalyticsParams)
 */
object AnalyticsParams {
    // 通用环境与路由参数
    const val SCREEN_NAME = "screen_name"               // 页面/屏幕名称
    const val MODULE_NAME = "module_name"               // 业务模块标识 (feedline / instagram / airbnb)
    const val USER_ID = "user_id"                       // 用户唯一ID
    const val DURATION_MS = "duration_ms"               // 停留时长/耗时(毫秒)

    // 内容与业务实体参数
    const val POST_ID = "post_id"                       // 动态/帖文ID
    const val COMMENT_ID = "comment_id"                 // 评论ID
    const val LISTING_ID = "listing_id"                 // 房源ID
    const val MEDIA_COUNT = "media_count"               // 包含媒体(图片/视频)数量
    const val HAS_TEXT = "has_text"                     // 是否包含文字内容

    // 搜索与筛选参数
    const val SEARCH_KEYWORD = "search_keyword"         // 搜索关键词
    const val FILTER_PARAMS = "filter_params"           // 筛选过滤条件

    // 异常监控参数
    const val ERROR_CODE = "error_code"                 // 错误状态码
    const val ERROR_MSG = "error_msg"                   // 错误堆栈/简记信息
    const val SDUI_TEMPLATE_ID = "sdui_template_id"     // SDUI 模板ID
}
