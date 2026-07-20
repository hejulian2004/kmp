package org.example.project.domain.model

data class FeedComment (
    val id: String,
    val postId: String,
    val commentUser: FeedUser,
    val content: String,
    val createTime: Long = System.currentTimeMillis()
)

