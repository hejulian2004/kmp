/**
 * @File: DeleteCommentNotificationUseCase.kt
 * @Package: org.example.project.domain.usecase.feedline
 * @Description: 删除评论通知业务用例
 * @Author: 何聚敛
 * @Date: 2026-07-20
 */
package org.example.project.domain.usecase.feedline

import org.example.project.domain.model.feedline.FeedLineNotification
import org.example.project.domain.repository.feedline.FeedLineRepository

class DeleteCommentNotificationUseCase(private val repository: FeedLineRepository) {
    suspend operator fun invoke(feedNotification: FeedLineNotification) {
        repository.deleteCommentNotification(feedNotification)
    }
}



