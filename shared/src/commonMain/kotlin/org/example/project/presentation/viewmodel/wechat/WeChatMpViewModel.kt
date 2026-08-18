/**
 * @File: WeChatMpViewModel.kt
 * @Package: org.example.project.presentation.viewmodel.wechat
 * @Description: 微信公众号MVI核心视图模型，负责状态管理与事件处理
 * @Author: 何聚敛
 * @Date: 2026-08-18
 */
package org.example.project.presentation.viewmodel.wechat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.core.analytics.AnalyticsEvents
import org.example.project.core.analytics.AnalyticsModules
import org.example.project.core.analytics.AnalyticsParams
import org.example.project.core.analytics.AppAnalyticsManager
import org.example.project.core.database.AppDatabaseInitializer
import org.example.project.core.network.client.AppNetworkInitializer
import org.example.project.data.repository.wechat.WeChatMpRepositoryImpl
import org.example.project.domain.model.wechat.WeChatAccount
import org.example.project.domain.model.wechat.WeChatArticle
import org.example.project.domain.repository.wechat.WeChatMpRepository
import org.example.project.presentation.effect.wechat.WeChatMpEffect
import org.example.project.presentation.intent.wechat.WeChatMpIntent
import org.example.project.presentation.state.RefreshState
import org.example.project.presentation.state.UiState
import org.example.project.presentation.state.wechat.WeChatMpUiState

/**
 * 微信公众号MVI视图模型
 *
 * @param repository数据仓库契约
 */
