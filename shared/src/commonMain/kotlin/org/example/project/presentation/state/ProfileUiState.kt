package org.example.project.presentation.state

import org.example.project.model.ContentThumbnailData
import org.example.project.model.DiscoverUser
import org.example.project.model.ProfileUser

data class ProfileUiState(

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

    // 各 Tab 内容
    // 三个 Tab 独立持有分页状态，切换 Tab 时互不干扰
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
data class PostsSection(
    val posts: List<ContentThumbnailData> = emptyList(),
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = true,
)