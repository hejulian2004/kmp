/**
 * @File: FeedLinePost.kt
 * @Package: org.example.project.domain.model.feedline
 * @Description: 朋友圈动态图文/视频帖子核心数据模型
 * @Author: 何聚敛
 * @Date: 2026-07-20
 */
package org.example.project.domain.model.feedline

import org.example.project.platform.currentTimeMillis

data class FeedLinePost (
    val id: String,
    val postUser: FeedLineUser,
    val content: String,
    val mediaList: List<FeedLineMedia> = emptyList(),
    val commentsList: List<FeedLineComment> = emptyList(),
    val likedUsers: List<FeedLineUser> = emptyList(),
    val isLiked: Boolean = false,
    val createTime: Long = currentTimeMillis(),
    val unreadNotificationCount: Int = 0
) {
    val likesCount: Int get() = likedUsers.size
    val commentsCount: Int get() = commentsList.size
}



