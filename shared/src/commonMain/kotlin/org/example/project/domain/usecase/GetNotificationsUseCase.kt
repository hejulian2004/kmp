package org.example.project.domain.usecase

import org.example.project.domain.model.FeedNotification
import org.example.project.domain.repository.FeedRepository
import kotlinx.coroutines.flow.Flow

class GetNotificationsUseCase(private val repository: FeedRepository) {
    operator fun invoke(): Flow<List<FeedNotification>> = repository.getNotifications()
}

