/**
 * @File: FeedLineUser.kt
 * @Package: org.example.project.domain.model.feedline
 * @Description: 朋友圈模块用户基础信息数据模型
 * @Author: 何聚敛
 * @Date: 2026-07-20
 */
package org.example.project.domain.model.feedline

import kotlinx.serialization.Serializable

@Serializable
data class FeedLineUser(
    val id: String,
    val name: String,
    val avatarUrl: String
)



