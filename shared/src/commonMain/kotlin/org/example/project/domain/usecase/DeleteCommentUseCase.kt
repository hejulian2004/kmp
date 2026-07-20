/**
 * @File: DeleteCommentUseCase.kt
 * @Package: org.example.project.domain.usecase
 * @Description: 删除指定的帖子评论业务用例
 * @Date: 2026-07-20
 */
package org.example.project.domain.usecase

import org.example.project.domain.model.FeedComment
import org.example.project.domain.repository.FeedRepository
import kotlinx.coroutines.flow.first

class DeleteCommentUseCase(private val repository: FeedRepository) {
    suspend operator fun invoke(comment: FeedComment): String {
        val result = repository.deleteComment(comment)
        val notifications = repository.getNotifications().first()
        val commentNotification = notifications.find { it.comment?.id == comment.id }
        if (commentNotification != null) {
            repository.deleteCommentNotification(commentNotification)
        }
        return result
    }
}



