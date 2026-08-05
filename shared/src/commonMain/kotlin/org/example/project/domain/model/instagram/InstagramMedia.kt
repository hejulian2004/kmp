/**
 * @File: InstagramMedia.kt
 * @Package: org.example.project.domain.model.instagram
 * @Description: Instagram 媒体数据模型（对标 FeedLineMedia）
 * @Author: 何聚敛
 * @Date: 2026-08-05
 */
package org.example.project.domain.model.instagram

import kotlinx.serialization.Serializable

@Serializable
sealed interface InstagramMedia {
    @Serializable
    data class Image(
        val url: String,
        val width: Int? = null,
        val height: Int? = null
    ) : InstagramMedia

    @Serializable
    data class Video(
        val videoUrl: String,
        val coverUrl: String? = null,
        val durationSecond: Int? = null,
        val width: Int? = null,
        val height: Int? = null
    ) : InstagramMedia
}
