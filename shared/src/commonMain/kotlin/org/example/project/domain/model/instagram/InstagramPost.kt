/**
 * @File: InstagramPost.kt
 * @Package: org.example.project.domain.model.instagram
 * @Description: Instagram 帖子数据模型实体（对标 FeedLinePost）
 * @Date: 2026-07-28
 */
package org.example.project.domain.model.instagram

import org.example.project.platform.currentTimeMillis

data class InstagramPost(
    val id: String,
    val postUser: ProfileUser,
    val content: String,
    val mediaList: List<InstagramMedia> = emptyList(),
    val commentsList: List<InstagramComment> = emptyList(),
    val likedUsers: List<ProfileUser> = emptyList(),
    val isLiked: Boolean = false,
    val createTime: Long = currentTimeMillis(),
    val unreadNotificationCount: Int = 0,
    
    // Instagram 特有字段
    val location: String? = null,
    val taggedUsers: List<ProfileUser> = emptyList(),
    val hashtags: List<String> = emptyList(),
    val collaborators: List<ProfileUser> = emptyList(),
    val isSaved: Boolean = false,
    val savedCount: Long? = null,
    val shareCount: Long? = null,
    val viewCount: Long? = null,

    // 互动控制与高级开关
    val isCommentsDisabled: Boolean = false,
    val isLikeCountHidden: Boolean = false,
    val isPinned: Boolean = false,
    val audioTitle: String? = null
) {
    val likesCount: Int get() = likedUsers.size
    val commentsCount: Int get() = commentsList.size
}

// 向后兼容/通用别名
typealias PostModel = InstagramPost
typealias InstagramPostModel = InstagramPost
