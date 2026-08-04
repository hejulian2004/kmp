/**
 * @File: InstagramComment.kt
 * @Package: org.example.project.domain.model.instagram
 * @Description: Instagram 评论数据模型（对标 FeedLineComment）
 * @Author: 何聚敛
 * @Date: 2026-08-04
 */
package org.example.project.domain.model.instagram

import kotlinx.serialization.Serializable
import org.example.project.platform.currentTimeMillis

@Serializable
data class InstagramComment(
    val id: String,
    val postId: String,
    val commentUser: ProfileUser,
    val content: String,
    val createTime: Long = currentTimeMillis()
)
