package org.example.project.domain.usecase

import org.example.project.domain.model.FeedMedia
import org.example.project.domain.repository.FeedRepository

class UpdatePostUseCase(private val repository: FeedRepository) {
    suspend operator fun invoke(postId: String, content: String, mediaList: List<FeedMedia>) {
        repository.updatePost(postId, content, mediaList)
    }
}

