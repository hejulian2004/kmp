package org.example.project.domain.usecase

import org.example.project.domain.repository.FeedRepository

class MarkNotificationsAsReadUseCase(private val repository: FeedRepository) {
    suspend operator fun invoke() {
        repository.markAllNotificationsAsRead()
    }
}

