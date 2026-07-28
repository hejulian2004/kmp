/**
 * @File: InstagramMedia.kt
 * @Package: org.example.project.domain.model.instagram
 * @Description: Instagram 媒体数据模型（对标 FeedLineMedia）
 * @Date: 2026-07-28
 */
package org.example.project.domain.model.instagram

sealed interface InstagramMedia {
    data class Image(
        val url: String,
        val width: Int? = null,
        val height: Int? = null
    ) : InstagramMedia

    data class Video(
        val videoUrl: String,
        val coverUrl: String? = null,
        val durationSecond: Int? = null,
        val width: Int? = null,
        val height: Int? = null
    ) : InstagramMedia
}
