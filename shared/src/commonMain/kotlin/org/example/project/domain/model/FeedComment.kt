/**
 * @File: FeedComment.kt
 * @Package: org.example.project.domain.model
 * @Description: 朋友圈评论数据模型
 * @Date: 2026-07-20
 */
package org.example.project.domain.model

data class FeedComment (
    val id: String,
    val postId: String,
    val commentUser: FeedUser,
    val content: String,
    val createTime: Long = System.currentTimeMillis()
)



