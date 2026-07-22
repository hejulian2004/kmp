/**
 * @File: FeedLineComment.kt
 * @Package: org.example.project.domain.model.feedline
 * @Description: 朋友圈动态评论数据模型
 * @Author: 何聚敛
 * @Date: 2026-07-20
 */
package org.example.project.domain.model.feedline

import org.example.project.platform.currentTimeMillis

data class FeedLineComment (
    val id: String,
    val postId: String,
    val commentUser: FeedLineUser,
    val content: String,
    val createTime: Long = currentTimeMillis()
)



