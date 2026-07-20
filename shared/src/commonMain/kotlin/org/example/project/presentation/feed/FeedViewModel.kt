package org.example.project.presentation.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.example.project.domain.model.FeedComment
import org.example.project.domain.model.FeedMedia
import org.example.project.domain.model.FeedNotification
import org.example.project.domain.model.FeedUser
import org.example.project.domain.repository.FeedRepository
import org.example.project.domain.usecase.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.domain.analytics.AnalyticsTracker
import org.example.project.domain.analytics.AnalyticsEvents
import org.example.project.domain.analytics.AnalyticsParams
import org.example.project.data.analytics.LogAnalyticsTracker

class FeedViewModel(
    private val feedRepository: FeedRepository,
    currentUser: FeedUser,
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
            isLoading = true,
            currentUser = currentUser
        )
    )
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<FeedEffect>()
    val effect = _effect.asSharedFlow()

    init {
        observeFeeds()
        observeNotifications()
        refreshFeed(showSuccessMessage = false)
        analyticsTracker.trackEvent(
            AnalyticsEvents.OPEN_FEED,
            mapOf(AnalyticsParams.USER_ID to currentUser.id)
        )
    }

    fun handelIntent(feedIntent: FeedIntent) {
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
            getFeedPostsUseCase().collect { newPosts ->
                _uiState.update {
                    it.copy(
                        posts = newPosts,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun observeNotifications() {
        viewModelScope.launch {
            getNotificationsUseCase().collect { notifications ->
                val unreadCount = notifications.count { !it.isRead && !it.isDelete }
                _uiState.update {
                    it.copy(
                        notifications = notifications,
                        unreadNotificationCount = unreadCount
                    )
                }
            }
        }
    }

    private fun refreshFeed(showSuccessMessage: Boolean) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true)
            }
            runCatching {
                refreshFeedUseCase()
            }.onSuccess {
                _uiState.update {
                    it.copy(isLoading = false)
                }
                if (showSuccessMessage) {
                    _effect.emit(FeedEffect.ShowMessage("刷新成功"))
                }
            }.onFailure {
                _uiState.update { it.copy(isLoading = false) }
                _effect.emit(FeedEffect.ShowMessage("刷新失败"))
            }
        }
    }

    private fun createPost(postUser: FeedUser, content: String, mediaList: List<FeedMedia>, scrollToIndex: Int = 0) {
        viewModelScope.launch {
            createPostUseCase(
                user = postUser,
                content = content,
                mediaList = mediaList
            )
            _effect.emit(FeedEffect.ShowMessage("发布成功"))
            _effect.emit(FeedEffect.ScrollToIndex(scrollToIndex))
        }
    }

    private fun updatePost(postId: String, content: String, mediaList: List<FeedMedia>) {
        viewModelScope.launch {
            updatePostUseCase(
                postId = postId,
                content = content,
                mediaList = mediaList
            )
            _effect.emit(FeedEffect.ShowMessage("修改成功"))
        }
    }

    private fun deletePost(postId: String) {
        viewModelScope.launch {
            deletePostUseCase(postId = postId)
            _effect.emit(FeedEffect.ShowMessage("删除成功"))
        }
    }

    private fun likePost(postId: String, user: FeedUser) {
        viewModelScope.launch {
            likePostUseCase(
                postId = postId,
                user = user,
                currentUserId = uiState.value.currentUser.id
            )
        }
    }

    private fun unlikePost(postId: String, user: FeedUser) {
        viewModelScope.launch {
            unlikePostUseCase(
                postId = postId,
                user = user
            )
        }
    }

    private fun addComment(postId: String, user: FeedUser, content: String) {
        viewModelScope.launch {
            addCommentUseCase(
                postId = postId,
                user = user,
                content = content,
                currentUserId = uiState.value.currentUser.id
            )
            _effect.emit(FeedEffect.ShowMessage("评论成功"))
        }
    }

    private fun deleteComment(comment: FeedComment) {
        viewModelScope.launch {
            deleteCommentUseCase(comment = comment)
            _effect.emit(FeedEffect.ShowMessage("删除评论成功"))
        }
    }

    private fun showMessage(message: String) {
        viewModelScope.launch {
            _effect.emit(FeedEffect.ShowMessage(message))
        }
    }

    private fun addNotification(notification: FeedNotification) {
        viewModelScope.launch {
            addNotificationUseCase(notification)
        }
    }

    private fun deleteCommentNotification(notification: FeedNotification) {
        viewModelScope.launch {
            deleteCommentNotificationUseCase(notification)
        }
    }

    private fun deleteLikeNotification(notification: FeedNotification) {
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
