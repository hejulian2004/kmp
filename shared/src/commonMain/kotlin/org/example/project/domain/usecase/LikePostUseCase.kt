/**
 * @File: LikePostUseCase.kt
 * @Package: org.example.project.domain.usecase
 * @Description: 为指定动态帖子点赞的业务用例
 * @Date: 2026-07-20
 */
package org.example.project.domain.usecase

import org.example.project.domain.model.FeedNotification
import org.example.project.domain.model.FeedUser
import org.example.project.domain.repository.FeedRepository
import kotlinx.coroutines.flow.first
import java.util.UUID

class LikePostUseCase(private val repository: FeedRepository) {
    suspend operator fun invoke(postId: String, user: FeedUser, currentUserId: String): String {
        val result = repository.likePost(postId, user)
        val post = repository.getFeedPost(postId).first()
        if (post != null && post.postUser.id == currentUserId) {
            repository.addNotification(
                FeedNotification(
                    id = UUID.randomUUID().toString(),
                    post = post,
                    user = user,
                    isLikeNotification = true
                )
            )
        }
        return result
    }
}



