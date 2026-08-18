/**
 * @File: WeChatMpScreen.kt
 * @Package: org.example.project.ui.screens.wechat
 * @Description: 微信公众号与看一看瀑布流主界面（相对布局屏幕多状态适配与MVI架构对齐）
 * @Author: 何聚敛
 * @Date: 2026-08-18
 */
package org.example.project.ui.screens.wechat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinproject.composeapp.generated.resources.Res
import kotlinproject.composeapp.generated.resources.wechat_mp_look_around
import org.jetbrains.compose.resources.stringResource
import org.example.project.domain.model.wechat.WeChatCardType
import org.example.project.presentation.effect.wechat.WeChatMpEffect
import org.example.project.presentation.intent.wechat.WeChatMpIntent
import org.example.project.presentation.state.UiState
import org.example.project.presentation.viewmodel.wechat.WeChatMpViewModel
import org.example.project.ui.components.wechat.WeChatMpDislikeBottomSheet
import org.example.project.ui.components.wechat.WeChatMpFeaturedBannerCard
import org.example.project.ui.components.wechat.WeChatMpFrequentlyReadBar
import org.example.project.ui.components.wechat.WeChatMpHorizontalCard
import org.example.project.ui.components.wechat.WeChatMpTopBar
import org.example.project.ui.components.wechat.WeChatMpWaterfallCard
import org.example.project.ui.components.wechat.getMaxAdaptiveContentWidth
import org.example.project.ui.components.wechat.getWaterfallColumnCount
import org.example.project.ui.components.wechat.rememberWeChatWindowSizeClass
import org.example.project.ui.theme.wechat.WeChatBackgroundGray
import org.example.project.ui.theme.wechat.WeChatBrandGreen
import org.example.project.ui.theme.wechat.WeChatTextPrimary
import org.example.project.ui.theme.wechat.WeChatTheme
import org.jetbrains.compose.resources.stringResource

