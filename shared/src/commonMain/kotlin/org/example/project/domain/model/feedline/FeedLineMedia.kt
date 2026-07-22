/**
 * @File: FeedLineMedia.kt
 * @Package: org.example.project.domain.model.feedline
 * @Description: 朋友圈动态媒体内容密封接口(图片/视频)
 * @Author: 何聚敛
 * @Date: 2026-07-20
 */
package org.example.project.domain.model.feedline

sealed interface FeedLineMedia {
    data class Image(
        val url: String,
        val width: Int? = null,
        val height: Int? = null
    ) : FeedLineMedia

    data class Video(
        val coverUrl: String? = null,
        val videoUrl: String,
        val durationSecond: Int? = null,
        val width: Int? = null,
        val height: Int? = null
    ) : FeedLineMedia
}



