/**
 * @File: FeedLineViewModel.kt
 * @Package: org.example.project.presentation.viewmodel.feedline
 * @Description: 朋友圈/动态模块的MVI核心视图模型，负责状态管理与事件处理
 * @Author: 何聚敛
 * @Date: 2026-08-11
 */
package org.example.project.presentation.viewmodel.feedline

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
import org.example.project.core.analytics.AnalyticsTracker
import org.example.project.core.analytics.AppAnalyticsManager
import org.example.project.domain.model.feedline.FeedLineComment
import org.example.project.domain.model.feedline.FeedLineMedia
import org.example.project.domain.model.feedline.FeedLineNotification
import org.example.project.domain.model.feedline.FeedLineUser
import org.example.project.domain.repository.feedline.FeedLineRepository
import org.example.project.domain.usecase.feedline.AddCommentUseCase
import org.example.project.domain.usecase.feedline.AddNotificationUseCase
import org.example.project.domain.usecase.feedline.ClearNotificationsUseCase
import org.example.project.domain.usecase.feedline.CreatePostUseCase
import org.example.project.domain.usecase.feedline.DeleteCommentNotificationUseCase
import org.example.project.domain.usecase.feedline.DeleteCommentUseCase
import org.example.project.domain.usecase.feedline.DeleteLikeNotificationUseCase
import org.example.project.domain.usecase.feedline.DeletePostUseCase
import org.example.project.domain.usecase.feedline.GetFeedPostsUseCase
import org.example.project.domain.usecase.feedline.GetNotificationsUseCase
import org.example.project.domain.usecase.feedline.LikePostUseCase
import org.example.project.domain.usecase.feedline.MarkNotificationsAsReadUseCase
import org.example.project.domain.usecase.feedline.RefreshFeedUseCase
import org.example.project.domain.usecase.feedline.UnlikePostUseCase
import org.example.project.domain.usecase.feedline.UpdatePostUseCase
import org.example.project.platform.currentTimeMillis
import org.example.project.presentation.effect.feedline.FeedLineEffect
import org.example.project.presentation.intent.feedline.FeedIntent
import org.example.project.presentation.state.RefreshState
import org.example.project.presentation.state.UiState
import org.example.project.presentation.state.feedline.FeedUiState
import org.example.project.presentation.state.feedline.Screen
/**
 * 朋友圈状态管理器
 *
 * @param feedRepository动态数据仓库接口
 * @param currentUser当前登录用户
 * @param analyticsTracker数据埋点追踪器（默认使用 AppAnalyticsManager 全局单例）
 */
