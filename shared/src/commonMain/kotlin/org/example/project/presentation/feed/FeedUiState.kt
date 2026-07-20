/**
 * @File: FeedUiState.kt
 * @Package: org.example.project.presentation.feed
 * @Description: 朋友圈MVI架构中的界面状态数据定义
 * @Date: 2026-07-20
 */
package org.example.project.presentation.feed

import org.example.project.domain.model.FeedNotification
import org.example.project.domain.model.FeedPost
import org.example.project.domain.model.FeedUser

enum class Screen {
    Feed, Notification, Publish
}

data class FeedUiState(
    val currentUser: FeedUser,
    val currentScreen: Screen = Screen.Feed,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val posts: List<FeedPost> = emptyList(),
    val notifications: List<FeedNotification> = emptyList(),
    val unreadNotificationCount: Int = 0,
    val errorMessage: String? = null
)



