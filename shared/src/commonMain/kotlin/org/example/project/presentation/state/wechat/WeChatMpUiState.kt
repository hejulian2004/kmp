/**
 * @File: WeChatMpUiState.kt
 * @Package: org.example.project.presentation.state.wechat
 * @Description: 微信公众号MVI全局UI状态定义
 * @Author: 何聚敛
 * @Date: 2026-08-18
 */
package org.example.project.presentation.state.wechat

import org.example.project.domain.model.wechat.WeChatAccount
import org.example.project.domain.model.wechat.WeChatArticle
import org.example.project.presentation.state.RefreshState
import org.example.project.presentation.state.UiState

/**
 * 微信公众号页面UI状态模型
 *
 * @param accountsState常读公众号列表加载状态
 * @param featuredState置顶常读推文大卡片状态
 * @param waterfallState瀑布流文章列表状态
 * @param isRefreshing下拉刷新中标志
 * @param isLoadingMore上拉加载更多中标志
 * @param dislikeTargetArticle当前正在操作不感兴趣的目标文章 (为null则不展示浮层)
 */
data class WeChatMpUiState(
    val accountsState: UiState<List<WeChatAccount>> = UiState.Loading,
    val featuredState: UiState<WeChatArticle?> = UiState.Loading,
    val waterfallState: UiState<List<WeChatArticle>> = UiState.Loading,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val dislikeTargetArticle: WeChatArticle? = null
) {
    val frequentlyReadAccounts: List<WeChatAccount>
        get() = (accountsState as? UiState.Success)?.data ?: emptyList()

    val featuredArticle: WeChatArticle?
        get() = (featuredState as? UiState.Success)?.data

    val waterfallArticles: List<WeChatArticle>
        get() = (waterfallState as? UiState.Success)?.data ?: emptyList()
}
