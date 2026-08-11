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
import org.example.project.presentation.state.UiState

enum class Screen {
    Feed, Notification, Publish
}

data class FeedUiState(
    val currentUser: FeedLineUser,
    val currentScreen: Screen = Screen.Feed,
    val feedState: UiState<List<FeedLinePost>> = UiState.Idle,
    val notificationsState: UiState<List<FeedLineNotification>> = UiState.Idle,
    val unreadNotificationCount: Int = 0
)
