package org.example.project.model


data class ContentThumbnailData(
    val id: String,
    val imageUrl: String,
    val type: PostType,
    val duration: String? = null,
)

enum class PostType { SINGLE, CAROUSEL, VIDEO, REEL }

