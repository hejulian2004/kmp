/**
 * @File: FeedIntent.kt
 * @Package: org.example.project.presentation.intent.feedline
 * @Description: 朋友圈MVI架构中的用户意图与操作定义
 * @Author: 何聚敛
 * @Date: 2026-08-11
 */
package org.example.project.presentation.intent.feedline

import org.example.project.domain.model.feedline.FeedLineComment
import org.example.project.domain.model.feedline.FeedLineMedia
import org.example.project.domain.model.feedline.FeedLineNotification
import org.example.project.domain.model.feedline.FeedLineUser
import org.example.project.presentation.state.feedline.Screen
sealed interface FeedIntent {
    data object Refresh: FeedIntent

    data class LikePost(
        val postId: String,
        val user: FeedLineUser
    ): FeedIntent

    data class UnlikePost(
        val postId: String,
        val user: FeedLineUser
    ): FeedIntent

    data class AddComment(
        val postId: String,
        val user: FeedLineUser,
        val content: String,
    ): FeedIntent

    data class DeleteComment(
        val comment: FeedLineComment
    ): FeedIntent

    data class CreatePost(
        val user: FeedLineUser,
        val content: String,
        val mediaList: List<FeedLineMedia>
    ): FeedIntent

    data class UpdatePost(
        val postId: String,
        val content: String,
        val mediaList: List<FeedLineMedia>
    ): FeedIntent

    data class DeletePost(
        val postId: String
    ): FeedIntent

    data class ShowMessage(
        val message: String
    ): FeedIntent

    data class AddNotification(
        val feedNotification: FeedLineNotification
    ): FeedIntent

    data class DeleteCommentNotification(
        val feedNotification: FeedLineNotification
    ): FeedIntent

    data class DeleteLikeNotification(
        val feedNotification: FeedLineNotification
    ): FeedIntent

    data object ClearAllNotifications: FeedIntent

    data class NavigateTo(val screen: Screen): FeedIntent

    data object LoadMore: FeedIntent

    data class PreviewMedia(
        val postId: String,
        val mediaUrl: String,
        val isVideo: Boolean
    ): FeedIntent

    data class ViewUserProfile(
        val targetUserId: String,
        val clickSource: String
    ): FeedIntent

    data class ClickNotificationBar(
        val unreadCount: Int
    ): FeedIntent

    data class SelectMedia(
        val sourceType: String,
        val mediaCount: Int
    ): FeedIntent

    data class CancelPublish(
        val hasContent: Boolean
    ): FeedIntent

    data object LongClickCreatePostTextOnly: FeedIntent
}