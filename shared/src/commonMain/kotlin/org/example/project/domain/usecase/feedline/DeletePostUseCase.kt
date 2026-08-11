/**
 * @File: DeletePostUseCase.kt
 * @Package: org.example.project.domain.usecase.feedline
 * @Description: 删除指定动态帖子业务用例
 * @Author: 何聚敛
 * @Date: 2026-07-20
 */
package org.example.project.domain.usecase.feedline

import org.example.project.domain.repository.feedline.FeedLineRepository

class DeletePostUseCase(private val repository: FeedLineRepository) {
    suspend operator fun invoke(postId: String) {
        repository.deletePost(postId)
    }
}



