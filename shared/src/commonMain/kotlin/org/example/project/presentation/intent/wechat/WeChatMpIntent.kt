/**
 * @File: WeChatMpIntent.kt
 * @Package: org.example.project.presentation.intent.wechat
 * @Description: 微信公众号MVI模式用户交互意图集合
 * @Author: 何聚敛
 * @Date: 2026-08-18
 */
package org.example.project.presentation.intent.wechat

import org.example.project.domain.model.wechat.WeChatAccount
import org.example.project.domain.model.wechat.WeChatArticle

/**
 * 微信公众号MVI用户意图接口
 */
sealed interface WeChatMpIntent {
    /** 下拉刷新页面 */
    data object Refresh : WeChatMpIntent

    /** 上拉加载更多 */
    data object LoadMore : WeChatMpIntent

    /** 点击文章卡片 */
    data class ClickArticle(val article: WeChatArticle) : WeChatMpIntent

    /** 点击常读公众号头像 */
    data class ClickAccount(val account: WeChatAccount) : WeChatMpIntent

    /** 点击文章不感兴趣/屏蔽按钮 */
    data class RequestDislike(val article: WeChatArticle) : WeChatMpIntent

    /** 确认提交不感兴趣原因 */
    data class SubmitDislike(val articleId: String, val reason: String) : WeChatMpIntent

    /** 关闭不感兴趣弹窗 */
    data object DismissDislike : WeChatMpIntent

    /** 点击搜索按钮 */
    data object ClickSearch : WeChatMpIntent

    /** 点击个人中心/订阅管理按钮 */
    data object ClickProfile : WeChatMpIntent

    /** 关注/取消关注公众号 */
    data class ToggleFollow(val accountId: String) : WeChatMpIntent
}
