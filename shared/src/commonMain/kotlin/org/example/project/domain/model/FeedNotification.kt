package org.example.project.domain.model

data class FeedNotification(
    val id: String,
    val post: FeedPost,
    val user: FeedUser,
    val comment: FeedComment? = null,
    val isLikeNotification: Boolean = false,
    val createdTime: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val isDelete: Boolean = false
)

