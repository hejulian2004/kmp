/**
 * @File: UnlikePostUseCase.kt
 * @Package: org.example.project.domain.usecase.feedline
 * @Description: 取消动态帖子点赞业务用例
 * @Author: 何聚敛
 * @Date: 2026-07-20
 */
package org.example.project.domain.usecase.feedline

import org.example.project.domain.model.feedline.FeedLineUser
import org.example.project.domain.repository.feedline.FeedLineRepository
import kotlinx.coroutines.flow.first

class UnlikePostUseCase(private val repository: FeedLineRepository) {
    suspend operator fun invoke(postId: String, user: FeedLineUser): String {
        val result = repository.unlikePost(postId, user)
        val notifications = repository.getNotifications().first()
        val likeNotification = notifications.find {
            it.post.id == postId && it.user.id == user.id && it.isLikeNotification
        }
        if (likeNotification != null) {
            repository.deleteLikeNotification(likeNotification)
        }
        return result
    }
}



