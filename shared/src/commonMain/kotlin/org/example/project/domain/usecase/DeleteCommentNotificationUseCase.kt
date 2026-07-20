package org.example.project.domain.usecase

import org.example.project.domain.model.FeedNotification
import org.example.project.domain.repository.FeedRepository

class DeleteCommentNotificationUseCase(private val repository: FeedRepository) {
    suspend operator fun invoke(feedNotification: FeedNotification) {
        repository.deleteCommentNotification(feedNotification)
    }
}

