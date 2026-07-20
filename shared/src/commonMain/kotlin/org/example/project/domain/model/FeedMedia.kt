/**
 * @File: FeedMedia.kt
 * @Package: org.example.project.domain.model
 * @Description: 朋友圈媒体内容数据模型(包含图片与视频)
 * @Date: 2026-07-20
 */
package org.example.project.domain.model

sealed interface FeedMedia {
    data class Image(
        val url: String,
        val width: Int? = null,
        val height: Int? = null
    ) : FeedMedia

    data class Video(
        val coverUrl: String? = null,
        val videoUrl: String,
        val durationSecond: Int? = null,
        val width: Int? = null,
        val height: Int? = null
    ) : FeedMedia
}



