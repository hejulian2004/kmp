/**
 * @File: HostProfileModels.kt
 * @Package: org.example.project.domain.model.airbnb
 * @Description: Airbnb房东与房源领域数据模型（从原仓库合并，保留原字段与核心接口结构）
 * @Author: 何聚敛
 * @Date: 2026-08-05
 */
package org.example.project.domain.model.airbnb

import kotlinx.serialization.Serializable

@Serializable
data class Host(
    val id: String,
    val name: String,
    val reviewCount: Int,
    val rating: Double,
    val yearsHosting: Int,
    val totalListings: Int,
    val languages: String,
    val identityVerified: Boolean,
    val superHost: Boolean,
    val about: String,
    val occupation: String = "",
    val livesIn: String = "",
    val hobbies: List<String>,
    val places: List<String> = emptyList(),
    val placesVisible: Boolean = true,
    val avatarUrl: String,
)

@Serializable
data class PropertyListing(
    val id: String,
    val hostId: String,
    val title: String,
    val subtitle: String,
    val rating: Double,
    val reviewCount: Int,
    val imageUrl: String,
)

@Serializable
data class HostReview(
    val id: String,
    val hostId: String,
    val reviewerName: String,
    val reviewerLocation: String,
    val reviewerAvatarUrl: String,
    val stars: Int,
    val dateText: String,
    val content: String,
)

@Serializable
data class TravelGuide(
    val id: String,
    val hostId: String,
    val title: String,
)
