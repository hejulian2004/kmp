/**
 * @File: FeedIntent.kt
 * @Package: org.example.project.presentation.feed
 * @Description: 朋友圈MVI架构中的用户意图与操作定义
 * @Date: 2026-07-20
 */
package org.example.project.presentation.feed

import org.example.project.domain.model.FeedComment
import org.example.project.domain.model.FeedMedia
import org.example.project.domain.model.FeedNotification
import org.example.project.domain.model.FeedUser

sealed interface FeedIntent {
    data object Refresh: FeedIntent

    data class LikePost(
        val postId: String,
        val user: FeedUser
    ): FeedIntent

    data class UnlikePost(
        val postId: String,
        val user: FeedUser
    ): FeedIntent

    data class AddComment(
        val postId: String,
        val user: FeedUser,
        val content: String,
    ): FeedIntent

    data class DeleteComment(
        val comment: FeedComment
    ): FeedIntent

    data class CreatePost(
        val user: FeedUser,
        val content: String,
        val mediaList: List<FeedMedia>
    ): FeedIntent

    data class UpdatePost(
        val postId: String,
        val content: String,
        val mediaList: List<FeedMedia>
    ): FeedIntent

    data class DeletePost(
        val postId: String
    ): FeedIntent

    data class ShowMessage(
        val message: String
    ): FeedIntent

    data class AddNotification(
        val feedNotification: FeedNotification
    ): FeedIntent

    data class DeleteCommentNotification(
        val feedNotification: FeedNotification
    ): FeedIntent

    data class DeleteLikeNotification(
        val feedNotification: FeedNotification
    ): FeedIntent

    data object ClearAllNotifications: FeedIntent

    data class NavigateTo(val screen: Screen): FeedIntent
}


