/**
 * @File: AddNotificationUseCase.kt
 * @Package: org.example.project.domain.usecase
 * @Description: 新增朋友圈互动通知业务用例
 * @Date: 2026-07-20
 */
package org.example.project.domain.usecase

import org.example.project.domain.model.FeedNotification
import org.example.project.domain.repository.FeedRepository

class AddNotificationUseCase(private val repository: FeedRepository) {
    suspend operator fun invoke(feedNotification: FeedNotification) {
        repository.addNotification(feedNotification)
    }
}



