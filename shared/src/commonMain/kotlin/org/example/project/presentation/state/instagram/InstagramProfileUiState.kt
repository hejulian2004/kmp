package org.example.project.presentation.state.instagram

import org.example.project.domain.model.instagram.ContentThumbnailData
import org.example.project.domain.model.instagram.DiscoverUser
import org.example.project.domain.model.instagram.ProfileUser
import org.example.project.presentation.state.UiState

data class InstagramProfileUiState(

    // 用户信息
    val profileState: UiState<ProfileUser> = UiState.Idle,

    // TopBar
    val isTopBarVisible: Boolean = true,
    val isTitleExpanded: Boolean = false,

    // 发现用户区域
    val discoverState: UiState<List<DiscoverUser>> = UiState.Idle,

    val isDiscoverVisible: Boolean = false,

    // Tab
    val selectedTabId: String = TAB_POSTS,

    // 各Tab内容
    // 三个Tab独立持有分页状态，切换Tab时互不干扰
    val postsSection: UiState<PostsSection> = UiState.Idle,
    val reelsSection: UiState<PostsSection> = UiState.Idle,
    val taggedSection: UiState<PostsSection> = UiState.Idle,

    ) {
    companion object {
        const val TAB_POSTS  = "posts"
        const val TAB_REELS  = "reels"
        const val TAB_TAGGED = "tagged"
    }
}

typealias ProfileUiState = InstagramProfileUiState

data class PostsSection(
    val posts: List<ContentThumbnailData> = emptyList(),
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = true,
)