class FeedLineViewModel(
    private val feedRepository: FeedLineRepository,
    currentUser: FeedLineUser,
    private val analyticsTracker: AnalyticsTracker = AppAnalyticsManager
) : ViewModel() {
    private val getFeedPostsUseCase = GetFeedPostsUseCase(feedRepository)
    private val refreshFeedUseCase = RefreshFeedUseCase(feedRepository)
    private val likePostUseCase = LikePostUseCase(feedRepository)
    private val unlikePostUseCase = UnlikePostUseCase(feedRepository)
    private val addCommentUseCase = AddCommentUseCase(feedRepository)
    private val deleteCommentUseCase = DeleteCommentUseCase(feedRepository)
    private val createPostUseCase = CreatePostUseCase(feedRepository)
    private val deletePostUseCase = DeletePostUseCase(feedRepository)
    private val updatePostUseCase = UpdatePostUseCase(feedRepository)
    private val getNotificationsUseCase = GetNotificationsUseCase(feedRepository)
    private val addNotificationUseCase = AddNotificationUseCase(feedRepository)
    private val deleteCommentNotificationUseCase = DeleteCommentNotificationUseCase(feedRepository)
    private val deleteLikeNotificationUseCase = DeleteLikeNotificationUseCase(feedRepository)
    private val clearNotificationsUseCase = ClearNotificationsUseCase(feedRepository)
    private val markNotificationsAsReadUseCase = MarkNotificationsAsReadUseCase(feedRepository)

    private val _uiState = MutableStateFlow(
        FeedUiState(
            currentUser = currentUser,
            feedState = UiState.Loading
        )
    )
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    private val _effect = Channel<FeedLineEffect>(Channel.BUFFERED)
    val effect: Flow<FeedLineEffect> = _effect.receiveAsFlow()

    private var currentScreenStartTime = currentTimeMillis()

    init {
        AppAnalyticsManager.setUserContext(currentUser.id)
        observeFeeds()
        observeNotifications()
        refreshFeed(showSuccessMessage = false)
        analyticsTracker.trackEvent(
            AnalyticsEvents.OPEN_FEED,
            mapOf(
                AnalyticsParams.MODULE_NAME to AnalyticsModules.FEEDLINE,
                AnalyticsParams.USER_ID to currentUser.id
            )
        )
    }

    fun handleIntent(feedIntent: FeedIntent) {
        when (feedIntent) {
            FeedIntent.Refresh -> {
                analyticsTracker.trackEvent(
                    AnalyticsEvents.REFRESH_FEED,
                    mapOf(AnalyticsParams.MODULE_NAME to AnalyticsModules.FEEDLINE)
                )
                refreshFeed(showSuccessMessage = true)
            }

            FeedIntent.LoadMore -> {
                analyticsTracker.trackEvent(
                    AnalyticsEvents.FEED_LOAD_MORE,
                    mapOf(AnalyticsParams.MODULE_NAME to AnalyticsModules.FEEDLINE)
                )
            }

            is FeedIntent.PreviewMedia -> {
                analyticsTracker.trackEvent(
                    AnalyticsEvents.MEDIA_PREVIEW,
                    mapOf(
                        AnalyticsParams.MODULE_NAME to AnalyticsModules.FEEDLINE,
                        AnalyticsParams.POST_ID to feedIntent.postId,
                        "media_url" to feedIntent.mediaUrl,
                        "media_type" to if (feedIntent.isVideo) "video" else "image"
                    )
                )
            }

            is FeedIntent.CreatePost -> {
                analyticsTracker.trackEvent(
                    AnalyticsEvents.CREATE_POST,
                    mapOf(
                        AnalyticsParams.MODULE_NAME to AnalyticsModules.FEEDLINE,
                        AnalyticsParams.USER_ID to feedIntent.user.id,
                        AnalyticsParams.MEDIA_COUNT to feedIntent.mediaList.size,
                        AnalyticsParams.HAS_TEXT to feedIntent.content.isNotBlank()
                    )
                )
                createPost(
                    postUser = feedIntent.user,
                    content = feedIntent.content,
                    mediaList = feedIntent.mediaList
                )
            }

            is FeedIntent.UpdatePost -> {
                updatePost(
                    postId = feedIntent.postId,
                    content = feedIntent.content,
                    mediaList = feedIntent.mediaList
                )
            }

            is FeedIntent.DeletePost -> {
                analyticsTracker.trackEvent(
                    AnalyticsEvents.DELETE_POST,
                    mapOf(
                        AnalyticsParams.MODULE_NAME to AnalyticsModules.FEEDLINE,
                        AnalyticsParams.POST_ID to feedIntent.postId,
                        AnalyticsParams.USER_ID to uiState.value.currentUser.id
                    )
                )
                deletePost(feedIntent.postId)
            }

            is FeedIntent.AddComment -> {
                analyticsTracker.trackEvent(
                    AnalyticsEvents.ADD_COMMENT,
                    mapOf(
                        AnalyticsParams.MODULE_NAME to AnalyticsModules.FEEDLINE,
                        AnalyticsParams.POST_ID to feedIntent.postId,
                        AnalyticsParams.USER_ID to feedIntent.user.id
                    )
                )
                addComment(
                    feedIntent.postId,
                    feedIntent.user,
                    feedIntent.content
                )
            }

            is FeedIntent.DeleteComment -> {
                analyticsTracker.trackEvent(
                    AnalyticsEvents.DELETE_COMMENT,
                    mapOf(
                        AnalyticsParams.MODULE_NAME to AnalyticsModules.FEEDLINE,
                        AnalyticsParams.COMMENT_ID to feedIntent.comment.id,
                        AnalyticsParams.USER_ID to uiState.value.currentUser.id
                    )
                )
                deleteComment(
                    feedIntent.comment
                )
            }

            is FeedIntent.LikePost -> {
                analyticsTracker.trackEvent(
                    AnalyticsEvents.LIKE_POST,
                    mapOf(
                        AnalyticsParams.MODULE_NAME to AnalyticsModules.FEEDLINE,
                        AnalyticsParams.POST_ID to feedIntent.postId,
                        AnalyticsParams.USER_ID to feedIntent.user.id
                    )
                )
                likePost(
                    feedIntent.postId,
                    feedIntent.user
                )
            }

            is FeedIntent.UnlikePost -> {
                analyticsTracker.trackEvent(
                    AnalyticsEvents.UNLIKE_POST,
                    mapOf(
                        AnalyticsParams.MODULE_NAME to AnalyticsModules.FEEDLINE,
                        AnalyticsParams.POST_ID to feedIntent.postId,
                        AnalyticsParams.USER_ID to feedIntent.user.id
                    )
                )
                unlikePost(
                    feedIntent.postId,
                    feedIntent.user
                )
            }

            is FeedIntent.ShowMessage -> {
                showMessage(feedIntent.message)
            }

            is FeedIntent.AddNotification -> {
                addNotification(feedIntent.feedNotification)
            }

            is FeedIntent.DeleteCommentNotification -> {
                deleteCommentNotification(feedIntent.feedNotification)
            }

            is FeedIntent.DeleteLikeNotification -> {
                deleteLikeNotification(feedIntent.feedNotification)
            }

            FeedIntent.ClearAllNotifications -> {
                val currentCount = (uiState.value.notificationsState as? UiState.Success)?.data?.count { !it.isDelete } ?: 0
                analyticsTracker.trackEvent(
                    AnalyticsEvents.NOTIFICATION_CLEAR,
                    mapOf(
                        AnalyticsParams.MODULE_NAME to AnalyticsModules.FEEDLINE,
                        AnalyticsParams.CLEARED_COUNT to currentCount
                    )
                )
                clearAllNotifications()
            }

            is FeedIntent.ViewUserProfile -> {
                analyticsTracker.trackEvent(
                    AnalyticsEvents.USER_PROFILE_VIEW,
                    mapOf(
                        AnalyticsParams.MODULE_NAME to AnalyticsModules.FEEDLINE,
                        AnalyticsParams.TARGET_USER_ID to feedIntent.targetUserId,
                        AnalyticsParams.CLICK_SOURCE to feedIntent.clickSource
                    )
                )
            }

            is FeedIntent.ClickNotificationBar -> {
                analyticsTracker.trackEvent(
                    AnalyticsEvents.NOTIFICATION_BAR_CLICK,
                    mapOf(
                        AnalyticsParams.MODULE_NAME to AnalyticsModules.FEEDLINE,
                        AnalyticsParams.UNREAD_COUNT to feedIntent.unreadCount
                    )
                )
                handleIntent(FeedIntent.NavigateTo(Screen.Notification))
            }

            is FeedIntent.SelectMedia -> {
                analyticsTracker.trackEvent(
                    AnalyticsEvents.MEDIA_SELECT,
                    mapOf(
                        AnalyticsParams.MODULE_NAME to AnalyticsModules.FEEDLINE,
                        AnalyticsParams.SOURCE_TYPE to feedIntent.sourceType,
                        AnalyticsParams.MEDIA_COUNT to feedIntent.mediaCount
                    )
                )
            }

            is FeedIntent.CancelPublish -> {
                analyticsTracker.trackEvent(
                    AnalyticsEvents.POST_CANCEL,
                    mapOf(
                        AnalyticsParams.MODULE_NAME to AnalyticsModules.FEEDLINE,
                        AnalyticsParams.HAS_CONTENT to feedIntent.hasContent
                    )
                )
                handleIntent(FeedIntent.NavigateTo(Screen.Feed))
            }

            FeedIntent.LongClickCreatePostTextOnly -> {
                analyticsTracker.trackEvent(
                    AnalyticsEvents.POST_TEXT_ONLY_ENTER,
                    mapOf(AnalyticsParams.MODULE_NAME to AnalyticsModules.FEEDLINE)
                )
                handleIntent(FeedIntent.NavigateTo(Screen.Publish))
            }

            is FeedIntent.NavigateTo -> {
                val now = currentTimeMillis()
                val duration = now - currentScreenStartTime
                analyticsTracker.trackEvent(
                    AnalyticsEvents.LEAVE_SCREEN,
                    mapOf(
                        AnalyticsParams.MODULE_NAME to AnalyticsModules.FEEDLINE,
                        AnalyticsParams.SCREEN_NAME to uiState.value.currentScreen.name,
                        AnalyticsParams.DURATION_MS to duration
                    )
                )
                currentScreenStartTime = now
                analyticsTracker.trackEvent(
                    AnalyticsEvents.ENTER_SCREEN,
                    mapOf(
                        AnalyticsParams.MODULE_NAME to AnalyticsModules.FEEDLINE,
                        AnalyticsParams.SCREEN_NAME to feedIntent.screen.name
                    )
                )
                _uiState.update {
                    it.copy(currentScreen = feedIntent.screen)
                }
                if (feedIntent.screen == Screen.Notification) {
                    markNotificationsAsRead()
                }
            }
        }
    }

    private fun observeFeeds() {
        viewModelScope.launch {
            getFeedPostsUseCase()
                .catch { e ->
                    analyticsTracker.trackEvent(
                        AnalyticsEvents.NETWORK_ERROR,
                        mapOf(
                            AnalyticsParams.MODULE_NAME to AnalyticsModules.FEEDLINE,
                            AnalyticsParams.ERROR_MSG to (e.message ?: "加载动态失败")
                        )
                    )
                    _uiState.update { it.copy(feedState = UiState.Error(e.message ?: "未知错误")) }
                    _effect.send(FeedLineEffect.ShowMessage("加载动态失败: ${e.message ?: "未知错误"}"))
                }
                .collect { newPosts ->
                    _uiState.update {
                        it.copy(
                            feedState = UiState.Success(newPosts)
                        )
                    }
                }
        }
    }

    private fun observeNotifications() {
        viewModelScope.launch {
            _uiState.update { it.copy(notificationsState = UiState.Loading) }
            getNotificationsUseCase()
                .catch { e ->
                    analyticsTracker.trackEvent(
                        AnalyticsEvents.NETWORK_ERROR,
                        mapOf(
                            AnalyticsParams.MODULE_NAME to AnalyticsModules.FEEDLINE,
                            AnalyticsParams.ERROR_MSG to (e.message ?: "加载通知失败")
                        )
                    )
                    _uiState.update { it.copy(notificationsState = UiState.Error(e.message ?: "未知错误")) }
                    _effect.send(FeedLineEffect.ShowMessage("加载通知失败: ${e.message ?: "未知错误"}"))
                }
                .collect { notifications ->
                    val unreadCount = notifications.count { !it.isRead && !it.isDelete }
                    _uiState.update {
                        it.copy(
                            notificationsState = UiState.Success(notifications),
                            unreadNotificationCount = unreadCount
                        )
                    }
                }
        }
    }

    private fun refreshFeed(showSuccessMessage: Boolean) {
        viewModelScope.launch {
            val currentState = _uiState.value.feedState
            if (currentState is UiState.Success) {
                _uiState.update {
                    it.copy(feedState = currentState.copy(refreshState = RefreshState.Refreshing))
                }
            } else {
                _uiState.update { it.copy(feedState = UiState.Loading) }
            }

            try {
                refreshFeedUseCase()
                if (showSuccessMessage) {
                    _effect.send(FeedLineEffect.ShowMessage("刷新成功"))
                }
            } catch (e: Exception) {
                if (currentState is UiState.Success) {
                    _uiState.update {
                        it.copy(feedState = currentState.copy(refreshState = RefreshState.Idle))
                    }
                } else {
                    _uiState.update { it.copy(feedState = UiState.Error(e.message ?: "刷新失败")) }
                }
                _effect.send(FeedLineEffect.ShowMessage("刷新失败"))
            }
        }
    }

    private fun createPost(postUser: FeedLineUser, content: String, mediaList: List<FeedLineMedia>, scrollToIndex: Int = 0) {
        viewModelScope.launch {
            try {
                createPostUseCase(
                    user = postUser,
                    content = content,
                    mediaList = mediaList
                )
                analyticsTracker.trackEvent(
                    AnalyticsEvents.CREATE_POST,
                    mapOf(
                        AnalyticsParams.MODULE_NAME to AnalyticsModules.FEEDLINE,
                        AnalyticsParams.USER_ID to postUser.id,
                        AnalyticsParams.MEDIA_COUNT to mediaList.size,
                        AnalyticsParams.HAS_TEXT to content.isNotBlank(),
                        AnalyticsParams.IS_SUCCESS to true
                    )
                )
                _effect.send(FeedLineEffect.ShowMessage("发布成功"))
                _effect.send(FeedLineEffect.ScrollToIndex(scrollToIndex))
            } catch (e: Exception) {
                analyticsTracker.trackEvent(
                    AnalyticsEvents.CREATE_POST,
                    mapOf(
                        AnalyticsParams.MODULE_NAME to AnalyticsModules.FEEDLINE,
                        AnalyticsParams.USER_ID to postUser.id,
                        AnalyticsParams.MEDIA_COUNT to mediaList.size,
                        AnalyticsParams.HAS_TEXT to content.isNotBlank(),
                        AnalyticsParams.IS_SUCCESS to false,
                        AnalyticsParams.ERROR_MSG to (e.message ?: "发布失败")
                    )
                )
                _effect.send(FeedLineEffect.ShowMessage("发布失败: ${e.message ?: "未知错误"}"))
            }
        }
    }

    private fun updatePost(postId: String, content: String, mediaList: List<FeedLineMedia>) {
        viewModelScope.launch {
            try {
                updatePostUseCase(
                    postId = postId,
                    content = content,
                    mediaList = mediaList
                )
                _effect.send(FeedLineEffect.ShowMessage("修改成功"))
            } catch (e: Exception) {
                _effect.send(FeedLineEffect.ShowMessage("修改失败: ${e.message ?: "未知错误"}"))
            }
        }
    }

    private fun deletePost(postId: String) {
        viewModelScope.launch {
            try {
                deletePostUseCase(postId = postId)
                analyticsTracker.trackEvent(
                    AnalyticsEvents.DELETE_POST,
                    mapOf(
                        AnalyticsParams.MODULE_NAME to AnalyticsModules.FEEDLINE,
                        AnalyticsParams.POST_ID to postId,
                        AnalyticsParams.USER_ID to uiState.value.currentUser.id,
                        AnalyticsParams.IS_SUCCESS to true
                    )
                )
                _effect.send(FeedLineEffect.ShowMessage("删除成功"))
            } catch (e: Exception) {
                analyticsTracker.trackEvent(
                    AnalyticsEvents.DELETE_POST,
                    mapOf(
                        AnalyticsParams.MODULE_NAME to AnalyticsModules.FEEDLINE,
                        AnalyticsParams.POST_ID to postId,
                        AnalyticsParams.USER_ID to uiState.value.currentUser.id,
                        AnalyticsParams.IS_SUCCESS to false,
                        AnalyticsParams.ERROR_MSG to (e.message ?: "删除失败")
                    )
                )
                _effect.send(FeedLineEffect.ShowMessage("删除失败: ${e.message ?: "未知错误"}"))
            }
        }
    }

    private fun likePost(postId: String, user: FeedLineUser) {
        viewModelScope.launch {
            try {
                likePostUseCase(
                    postId = postId,
                    user = user,
                    currentUserId = uiState.value.currentUser.id
                )
                analyticsTracker.trackEvent(
                    AnalyticsEvents.LIKE_POST,
                    mapOf(
                        AnalyticsParams.MODULE_NAME to AnalyticsModules.FEEDLINE,
                        AnalyticsParams.POST_ID to postId,
                        AnalyticsParams.USER_ID to user.id,
                        AnalyticsParams.IS_SUCCESS to true
                    )
                )
            } catch (e: Exception) {
                analyticsTracker.trackEvent(
                    AnalyticsEvents.LIKE_POST,
                    mapOf(
                        AnalyticsParams.MODULE_NAME to AnalyticsModules.FEEDLINE,
                        AnalyticsParams.POST_ID to postId,
                        AnalyticsParams.USER_ID to user.id,
                        AnalyticsParams.IS_SUCCESS to false,
                        AnalyticsParams.ERROR_MSG to (e.message ?: "点赞失败")
                    )
                )
            }
        }
    }

    private fun unlikePost(postId: String, user: FeedLineUser) {
        viewModelScope.launch {
            try {
                unlikePostUseCase(
                    postId = postId,
                    user = user
                )
                analyticsTracker.trackEvent(
                    AnalyticsEvents.UNLIKE_POST,
                    mapOf(
                        AnalyticsParams.MODULE_NAME to AnalyticsModules.FEEDLINE,
                        AnalyticsParams.POST_ID to postId,
                        AnalyticsParams.USER_ID to user.id,
                        AnalyticsParams.IS_SUCCESS to true
                    )
                )
            } catch (e: Exception) {
                analyticsTracker.trackEvent(
                    AnalyticsEvents.UNLIKE_POST,
                    mapOf(
                        AnalyticsParams.MODULE_NAME to AnalyticsModules.FEEDLINE,
                        AnalyticsParams.POST_ID to postId,
                        AnalyticsParams.USER_ID to user.id,
                        AnalyticsParams.IS_SUCCESS to false,
                        AnalyticsParams.ERROR_MSG to (e.message ?: "取消点赞失败")
                    )
                )
            }
        }
    }

    private fun addComment(postId: String, user: FeedLineUser, content: String) {
        viewModelScope.launch {
            try {
                addCommentUseCase(
                    postId = postId,
                    user = user,
                    content = content,
                    currentUserId = uiState.value.currentUser.id
                )
                analyticsTracker.trackEvent(
                    AnalyticsEvents.ADD_COMMENT,
                    mapOf(
                        AnalyticsParams.MODULE_NAME to AnalyticsModules.FEEDLINE,
                        AnalyticsParams.POST_ID to postId,
                        AnalyticsParams.USER_ID to user.id,
                        AnalyticsParams.IS_SUCCESS to true
                    )
                )
                _effect.send(FeedLineEffect.ShowMessage("评论成功"))
            } catch (e: Exception) {
                analyticsTracker.trackEvent(
                    AnalyticsEvents.ADD_COMMENT,
                    mapOf(
                        AnalyticsParams.MODULE_NAME to AnalyticsModules.FEEDLINE,
                        AnalyticsParams.POST_ID to postId,
                        AnalyticsParams.USER_ID to user.id,
                        AnalyticsParams.IS_SUCCESS to false,
                        AnalyticsParams.ERROR_MSG to (e.message ?: "评论失败")
                    )
                )
                _effect.send(FeedLineEffect.ShowMessage("评论失败: ${e.message ?: "未知错误"}"))
            }
        }
    }

    private fun deleteComment(comment: FeedLineComment) {
        viewModelScope.launch {
            try {
                deleteCommentUseCase(comment = comment)
                analyticsTracker.trackEvent(
                    AnalyticsEvents.DELETE_COMMENT,
                    mapOf(
                        AnalyticsParams.MODULE_NAME to AnalyticsModules.FEEDLINE,
                        AnalyticsParams.COMMENT_ID to comment.id,
                        AnalyticsParams.USER_ID to uiState.value.currentUser.id,
                        AnalyticsParams.IS_SUCCESS to true
                    )
                )
                _effect.send(FeedLineEffect.ShowMessage("删除评论成功"))
            } catch (e: Exception) {
                analyticsTracker.trackEvent(
                    AnalyticsEvents.DELETE_COMMENT,
                    mapOf(
                        AnalyticsParams.MODULE_NAME to AnalyticsModules.FEEDLINE,
                        AnalyticsParams.COMMENT_ID to comment.id,
                        AnalyticsParams.USER_ID to uiState.value.currentUser.id,
                        AnalyticsParams.IS_SUCCESS to false,
                        AnalyticsParams.ERROR_MSG to (e.message ?: "删除评论失败")
                    )
                )
                _effect.send(FeedLineEffect.ShowMessage("删除评论失败: ${e.message ?: "未知错误"}"))
            }
        }
    }

    private fun showMessage(message: String) {
        viewModelScope.launch {
            _effect.send(FeedLineEffect.ShowMessage(message))
        }
    }

    private fun addNotification(notification: FeedLineNotification) {
        viewModelScope.launch {
            addNotificationUseCase(notification)
        }
    }

    private fun deleteCommentNotification(notification: FeedLineNotification) {
        viewModelScope.launch {
            deleteCommentNotificationUseCase(notification)
        }
    }

    private fun deleteLikeNotification(notification: FeedLineNotification) {
        viewModelScope.launch {
            deleteLikeNotificationUseCase(notification)
        }
    }

    private fun clearAllNotifications() {
        viewModelScope.launch {
            clearNotificationsUseCase()
        }
    }

    private fun markNotificationsAsRead() {
        viewModelScope.launch {
            markNotificationsAsReadUseCase()
        }
    }
}
