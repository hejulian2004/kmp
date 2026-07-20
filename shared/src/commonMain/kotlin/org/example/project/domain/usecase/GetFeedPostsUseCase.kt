package org.example.project.domain.usecase

import org.example.project.domain.model.FeedPost
import org.example.project.domain.repository.FeedRepository
import kotlinx.coroutines.flow.Flow

class GetFeedPostsUseCase(private val repository: FeedRepository) {
    operator fun invoke(): Flow<List<FeedPost>> = repository.getFeedPosts()
}

