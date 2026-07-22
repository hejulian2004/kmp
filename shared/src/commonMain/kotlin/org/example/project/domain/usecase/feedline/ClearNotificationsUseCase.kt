/**
 * @File: ClearNotificationsUseCase.kt
 * @Package: org.example.project.domain.usecase.feedline
 * @Description: 清空互动通知列表业务用例
 * @Author: 何聚敛
 * @Date: 2026-07-20
 */
package org.example.project.domain.usecase.feedline

import org.example.project.domain.repository.feedline.FeedLineRepository
import kotlinx.coroutines.flow.first

class ClearNotificationsUseCase(private val repository: FeedLineRepository) {
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



