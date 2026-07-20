package org.example.project.domain.model

data class FeedPost (
    val id: String,
    val postUser: FeedUser,
    val content: String,
    val mediaList: List<FeedMedia> = emptyList(),
    val commentsList: List<FeedComment> = emptyList(),
    val likedUsers: List<FeedUser> = emptyList(),
    val isLiked: Boolean = false,
    val createTime: Long = System.currentTimeMillis(),
    val unreadNotificationCount: Int = 0
) {
    val likesCount: Int get() = likedUsers.size
    val commentsCount: Int get() = commentsList.size
}

