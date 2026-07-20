package org.example.project.domain.usecase

import org.example.project.domain.repository.FeedRepository
import kotlinx.coroutines.flow.first

class ClearNotificationsUseCase(private val repository: FeedRepository) {
    suspend operator fun invoke() {
        val notifications = repository.getNotifications().first()
        notifications.forEach {
            if (it.isLikeNotification) {
                repository.deleteLikeNotification(it)
            } else {
                repository.deleteCommentNotification(it)
            }
        }
    }
}

