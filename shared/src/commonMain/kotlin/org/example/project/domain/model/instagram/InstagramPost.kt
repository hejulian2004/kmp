/**
 * @File: InstagramPost.kt
 * @Package: org.example.project.domain.model.instagram
 * @Description: Instagram 模块帖子数据模型实体
 * @Date: 2026-07-22
 */
package org.example.project.domain.model.instagram

data class InstagramPostModel(
    val id: String,
    val userId: String,
    val createdAt: Long,
    val updatedAt: Long,
    val type: PostType,
    val thumbnail: String?,
    val summary: String,
    val contentId: String
)

// 向后兼容/通用别名
typealias PostModel = InstagramPostModel

sealed class InstagramPostContent {
    abstract val title: String
    abstract val id: String

    data class ImageTextContent(
        override val id: String,
        override val title: String,
        val body: String,
        val images: List<String>,
    ) : InstagramPostContent()

    data class VideoContent(
        override val id: String,
        override val title: String,
        val description: String,
        val videoUrl: String,
        val coverUrl: String,
    ) : InstagramPostContent()
}

typealias PostContent = InstagramPostContent
