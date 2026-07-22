/**
 * @File: FeedLineRepository.kt
 * @Package: org.example.project.domain.repository.feedline
 * @Description: 朋友圈业务数据仓库抽象接口
 * @Author: 何聚敛
 * @Date: 2026-07-20
 */
package org.example.project.domain.repository.feedline

import kotlinx.coroutines.flow.Flow
import org.example.project.domain.model.feedline.FeedLineComment
import org.example.project.domain.model.feedline.FeedLineMedia
import org.example.project.domain.model.feedline.FeedLineNotification
import org.example.project.domain.model.feedline.FeedLinePost
import org.example.project.domain.model.feedline.FeedLineUser
interface FeedLineRepository {
    fun getFeedPosts(): Flow<List<FeedLinePost>>

    fun getFeedPost(postId: String): Flow<FeedLinePost?>

    suspend fun refreshFeed()

    suspend fun likePost(
        postId: String,
        user: FeedLineUser
    ): String

    suspend fun getLikedUsers(
        postId: String
    ): List<FeedLineUser>

    suspend fun unlikePost(
        postId: String,
        user: FeedLineUser
    ): String

    suspend fun addComment(
        postId: String,
        commentUser: FeedLineUser,
        content: String
    ): String

    suspend fun getComments(
        postId: String
    ): List<FeedLineComment>

    suspend fun deleteComment(
        comment: FeedLineComment
    ): String

    suspend fun createPost(
        user: FeedLineUser,
        content: String,
        mediaList: List<FeedLineMedia>
    )

    suspend fun deletePost(
        postId: String
    )

    suspend fun updatePost(
        postId: String,
        content: String,
        mediaList: List<FeedLineMedia>
    )

    suspend fun addNotification(
        feedNotification: FeedLineNotification
    )

    suspend fun deleteCommentNotification(
        feedNotification: FeedLineNotification
    )

    suspend fun deleteLikeNotification(
        feedNotification: FeedLineNotification
    )

    fun getNotifications(): Flow<List<FeedLineNotification>>

    suspend fun markAllNotificationsAsRead()
}