class WeChatMpViewModel(
    private val repository: WeChatMpRepository = WeChatMpRepositoryImpl(
        weChatMpDao = AppDatabaseInitializer.database.weChatMpDao(),
        networkContainer = AppNetworkInitializer.container
    )
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeChatMpUiState())
    val uiState: StateFlow<WeChatMpUiState> = _uiState.asStateFlow()

    private val _effect = Channel<WeChatMpEffect>(Channel.BUFFERED)
    val effect: Flow<WeChatMpEffect> = _effect.receiveAsFlow()

    init {
        observeData()
    }

    /**
     * 统一意图分发入口
     */
    fun handleIntent(intent: WeChatMpIntent) {
        when (intent) {
            WeChatMpIntent.Refresh -> refresh()
            WeChatMpIntent.LoadMore -> loadMore()
            is WeChatMpIntent.ClickArticle -> onArticleClicked(intent.article)
            is WeChatMpIntent.ClickAccount -> onAccountClicked(intent.account)
            is WeChatMpIntent.RequestDislike -> requestDislike(intent.article)
            is WeChatMpIntent.SubmitDislike -> submitDislike(intent.articleId, intent.reason)
            WeChatMpIntent.DismissDislike -> dismissDislike()
            WeChatMpIntent.ClickSearch -> onSearchClicked()
            WeChatMpIntent.ClickProfile -> onProfileClicked()
            is WeChatMpIntent.ToggleFollow -> toggleFollow(intent.accountId)
        }
    }

    private fun observeData() {
        viewModelScope.launch {
            repository.observeFrequentlyReadAccounts()
                .catch { e ->
                    _uiState.update { it.copy(accountsState = UiState.Error(e.message ?: "Load accounts failed")) }
                }
                .collect { list ->
                    _uiState.update { it.copy(accountsState = UiState.Success(list)) }
                }
        }

        viewModelScope.launch {
            repository.observeFeaturedArticle()
                .catch { e ->
                    _uiState.update { it.copy(featuredState = UiState.Error(e.message ?: "Load featured article failed")) }
                }
                .collect { article ->
                    _uiState.update { it.copy(featuredState = UiState.Success(article)) }
                }
        }

        viewModelScope.launch {
            repository.observeWaterfallArticles()
                .catch { e ->
                    _uiState.update { it.copy(waterfallState = UiState.Error(e.message ?: "Load feed failed")) }
                }
                .collect { list ->
                    _uiState.update { it.copy(waterfallState = UiState.Success(list)) }
                }
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            val result = repository.refreshData()
            _uiState.update { it.copy(isRefreshing = false) }

            AppAnalyticsManager.trackEvent(
                AnalyticsEvents.WECHAT_MP_REFRESH,
                mapOf(
                    AnalyticsParams.MODULE_NAME to AnalyticsModules.WECHAT_MP,
                    AnalyticsParams.IS_SUCCESS to result.isSuccess,
                    AnalyticsParams.ERROR_MSG to (result.exceptionOrNull()?.message ?: "")
                )
            )

            if (result.isSuccess) {
                _effect.send(WeChatMpEffect.ShowToast("已更新公众号与看一看动态"))
            } else {
                _effect.send(WeChatMpEffect.ShowToast("刷新失败，请检查网络"))
            }
        }
    }

    private fun loadMore() {
        if (_uiState.value.isLoadingMore) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true) }
            val result = repository.loadMoreArticles()
            _uiState.update { it.copy(isLoadingMore = false) }

            AppAnalyticsManager.trackEvent(
                AnalyticsEvents.WECHAT_MP_LOAD_MORE,
                mapOf(
                    AnalyticsParams.MODULE_NAME to AnalyticsModules.WECHAT_MP,
                    AnalyticsParams.IS_SUCCESS to result.isSuccess
                )
            )
        }
    }

    private fun onArticleClicked(article: WeChatArticle) {
        viewModelScope.launch {
            AppAnalyticsManager.trackEvent(
                AnalyticsEvents.WECHAT_ARTICLE_CLICK,
                mapOf(
                    AnalyticsParams.MODULE_NAME to AnalyticsModules.WECHAT_MP,
                    AnalyticsParams.ARTICLE_ID to article.id,
                    AnalyticsParams.ACCOUNT_ID to article.account.id,
                    AnalyticsParams.IS_SUCCESS to true
                )
            )
            _effect.send(WeChatMpEffect.OpenArticle(article.id, article.title))
        }
    }

    private fun onAccountClicked(account: WeChatAccount) {
        viewModelScope.launch {
            if (account.hasUnread) {
                repository.markAccountAsRead(account.id)
            }
            AppAnalyticsManager.trackEvent(
                AnalyticsEvents.WECHAT_ACCOUNT_CLICK,
                mapOf(
                    AnalyticsParams.MODULE_NAME to AnalyticsModules.WECHAT_MP,
                    AnalyticsParams.ACCOUNT_ID to account.id,
                    AnalyticsParams.IS_SUCCESS to true
                )
            )
            _effect.send(WeChatMpEffect.ShowToast("已打开公众号: ${account.name}"))
        }
    }

    private fun requestDislike(article: WeChatArticle) {
        AppAnalyticsManager.trackEvent(
            AnalyticsEvents.WECHAT_DISLIKE_CLICK,
            mapOf(
                AnalyticsParams.MODULE_NAME to AnalyticsModules.WECHAT_MP,
                AnalyticsParams.ARTICLE_ID to article.id,
                AnalyticsParams.IS_SUCCESS to true
            )
        )
        _uiState.update { it.copy(dislikeTargetArticle = article) }
    }

    private fun submitDislike(articleId: String, reason: String) {
        viewModelScope.launch {
            val result = repository.dislikeArticle(articleId, reason)
            _uiState.update { it.copy(dislikeTargetArticle = null) }

            AppAnalyticsManager.trackEvent(
                AnalyticsEvents.WECHAT_DISLIKE_SUBMIT,
                mapOf(
                    AnalyticsParams.MODULE_NAME to AnalyticsModules.WECHAT_MP,
                    AnalyticsParams.ARTICLE_ID to articleId,
                    AnalyticsParams.DISLIKE_REASON to reason,
                    AnalyticsParams.IS_SUCCESS to result.isSuccess
                )
            )

            if (result.isSuccess) {
                _effect.send(WeChatMpEffect.ShowToast("已减少类似内容推荐"))
            }
        }
    }

    private fun dismissDislike() {
        _uiState.update { it.copy(dislikeTargetArticle = null) }
    }

    private fun onSearchClicked() {
        viewModelScope.launch {
            AppAnalyticsManager.trackEvent(
                AnalyticsEvents.WECHAT_SEARCH_CLICK,
                mapOf(
                    AnalyticsParams.MODULE_NAME to AnalyticsModules.WECHAT_MP,
                    AnalyticsParams.IS_SUCCESS to true
                )
            )
            _effect.send(WeChatMpEffect.ShowToast("搜索公众号与文章"))
        }
    }

    private fun onProfileClicked() {
        viewModelScope.launch {
            AppAnalyticsManager.trackEvent(
                AnalyticsEvents.WECHAT_PROFILE_CLICK,
                mapOf(
                    AnalyticsParams.MODULE_NAME to AnalyticsModules.WECHAT_MP,
                    AnalyticsParams.IS_SUCCESS to true
                )
            )
            _effect.send(WeChatMpEffect.ShowToast("进入订阅号管理"))
        }
    }

    private fun toggleFollow(accountId: String) {
        viewModelScope.launch {
            repository.toggleFollowAccount(accountId)
        }
    }
}
