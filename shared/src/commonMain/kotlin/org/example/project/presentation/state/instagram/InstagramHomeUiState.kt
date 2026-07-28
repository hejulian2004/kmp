/**
 * @File: InstagramHomeUiState.kt
 * @Package: org.example.project.presentation.state.instagram
 * @Description: Instagram首页UI状态定义（全量使用InstagramPost实体）
 * @Author: 何聚敛
 * @Date: 2026-07-28
 */
package org.example.project.presentation.state.instagram

import org.example.project.domain.model.instagram.InstagramPost
import org.example.project.domain.model.instagram.ProfileUser
import org.example.project.presentation.state.UiState

/**
 * Instagram首页页面类型枚举
 */
enum class InstagramHomeScreenType {
    Home,
    DirectMessage,
    Notifications,
    Publish
}

/**
 * Instagram首页UI状态模型
 *
 * @param currentUser 当前登录用户
 * @param postsState Feed动态帖子列表加载状态(Loading, Success, Error)
 * @param storiesState 顶部Story快拍列表加载状态(Loading, Success, Error)
 * @param unreadMessageCount 未读私信数量
 * @param unreadNotificationCount 未读通知数量
 * @param currentScreen 当前所在的二级子页面
 */
data class InstagramHomeUiState(
    val currentUser: ProfileUser = ProfileUser(
        userId = "u_me",
        username = "hejulian",
        avatarUrl = "https://picsum.photos/seed/me/200/200",
        signature = "Kotlin KMP Developer",
        postCount = "18",
        followerCount = "450",
        followingCount = "320"
    ),
    val postsState: UiState<List<InstagramPost>> = UiState.Loading,
    val storiesState: UiState<List<InstagramPost>> = UiState.Loading,
    val unreadMessageCount: Int = 3,
    val unreadNotificationCount: Int = 5,
    val currentScreen: InstagramHomeScreenType = InstagramHomeScreenType.Home
)
