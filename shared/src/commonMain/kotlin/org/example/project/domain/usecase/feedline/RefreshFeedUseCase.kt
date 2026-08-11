/**
 * @File: RefreshFeedUseCase.kt
 * @Package: org.example.project.domain.usecase.feedline
 * @Description: 手动刷新朋友圈动态数据业务用例
 * @Author: 何聚敛
 * @Date: 2026-07-20
 */
package org.example.project.domain.usecase.feedline

import org.example.project.domain.repository.feedline.FeedLineRepository

class RefreshFeedUseCase(private val repository: FeedLineRepository) {
    suspend operator fun invoke() = repository.refreshFeed()
}



