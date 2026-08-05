/**
 * @File: InstagramProfileUser.kt
 * @Package: org.example.project.domain.model.instagram
 * @Description: Instagram 用户主页个人信息数据模型
 * @Author: 何聚敛
 * @Date: 2026-08-05
 */
package org.example.project.domain.model.instagram

import kotlinx.serialization.Serializable

@Serializable
data class InstagramProfileUser(
    val userId: String,
    val username: String,
    val avatarUrl: String,
    val signature: String,
    val postCount: String,
    val followerCount: String,
    val followingCount: String,
    val isFollowing: Boolean = false
)

typealias ProfileUser = InstagramProfileUser

@Serializable
data class InstagramDiscoverUser(
    val userId: String,
    val username: String,
    val avatarUrl: String,
    val extraInfo: String? = null,
)

typealias DiscoverUser = InstagramDiscoverUser
