/**
 * @File: ProfileUser.kt
 * @Package: org.example.project.domain.model
 * @Description: 用户主页个人信息数据模型
 * @Date: 2026-07-20
 */
package org.example.project.domain.model

data class ProfileUser(
    val userId: String,
    val username: String,
    val avatarUrl: String,
    val signature: String,
    val postCount: String,
    val followerCount: String,
    val followingCount: String,
)

data class DiscoverUser(
    val userId: String,
    val username: String, 
    val avatarUrl: String,
    val extraInfo: String? = null,
)




