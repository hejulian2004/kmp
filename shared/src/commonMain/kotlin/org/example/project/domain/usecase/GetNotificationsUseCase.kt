/**
 * @File: GetNotificationsUseCase.kt
 * @Package: org.example.project.domain.usecase
 * @Description: 获取并监听互动通知列表业务用例
 * @Date: 2026-07-20
 */
package org.example.project.domain.usecase

import org.example.project.domain.model.FeedNotification
import org.example.project.domain.repository.FeedRepository
import kotlinx.coroutines.flow.Flow

class GetNotificationsUseCase(private val repository: FeedRepository) {
    operator fun invoke(): Flow<List<FeedNotification>> = repository.getNotifications()
}



