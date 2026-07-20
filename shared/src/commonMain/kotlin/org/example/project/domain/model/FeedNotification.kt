/**
 * @File: FeedNotification.kt
 * @Package: org.example.project.domain.model
 * @Description: 朋友圈消息通知数据模型(点赞/评论)
 * @Date: 2026-07-20
 */
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



