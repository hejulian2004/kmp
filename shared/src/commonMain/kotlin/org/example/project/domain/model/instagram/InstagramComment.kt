/**
 * @File: InstagramComment.kt
 * @Package: org.example.project.domain.model.instagram
 * @Description: Instagram 评论数据模型（对标 FeedLineComment）
 * @Date: 2026-07-28
 */
package org.example.project.domain.model.instagram

import org.example.project.platform.currentTimeMillis

data class InstagramComment(
    val id: String,
    val postId: String,
    val commentUser: ProfileUser,
    val content: String,
    val createTime: Long = currentTimeMillis()
)
