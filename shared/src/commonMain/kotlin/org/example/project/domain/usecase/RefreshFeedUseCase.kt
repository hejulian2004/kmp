/**
 * @File: RefreshFeedUseCase.kt
 * @Package: org.example.project.domain.usecase
 * @Description: 下拉刷新朋友圈动态数据业务用例
 * @Date: 2026-07-20
 */
package org.example.project.domain.usecase

import org.example.project.domain.repository.FeedRepository

class RefreshFeedUseCase(private val repository: FeedRepository) {
    suspend operator fun invoke() = repository.refreshFeed()
}



