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

