/**
 * @File: DeleteCommentUseCase.kt
 * @Package: org.example.project.domain.usecase.feedline
 * @Description: 删除评论业务用例
 * @Author: 何聚敛
 * @Date: 2026-07-20
 */
package org.example.project.domain.usecase.feedline

import org.example.project.domain.model.feedline.FeedLineComment
import org.example.project.domain.repository.feedline.FeedLineRepository
import kotlinx.coroutines.flow.first

class DeleteCommentUseCase(private val repository: FeedLineRepository) {
    suspend operator fun invoke(comment: FeedLineComment): String {
        val result = repository.deleteComment(comment)
        val notifications = repository.getNotifications().first()
        val commentNotification = notifications.find { it.comment?.id == comment.id }
        if (commentNotification != null) {
            repository.deleteCommentNotification(commentNotification)
        }
        return result
    }
}



