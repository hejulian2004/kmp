/**
 * @File: WeChatMpViewModelTest.kt
 * @Package: org.example.project.presentation.viewmodel.wechat
 * @Description: WeChatMpViewModel微信公众号MVI意图响应与状态流驱动单元测试
 * @Author: 何聚敛
 * @Date: 2026-08-18
 */
package org.example.project.presentation.viewmodel.wechat

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.example.project.core.analytics.AnalyticsConfig
import org.example.project.core.analytics.AppAnalyticsManager
import org.example.project.core.analytics.LogAnalyticsTracker
import org.example.project.core.database.FakeWeChatMpDao
import org.example.project.data.repository.wechat.WeChatMpRepositoryImpl
import org.example.project.presentation.intent.wechat.WeChatMpIntent
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class WeChatMpViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var dao: FakeWeChatMpDao
    private lateinit var repository: WeChatMpRepositoryImpl
    private lateinit var viewModel: WeChatMpViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        if (!AppAnalyticsManager.isInitialized) {
            AppAnalyticsManager.init(
                AnalyticsConfig(
                    platformName = "Test",
                    appVersion = "1.0.0",
                    deviceId = "test_device",
                    trackers = listOf(LogAnalyticsTracker())
                )
            )
        }
        dao = FakeWeChatMpDao()
        repository = WeChatMpRepositoryImpl(weChatMpDao = dao)
        viewModel = WeChatMpViewModel(repository = repository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialUiState() = runTest(testDispatcher) {
        testScheduler.advanceUntilIdle()
        val state = viewModel.uiState.value
        assertFalse(state.isRefreshing)
        assertFalse(state.isLoadingMore)
        assertNotNull(state.featuredArticle)
        assertTrue(state.frequentlyReadAccounts.isNotEmpty())
        assertTrue(state.waterfallArticles.isNotEmpty())
    }

    @Test
    fun testHandleRefreshIntent() = runTest(testDispatcher) {
        testScheduler.advanceUntilIdle()
        viewModel.handleIntent(WeChatMpIntent.Refresh)
        testScheduler.advanceUntilIdle()
        val state = viewModel.uiState.value
        assertFalse(state.isRefreshing)
        assertNotNull(state.featuredArticle)
    }

    @Test
    fun testHandleLoadMoreIntent() = runTest(testDispatcher) {
        testScheduler.advanceUntilIdle()
        val initialCount = viewModel.uiState.value.waterfallArticles.size
        viewModel.handleIntent(WeChatMpIntent.LoadMore)
        testScheduler.advanceUntilIdle()
        val newCount = viewModel.uiState.value.waterfallArticles.size
        assertEquals(initialCount + 2, newCount)
    }

    @Test
    fun testHandleClickArticleAndAccount() = runTest(testDispatcher) {
        testScheduler.advanceUntilIdle()
        val article = viewModel.uiState.value.waterfallArticles.first()
        viewModel.handleIntent(WeChatMpIntent.ClickArticle(article))
        testScheduler.advanceUntilIdle()

        val account = viewModel.uiState.value.frequentlyReadAccounts.first()
        viewModel.handleIntent(WeChatMpIntent.ClickAccount(account))
        testScheduler.advanceUntilIdle()
    }

    @Test
    fun testHandleDislikeFlow() = runTest(testDispatcher) {
        testScheduler.advanceUntilIdle()
        val article = viewModel.uiState.value.waterfallArticles.first()

        // 1. 请求不感兴趣，设置弹窗目标
        viewModel.handleIntent(WeChatMpIntent.RequestDislike(article))
        val sheetState = viewModel.uiState.value
        assertEquals(article.id, sheetState.dislikeTargetArticle?.id)

        // 2. 提交不感兴趣原因，移除文章并关闭弹窗
        viewModel.handleIntent(WeChatMpIntent.SubmitDislike(article.id, "内容低俗/标题党"))
        testScheduler.advanceUntilIdle()
        val afterSubmitState = viewModel.uiState.value
        assertNull(afterSubmitState.dislikeTargetArticle)
        assertFalse(afterSubmitState.waterfallArticles.any { it.id == article.id })
    }

    @Test
    fun testHandleDismissDislike() = runTest(testDispatcher) {
        testScheduler.advanceUntilIdle()
        val article = viewModel.uiState.value.waterfallArticles.first()
        viewModel.handleIntent(WeChatMpIntent.RequestDislike(article))
        assertEquals(article.id, viewModel.uiState.value.dislikeTargetArticle?.id)

        viewModel.handleIntent(WeChatMpIntent.DismissDislike)
        testScheduler.advanceUntilIdle()
        assertNull(viewModel.uiState.value.dislikeTargetArticle)
    }

    @Test
    fun testHandleSearchAndProfileClicks() = runTest(testDispatcher) {
        testScheduler.advanceUntilIdle()
        viewModel.handleIntent(WeChatMpIntent.ClickSearch)
        testScheduler.advanceUntilIdle()

        viewModel.handleIntent(WeChatMpIntent.ClickProfile)
        testScheduler.advanceUntilIdle()
    }
}
