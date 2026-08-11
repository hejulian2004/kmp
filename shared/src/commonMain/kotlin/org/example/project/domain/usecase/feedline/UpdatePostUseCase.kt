/**
 * @File: UpdatePostUseCase.kt
 * @Package: org.example.project.domain.usecase.feedline
 * @Description: 更新已发布的动态帖子业务用例
 * @Author: 何聚敛
 * @Date: 2026-07-20
 */
package org.example.project.domain.usecase.feedline

import org.example.project.domain.model.feedline.FeedLineMedia
import org.example.project.domain.repository.feedline.FeedLineRepository

class UpdatePostUseCase(private val repository: FeedLineRepository) {
    suspend operator fun invoke(postId: String, content: String, mediaList: List<FeedLineMedia>) {
        repository.updatePost(postId, content, mediaList)
    }
}



