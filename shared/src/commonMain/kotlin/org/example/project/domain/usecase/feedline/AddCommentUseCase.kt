/**
 * @File: AddCommentUseCase.kt
 * @Package: org.example.project.domain.usecase.feedline
 * @Description: 添加评论业务用例
 * @Author: 何聚敛
 * @Date: 2026-07-20
 */
package org.example.project.domain.usecase.feedline

import org.example.project.domain.model.feedline.FeedLineNotification
import org.example.project.domain.model.feedline.FeedLineUser
import org.example.project.domain.repository.feedline.FeedLineRepository
import kotlinx.coroutines.flow.first
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class AddCommentUseCase(private val repository: FeedLineRepository) {
    @OptIn(ExperimentalUuidApi::class)
    suspend operator fun invoke(postId: String, user: FeedLineUser, content: String, currentUserId: String): String {
        val result = repository.addComment(postId, user, content)
        val post = repository.getFeedPost(postId).first()
        if (post != null && post.postUser.id == currentUserId) {
            val newComment = post.commentsList.findLast { it.commentUser.id == user.id && it.content == content }
            repository.addNotification(
                FeedLineNotification(
                    id = Uuid.random().toString(),
                    post = post,
                    user = user,
                    comment = newComment,
                    isLikeNotification = false
                )
            )
        }
        return result
    }
}



