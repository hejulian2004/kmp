/**
 * @File: InstagramProfileUser.kt
 * @Package: org.example.project.domain.model.instagram
 * @Description: Instagram 用户主页个人信息数据模型
 * @Date: 2026-07-22
 */
package org.example.project.domain.model.instagram

data class InstagramProfileUser(
    val userId: String,
    val username: String,
    val avatarUrl: String,
    val signature: String,
    val postCount: String,
    val followerCount: String,
    val followingCount: String,
)

typealias ProfileUser = InstagramProfileUser

data class InstagramDiscoverUser(
    val userId: String,
    val username: String,
    val avatarUrl: String,
    val extraInfo: String? = null,
)

typealias DiscoverUser = InstagramDiscoverUser
