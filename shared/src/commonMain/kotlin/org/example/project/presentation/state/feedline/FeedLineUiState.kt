/**
 * @File: FeedLineUiState.kt
 * @Package: org.example.project.presentation.state.feedline
 * @Description: 朋友圈MVI架构中的界面状态数据定义
 * @Author: 何聚敛
 * @Date: 2026-07-20
 */
package org.example.project.presentation.state.feedline

import org.example.project.domain.model.feedline.FeedLineNotification
import org.example.project.domain.model.feedline.FeedLinePost
import org.example.project.domain.model.feedline.FeedLineUser

enum class Screen {
    Feed, Notification, Publish
}

data class FeedUiState(
    val currentUser: FeedLineUser,
    val currentScreen: Screen = Screen.Feed,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val posts: List<FeedLinePost> = emptyList(),
    val notifications: List<FeedLineNotification> = emptyList(),
    val unreadNotificationCount: Int = 0,
    val errorMessage: String? = null
)



