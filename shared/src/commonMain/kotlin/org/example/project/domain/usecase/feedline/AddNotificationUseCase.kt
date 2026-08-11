/**
 * @File: AddNotificationUseCase.kt
 * @Package: org.example.project.domain.usecase.feedline
 * @Description: 添加消息通知业务用例
 * @Author: 何聚敛
 * @Date: 2026-07-20
 */
package org.example.project.domain.usecase.feedline

import org.example.project.domain.model.feedline.FeedLineNotification
import org.example.project.domain.repository.feedline.FeedLineRepository

class AddNotificationUseCase(private val repository: FeedLineRepository) {
    suspend operator fun invoke(feedNotification: FeedLineNotification) {
        repository.addNotification(feedNotification)
    }
}



