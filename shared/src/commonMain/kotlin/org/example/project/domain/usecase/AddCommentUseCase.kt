/**
 * @File: AddCommentUseCase.kt
 * @Package: org.example.project.domain.usecase
 * @Description: 添加朋友圈评论的具体业务用例
 * @Date: 2026-07-20
 */
package org.example.project.domain.usecase

import org.example.project.domain.model.FeedNotification
import org.example.project.domain.model.FeedUser
import org.example.project.domain.repository.FeedRepository
import kotlinx.coroutines.flow.first
import java.util.UUID

class AddCommentUseCase(private val repository: FeedRepository) {
    suspend operator fun invoke(postId: String, user: FeedUser, content: String, currentUserId: String): String {
        val result = repository.addComment(postId, user, content)
        val post = repository.getFeedPost(postId).first()
        if (post != null && post.postUser.id == currentUserId) {
            val newComment = post.commentsList.findLast { it.commentUser.id == user.id && it.content == content }
            repository.addNotification(
                FeedNotification(
                    id = UUID.randomUUID().toString(),
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



