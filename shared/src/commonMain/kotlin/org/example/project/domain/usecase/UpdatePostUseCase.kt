/**
 * @File: UpdatePostUseCase.kt
 * @Package: org.example.project.domain.usecase
 * @Description: 更新指定帖子内容的业务用例
 * @Date: 2026-07-20
 */
package org.example.project.domain.usecase

import org.example.project.domain.model.FeedMedia
import org.example.project.domain.repository.FeedRepository

class UpdatePostUseCase(private val repository: FeedRepository) {
    suspend operator fun invoke(postId: String, content: String, mediaList: List<FeedMedia>) {
        repository.updatePost(postId, content, mediaList)
    }
}



