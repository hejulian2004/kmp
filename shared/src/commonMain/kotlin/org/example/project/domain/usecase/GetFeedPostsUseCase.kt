/**
 * @File: GetFeedPostsUseCase.kt
 * @Package: org.example.project.domain.usecase
 * @Description: 获取并监听朋友圈动态列表业务用例
 * @Date: 2026-07-20
 */
package org.example.project.domain.usecase

import org.example.project.domain.model.FeedPost
import org.example.project.domain.repository.FeedRepository
import kotlinx.coroutines.flow.Flow

class GetFeedPostsUseCase(private val repository: FeedRepository) {
    operator fun invoke(): Flow<List<FeedPost>> = repository.getFeedPosts()
}



