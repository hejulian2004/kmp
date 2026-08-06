/**
 * @File: FeedLineViewModel.kt
 * @Package: org.example.project.presentation.viewmodel.feedline
 * @Description: 朋友圈/动态模块的MVI核心视图模型，负责状态管理与事件处理
 * @Author: 何聚敛
 * @Date: 2026-07-20
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
import org.example.project.data.analytics.feedline.LogAnalyticsTracker
import org.example.project.domain.analytics.feedline.AnalyticsEvents
import org.example.project.domain.analytics.feedline.AnalyticsParams
import org.example.project.domain.analytics.feedline.AnalyticsTracker
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
 * @param analyticsTracker数据埋点追踪器
 */
class FeedLineViewModel(
    private val feedRepository: FeedLineRepository,
    currentUser: FeedLineUser,
    private val analyticsTracker: AnalyticsTracker = LogAnalyticsTracker()
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

    init {
        observeFeeds()
        observeNotifications()
        refreshFeed(showSuccessMessage = false)
        analyticsTracker.trackEvent(
            AnalyticsEvents.OPEN_FEED,
            mapOf(AnalyticsParams.USER_ID to currentUser.id)
        )
    }

    fun handleIntent(feedIntent: FeedIntent) {
        when (feedIntent) {
            FeedIntent.Refresh -> {
                analyticsTracker.trackEvent(AnalyticsEvents.REFRESH_FEED)
                refreshFeed(showSuccessMessage = true)
            }

            is FeedIntent.CreatePost -> {
                analyticsTracker.trackEvent(
                    AnalyticsEvents.CREATE_POST,
                    mapOf(
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
                clearAllNotifications()
            }

            is FeedIntent.NavigateTo -> {
                analyticsTracker.trackEvent(
                    AnalyticsEvents.ENTER_SCREEN,
                    mapOf(AnalyticsParams.SCREEN_NAME to feedIntent.screen.name)
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
            createPostUseCase(
                user = postUser,
                content = content,
                mediaList = mediaList
            )
            _effect.send(FeedLineEffect.ShowMessage("发布成功"))
            _effect.send(FeedLineEffect.ScrollToIndex(scrollToIndex))
        }
    }

    private fun updatePost(postId: String, content: String, mediaList: List<FeedLineMedia>) {
        viewModelScope.launch {
            updatePostUseCase(
                postId = postId,
                content = content,
                mediaList = mediaList
            )
            _effect.send(FeedLineEffect.ShowMessage("修改成功"))
        }
    }

    private fun deletePost(postId: String) {
        viewModelScope.launch {
            deletePostUseCase(postId = postId)
            _effect.send(FeedLineEffect.ShowMessage("删除成功"))
        }
    }

    private fun likePost(postId: String, user: FeedLineUser) {
        viewModelScope.launch {
            likePostUseCase(
                postId = postId,
                user = user,
                currentUserId = uiState.value.currentUser.id
            )
        }
    }

    private fun unlikePost(postId: String, user: FeedLineUser) {
        viewModelScope.launch {
            unlikePostUseCase(
                postId = postId,
                user = user
            )
        }
    }

    private fun addComment(postId: String, user: FeedLineUser, content: String) {
        viewModelScope.launch {
            addCommentUseCase(
                postId = postId,
                user = user,
                content = content,
                currentUserId = uiState.value.currentUser.id
            )
            _effect.send(FeedLineEffect.ShowMessage("评论成功"))
        }
    }

    private fun deleteComment(comment: FeedLineComment) {
        viewModelScope.launch {
            deleteCommentUseCase(comment = comment)
            _effect.send(FeedLineEffect.ShowMessage("删除评论成功"))
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
