/**
 * @File: WeChatMpEffect.kt
 * @Package: org.example.project.presentation.effect.wechat
 * @Description: 微信公众号MVI一次性副作用事件管道
 * @Author: 何聚敛
 * @Date: 2026-08-18
 */
package org.example.project.presentation.effect.wechat

/**
 * 微信公众号一次性Side Effect事件
 */
sealed interface WeChatMpEffect {
    /** 弹出Toast/Snackbar提示 */
    data class ShowToast(val message: String) : WeChatMpEffect

    /** 打开文章落地页 */
    data class OpenArticle(val articleId: String, val title: String) : WeChatMpEffect

    /** 滚动至列表顶部 */
    data object ScrollToTop : WeChatMpEffect
}
