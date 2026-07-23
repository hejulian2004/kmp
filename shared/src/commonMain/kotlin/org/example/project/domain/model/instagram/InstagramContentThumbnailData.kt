/**
 * @File: InstagramContentThumbnailData.kt
 * @Package: org.example.project.domain.model.instagram
 * @Description: Instagram 内容缩略图数据实体类
 * @Date: 2026-07-22
 */
package org.example.project.domain.model.instagram

data class InstagramContentThumbnailData(
    val id: String,
    val imageUrl: String,
    val type: PostType,
    val duration: String? = null,
)

typealias ContentThumbnailData = InstagramContentThumbnailData

enum class PostType { SINGLE, CAROUSEL, VIDEO, REEL }
