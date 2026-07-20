/**
 * @File: ContentThumbnailData.kt
 * @Package: org.example.project.domain.model
 * @Description: 内容缩略图数据实体类
 * @Date: 2026-07-20
 */
package org.example.project.domain.model


data class ContentThumbnailData(
    val id: String,
    val imageUrl: String,
    val type: PostType,
    val duration: String? = null,
)

enum class PostType { SINGLE, CAROUSEL, VIDEO, REEL }



