/**
 * @File: MarkNotificationsAsReadUseCase.kt
 * @Package: org.example.project.domain.usecase
 * @Description: 将所有通知标记为已读业务用例
 * @Date: 2026-07-20
 */
package org.example.project.domain.usecase

import org.example.project.domain.repository.FeedRepository

class MarkNotificationsAsReadUseCase(private val repository: FeedRepository) {
    suspend operator fun invoke() {
        repository.markAllNotificationsAsRead()
    }
}



