package org.example.project.domain

data class PostModel(
    val id: String,
    val userId: String,
    val createdAt: Long,
    val updatedAt: Long,
    val type: PostType,
    val thumbnail: String?,
    val summary: String,
    val contentId: String
)

sealed class PostContent {
    abstract val title: String

    abstract val id: String
    data class ImageTextContent(
        override val id: String,
        override val title: String,
        val body: String,
        val images: List<String>,
    ) : PostContent()

    data class VideoContent(
        override val id: String,
        override val title: String,
        val description: String,
        val videoUrl: String,
        val coverUrl: String,
    ) : PostContent()
}