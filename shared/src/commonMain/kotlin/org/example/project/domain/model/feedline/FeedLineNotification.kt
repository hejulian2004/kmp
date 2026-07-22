/**
 * @File: FeedLineNotification.kt
 * @Package: org.example.project.domain.model.feedline
 * @Description: 朋友圈消息通知数据模型(点赞/评论)
 * @Author: 何聚敛
 * @Date: 2026-07-20
 */
package org.example.project.domain.model.feedline

import org.example.project.platform.currentTimeMillis

data class FeedLineNotification(
    val id: String,
    val post: FeedLinePost,
    val user: FeedLineUser,
    val comment: FeedLineComment? = null,
    val isLikeNotification: Boolean = false,
    val createdTime: Long = currentTimeMillis(),
    val isRead: Boolean = false,
    val isDelete: Boolean = false
)



