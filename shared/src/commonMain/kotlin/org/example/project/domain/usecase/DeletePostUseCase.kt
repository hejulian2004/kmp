package org.example.project.domain.usecase

import org.example.project.domain.repository.FeedRepository

class DeletePostUseCase(private val repository: FeedRepository) {
    suspend operator fun invoke(postId: String) {
        repository.deletePost(postId)
    }
}

