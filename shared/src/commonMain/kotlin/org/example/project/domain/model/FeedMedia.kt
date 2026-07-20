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

