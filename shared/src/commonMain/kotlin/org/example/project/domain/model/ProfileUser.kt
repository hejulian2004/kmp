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


