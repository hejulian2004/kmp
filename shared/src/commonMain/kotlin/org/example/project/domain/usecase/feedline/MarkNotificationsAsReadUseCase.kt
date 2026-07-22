/**
 * @File: MarkNotificationsAsReadUseCase.kt
 * @Package: org.example.project.domain.usecase.feedline
 * @Description: 将所有通知标记为已读业务用例
 * @Author: 何聚敛
 * @Date: 2026-07-20
 */
package org.example.project.domain.usecase.feedline

import org.example.project.domain.repository.feedline.FeedLineRepository

class MarkNotificationsAsReadUseCase(private val repository: FeedLineRepository) {
    suspend operator fun invoke() {
        repository.markAllNotificationsAsRead()
    }
}