import org.example.project.ui.core.sdui.registry.registerWeChatMpSduiComponents
import org.example.project.core.analytics.AnalyticsModules
import org.example.project.core.analytics.AnalyticsEvents
import org.example.project.core.analytics.AnalyticsParams
import org.example.project.core.analytics.AppAnalyticsManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeChatMpScreen(
    viewModel: WeChatMpViewModel,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val staggeredGridState = rememberLazyStaggeredGridState()

    // 相对布局屏幕状态断点检测
    val windowSizeClass = rememberWeChatWindowSizeClass()
    val columnCount = getWaterfallColumnCount(windowSizeClass)
    val maxContentWidth = getMaxAdaptiveContentWidth(windowSizeClass)

    LaunchedEffect(Unit) {
        registerWeChatMpSduiComponents()
        AppAnalyticsManager.trackEvent(
            AnalyticsEvents.ENTER_SCREEN,
            mapOf(
                AnalyticsParams.SCREEN_NAME to "wechat_mp_screen",
                AnalyticsParams.MODULE_NAME to AnalyticsModules.WECHAT_MP
            )
        )
        viewModel.effect.collect { effect ->
            when (effect) {
                is WeChatMpEffect.ShowToast -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is WeChatMpEffect.OpenArticle -> {
                    snackbarHostState.showSnackbar("打开文章: ${effect.title}")
                }
                WeChatMpEffect.ScrollToTop -> {
                    staggeredGridState.animateScrollToItem(0)
                }
            }
        }
    }

    // 触底分页加载更多检测
    val shouldLoadMore by remember {
        derivedStateOf {
            val totalItemsCount = staggeredGridState.layoutInfo.totalItemsCount
            val lastVisibleIndex = staggeredGridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItemsCount > 0 && lastVisibleIndex >= totalItemsCount - 3
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && !uiState.isLoadingMore && !uiState.isRefreshing) {
            viewModel.handleIntent(WeChatMpIntent.LoadMore)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = WeChatBackgroundGray,
        topBar = {
            WeChatMpTopBar(
                onBackClick = {
                    AppAnalyticsManager.trackEvent(
                        AnalyticsEvents.LEAVE_SCREEN,
                        mapOf(
                            AnalyticsParams.SCREEN_NAME to "wechat_mp_screen",
                            AnalyticsParams.MODULE_NAME to AnalyticsModules.WECHAT_MP
                        )
                    )
                    onBackClick()
                },
                onSearchClick = { viewModel.handleIntent(WeChatMpIntent.ClickSearch) },
                onProfileClick = { viewModel.handleIntent(WeChatMpIntent.ClickProfile) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(WeChatBackgroundGray),
            contentAlignment = Alignment.TopCenter
        ) {
            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = { viewModel.handleIntent(WeChatMpIntent.Refresh) },
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = maxContentWidth)
            ) {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(columnCount),
                    state = staggeredGridState,
                    contentPadding = PaddingValues(bottom = 24.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // 1. 常读公众号横滑列表
                    if (uiState.frequentlyReadAccounts.isNotEmpty()) {
                        item(span = StaggeredGridItemSpan.FullLine) {
                            WeChatMpFrequentlyReadBar(
                                accounts = uiState.frequentlyReadAccounts,
                                onAccountClick = { viewModel.handleIntent(WeChatMpIntent.ClickAccount(it)) }
                            )
                        }
                    }

                    // 2. 常读置顶推文大卡片
                    uiState.featuredArticle?.let { featured ->
                        item(span = StaggeredGridItemSpan.FullLine) {
                            WeChatMpFeaturedBannerCard(
                                article = featured,
                                onArticleClick = { viewModel.handleIntent(WeChatMpIntent.ClickArticle(it)) },
                                onMoreMessagesClick = { viewModel.handleIntent(WeChatMpIntent.ClickAccount(it)) },
                                onMenuClick = { viewModel.handleIntent(WeChatMpIntent.RequestDislike(it)) }
                            )
                        }
                    }

                    // 3. 看一看分区标题
                    item(span = StaggeredGridItemSpan.FullLine) {
                        Text(
                            text = stringResource(Res.string.wechat_mp_look_around),
                            color = WeChatTextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 12.dp)
                        )
                    }

                    // 4. 看一看瀑布流信息流 (混合卡片排布)
                    items(
                        items = uiState.waterfallArticles,
                        key = { it.id },
                        span = { article ->
                            if (article.cardType == WeChatCardType.BANNER_LARGE ||
                                article.cardType == WeChatCardType.HORIZONTAL_ROW
                            ) {
                                StaggeredGridItemSpan.FullLine
                            } else {
                                StaggeredGridItemSpan.SingleLane
                            }
                        }
                    ) { article ->
                        Box(
                            modifier = Modifier.padding(
                                horizontal = if (article.cardType == WeChatCardType.BANNER_LARGE ||
                                    article.cardType == WeChatCardType.HORIZONTAL_ROW
                                ) 12.dp else 4.dp,
                                vertical = 4.dp
                            )
                        ) {
                            if (article.cardType == WeChatCardType.BANNER_LARGE ||
                                article.cardType == WeChatCardType.HORIZONTAL_ROW
                            ) {
                                WeChatMpHorizontalCard(
                                    article = article,
                                    onArticleClick = { viewModel.handleIntent(WeChatMpIntent.ClickArticle(it)) },
                                    onDislikeClick = { viewModel.handleIntent(WeChatMpIntent.RequestDislike(it)) }
                                )
                            } else {
                                WeChatMpWaterfallCard(
                                    article = article,
                                    onArticleClick = { viewModel.handleIntent(WeChatMpIntent.ClickArticle(it)) },
                                    onDislikeClick = { viewModel.handleIntent(WeChatMpIntent.RequestDislike(it)) }
                                )
                            }
                        }
                    }

                    // 5. 底部加载中指示器
                    if (uiState.isLoadingMore) {
                        item(span = StaggeredGridItemSpan.FullLine) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = WeChatBrandGreen,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 负反馈/不感兴趣弹出面板
            WeChatMpDislikeBottomSheet(
                targetArticle = uiState.dislikeTargetArticle,
                onSelectReason = { reason ->
                    uiState.dislikeTargetArticle?.let { article ->
                        viewModel.handleIntent(WeChatMpIntent.SubmitDislike(article.id, reason))
                    }
                },
                onDismiss = { viewModel.handleIntent(WeChatMpIntent.DismissDislike) }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WeChatMpScreenPreview() {
    org.example.project.ui.theme.wechat.WeChatTheme {
        val mockRepo = object : org.example.project.domain.repository.wechat.WeChatMpRepository {
            override fun observeFrequentlyReadAccounts() = kotlinx.coroutines.flow.flowOf(emptyList<org.example.project.domain.model.wechat.WeChatAccount>())
            override fun observeFeaturedArticle() = kotlinx.coroutines.flow.flowOf(null)
            override fun observeWaterfallArticles() = kotlinx.coroutines.flow.flowOf(emptyList<org.example.project.domain.model.wechat.WeChatArticle>())
            override suspend fun refreshData() = Result.success(Unit)
            override suspend fun loadMoreArticles() = Result.success(emptyList<org.example.project.domain.model.wechat.WeChatArticle>())
            override suspend fun dislikeArticle(articleId: String, reason: String) = Result.success(Unit)
            override suspend fun markAccountAsRead(accountId: String) = Result.success(Unit)
            override suspend fun toggleFollowAccount(accountId: String) = Result.success(true)
        }
        val fakeViewModel = WeChatMpViewModel(repository = mockRepo)
        WeChatMpScreen(viewModel = fakeViewModel)
    }
}
