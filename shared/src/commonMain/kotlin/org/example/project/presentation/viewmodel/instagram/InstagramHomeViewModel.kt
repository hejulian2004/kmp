/**
 * @File: InstagramHomeViewModel.kt
 * @Package: org.example.project.presentation.viewmodel.instagram
 * @Description: Instagram首页的MVI核心视图模型，负责状态管理与事件处理（结构与FeedLineViewModel保持100%一致）
 * @Author: 何聚敛
 * @Date: 2026-07-28
 */
package org.example.project.presentation.viewmodel.instagram

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
import org.example.project.data.repository.instagram.InstagramHomeRepositoryImpl
import org.example.project.domain.model.instagram.InstagramMedia
import org.example.project.domain.model.instagram.ProfileUser
import org.example.project.domain.repository.instagram.InstagramHomeRepository
import org.example.project.presentation.effect.instagram.InstagramHomeEffect
import org.example.project.presentation.intent.instagram.InstagramHomeIntent
import org.example.project.presentation.state.RefreshState
import org.example.project.presentation.state.UiState
import org.example.project.presentation.state.instagram.InstagramHomeUiState

/**
 * Instagram首页状态管理器(ViewModel)
 *
 * 遵循MVI架构设计：
 * - 接收 [InstagramHomeIntent] 用户意图
 * - 维持 [InstagramHomeUiState] 页面状态StateFlow
 * - 发送 [InstagramHomeEffect] 一次性UI事件
 *
 * @param repository首页数据仓库接口
 * @param currentUser当前登录用户
 */
class InstagramHomeViewModel(
    private val repository: InstagramHomeRepository = InstagramHomeRepositoryImpl(),
    currentUser: ProfileUser? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        InstagramHomeUiState(
            currentUser = currentUser ?: ProfileUser(
                userId = "u_me",
                username = "hejulian",
                avatarUrl = "https://picsum.photos/seed/me/200/200",
                signature = "Kotlin KMP Developer",
                postCount = "18",
                followerCount = "450",
                followingCount = "320"
            ),
            postsState = UiState.Loading,
            storiesState = UiState.Loading
        )
    )
    val uiState: StateFlow<InstagramHomeUiState> = _uiState.asStateFlow()

    private val _effect = Channel<InstagramHomeEffect>(Channel.BUFFERED)
    val effect: Flow<InstagramHomeEffect> = _effect.receiveAsFlow()

    init {
        observeHomePosts()
        observeStories()
    }

    /**
     * MVI统一意图分发入口
     *
     * @param intent包含界面触发的所有用户操作意图
     */
    fun handleIntent(intent: InstagramHomeIntent) {
        when (intent) {
            InstagramHomeIntent.Refresh -> {
                refreshHome(showSuccessMessage = true)
            }

            is InstagramHomeIntent.LikePost -> {
                likePost(intent.postId, intent.user)
            }

            is InstagramHomeIntent.UnlikePost -> {
                unlikePost(intent.postId, intent.user)
            }

            is InstagramHomeIntent.SavePost -> {
                savePost(intent.postId)
            }

            is InstagramHomeIntent.UnsavePost -> {
                unsavePost(intent.postId)
            }

            is InstagramHomeIntent.AddComment -> {
                addComment(intent.postId, intent.user, intent.content)
            }

            is InstagramHomeIntent.DeleteComment -> {
                deleteComment(intent.postId, intent.commentId)
            }

            is InstagramHomeIntent.DeletePost -> {
                deletePost(intent.postId)
            }

            is InstagramHomeIntent.CreatePost -> {
                createPost(intent.user, intent.content, intent.mediaList, intent.location)
            }

            is InstagramHomeIntent.ShowMessage -> {
                showMessage(intent.message)
            }

            is InstagramHomeIntent.NavigateTo -> {
                _uiState.update { it.copy(currentScreen = intent.screen) }
            }
        }
    }

    private fun observeHomePosts() {
        viewModelScope.launch {
            repository.getHomePosts()
                .catch { e ->
                    _uiState.update { it.copy(postsState = UiState.Error(e.message ?: "加载动态失败")) }
                    _effect.send(InstagramHomeEffect.ShowMessage("加载动态失败: ${e.message}"))
                }
                .collect { posts ->
                    _uiState.update {
                        it.copy(postsState = UiState.Success(posts))
                    }
                }
        }
    }

    private fun observeStories() {
        viewModelScope.launch {
            repository.getStories()
                .catch { e ->
                    _uiState.update { it.copy(storiesState = UiState.Error(e.message ?: "加载Story失败")) }
                }
                .collect { stories ->
                    _uiState.update {
                        it.copy(storiesState = UiState.Success(stories))
                    }
                }
        }
    }

    private fun refreshHome(showSuccessMessage: Boolean) {
        viewModelScope.launch {
            val currentState = _uiState.value.postsState
            if (currentState is UiState.Success) {
                _uiState.update {
                    it.copy(postsState = currentState.copy(refreshState = RefreshState.Refreshing))
                }
            } else {
                _uiState.update { it.copy(postsState = UiState.Loading) }
            }

            try {
                repository.refreshHome()
                if (showSuccessMessage) {
                    _effect.send(InstagramHomeEffect.ShowMessage("刷新成功"))
                }
            } catch (e: Exception) {
                if (currentState is UiState.Success) {
                    _uiState.update {
                        it.copy(postsState = currentState.copy(refreshState = RefreshState.Idle))
                    }
                } else {
                    _uiState.update { it.copy(postsState = UiState.Error(e.message ?: "刷新失败")) }
                }
                _effect.send(InstagramHomeEffect.ShowMessage("刷新失败"))
            }
        }
    }

    private fun likePost(postId: String, user: ProfileUser) {
        viewModelScope.launch {
            repository.likePost(postId, user)
        }
    }

    private fun unlikePost(postId: String, user: ProfileUser) {
        viewModelScope.launch {
            repository.unlikePost(postId, user)
        }
    }

    private fun savePost(postId: String) {
        viewModelScope.launch {
            repository.savePost(postId)
            _effect.send(InstagramHomeEffect.ShowMessage("已保存到收藏夹"))
        }
    }

    private fun unsavePost(postId: String) {
        viewModelScope.launch {
            repository.unsavePost(postId)
            _effect.send(InstagramHomeEffect.ShowMessage("已从收藏夹移除"))
        }
    }

    private fun addComment(postId: String, user: ProfileUser, content: String) {
        viewModelScope.launch {
            repository.addComment(postId, user, content)
            _effect.send(InstagramHomeEffect.ShowMessage("评论已发布"))
        }
    }

    private fun deleteComment(postId: String, commentId: String) {
        viewModelScope.launch {
            repository.deleteComment(postId, commentId)
            _effect.send(InstagramHomeEffect.ShowMessage("删除评论成功"))
        }
    }

    private fun deletePost(postId: String) {
        viewModelScope.launch {
            repository.deletePost(postId)
            _effect.send(InstagramHomeEffect.ShowMessage("帖子已删除"))
        }
    }

    private fun createPost(user: ProfileUser, content: String, mediaList: List<InstagramMedia>, location: String?) {
        viewModelScope.launch {
            repository.createPost(user, content, mediaList, location)
            _effect.send(InstagramHomeEffect.ShowMessage("发布成功"))
            _effect.send(InstagramHomeEffect.ScrollToIndex(0))
        }
    }

    private fun showMessage(message: String) {
        viewModelScope.launch {
            _effect.send(InstagramHomeEffect.ShowMessage(message))
        }
    }
}
