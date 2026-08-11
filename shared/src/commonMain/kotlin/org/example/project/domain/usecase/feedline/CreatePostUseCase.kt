/**
 * @File: CreatePostUseCase.kt
 * @Package: org.example.project.domain.usecase.feedline
 * @Description: 发布新动态帖子业务用例
 * @Author: 何聚敛
 * @Date: 2026-07-20
 */
package org.example.project.domain.usecase.feedline

import org.example.project.domain.model.feedline.FeedLineMedia
import org.example.project.domain.model.feedline.FeedLineUser
import org.example.project.domain.repository.feedline.FeedLineRepository

class CreatePostUseCase(private val repository: FeedLineRepository) {
    suspend operator fun invoke(user: FeedLineUser, content: String, mediaList: List<FeedLineMedia>) {
        repository.createPost(user, content, mediaList)
    }
}



