/**
 * @File: LikePostUseCase.kt
 * @Package: org.example.project.domain.usecase.feedline
 * @Description: 给动态帖子点赞业务用例
 * @Author: 何聚敛
 * @Date: 2026-07-20
 */
package org.example.project.domain.usecase.feedline

import org.example.project.domain.model.feedline.FeedLineNotification
import org.example.project.domain.model.feedline.FeedLineUser
import org.example.project.domain.repository.feedline.FeedLineRepository
import kotlinx.coroutines.flow.first
import java.util.UUID

class LikePostUseCase(private val repository: FeedLineRepository) {
    suspend operator fun invoke(postId: String, user: FeedLineUser, currentUserId: String): String {
        val result = repository.likePost(postId, user)
        val post = repository.getFeedPost(postId).first()
        if (post != null && post.postUser.id == currentUserId) {
            repository.addNotification(
                FeedLineNotification(
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



