/**
 * @File: DeletePostUseCase.kt
 * @Package: org.example.project.domain.usecase
 * @Description: 删除指定的动态帖子业务用例
 * @Date: 2026-07-20
 */
package org.example.project.domain.usecase

import org.example.project.domain.repository.FeedRepository

class DeletePostUseCase(private val repository: FeedRepository) {
    suspend operator fun invoke(postId: String) {
        repository.deletePost(postId)
    }
}



