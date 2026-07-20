/**
 * @File: CreatePostUseCase.kt
 * @Package: org.example.project.domain.usecase
 * @Description: 发布新的朋友圈动态业务用例
 * @Date: 2026-07-20
 */
package org.example.project.domain.usecase

import org.example.project.domain.model.FeedMedia
import org.example.project.domain.model.FeedUser
import org.example.project.domain.repository.FeedRepository

class CreatePostUseCase(private val repository: FeedRepository) {
    suspend operator fun invoke(user: FeedUser, content: String, mediaList: List<FeedMedia>) {
        repository.createPost(user, content, mediaList)
    }
}



