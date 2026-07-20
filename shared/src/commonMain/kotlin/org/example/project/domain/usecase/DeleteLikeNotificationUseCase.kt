/**
 * @File: DeleteLikeNotificationUseCase.kt
 * @Package: org.example.project.domain.usecase
 * @Description: 删除特定点赞通知业务用例
 * @Date: 2026-07-20
 */
package org.example.project.domain.usecase

import org.example.project.domain.model.FeedNotification
import org.example.project.domain.repository.FeedRepository

class DeleteLikeNotificationUseCase(private val repository: FeedRepository) {
    suspend operator fun invoke(feedNotification: FeedNotification) {
        repository.deleteLikeNotification(feedNotification)
    }
}



