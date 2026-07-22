/**
 * @File: GetNotificationsUseCase.kt
 * @Package: org.example.project.domain.usecase.feedline
 * @Description: 获取并监听互动通知列表业务用例
 * @Author: 何聚敛
 * @Date: 2026-07-20
 */
package org.example.project.domain.usecase.feedline

import org.example.project.domain.model.feedline.FeedLineNotification
import org.example.project.domain.repository.feedline.FeedLineRepository
import kotlinx.coroutines.flow.Flow

class GetNotificationsUseCase(private val repository: FeedLineRepository) {
    operator fun invoke(): Flow<List<FeedLineNotification>> = repository.getNotifications()
}



