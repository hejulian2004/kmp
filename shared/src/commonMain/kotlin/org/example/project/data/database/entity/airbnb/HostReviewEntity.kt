/**
 * @File: HostReviewEntity.kt
 * @Package: org.example.project.data.database.entity.airbnb
 * @Description: Airbnb房东评价信息的Room本地数据库实体
 * @Author: 何聚敛
 * @Date: 2026-08-05
 */
package org.example.project.data.database.entity.airbnb

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import org.example.project.domain.model.airbnb.HostReview

@Serializable
@Entity(tableName = "host_reviews")
data class HostReviewEntity(
    @PrimaryKey val id: String,
    val hostId: String,
    val reviewerName: String,
    val reviewerLocation: String,
    val reviewerAvatarUrl: String,
    val stars: Int,
    val dateText: String,
    val content: String,
) {
    fun toDomainModel(): HostReview = HostReview(
        id = id,
        hostId = hostId,
        reviewerName = reviewerName,
        reviewerLocation = reviewerLocation,
        reviewerAvatarUrl = reviewerAvatarUrl,
        stars = stars,
        dateText = dateText,
        content = content
    )

    companion object {
        fun fromDomainModel(domain: HostReview): HostReviewEntity = HostReviewEntity(
            id = domain.id,
            hostId = domain.hostId,
            reviewerName = domain.reviewerName,
            reviewerLocation = domain.reviewerLocation,
            reviewerAvatarUrl = domain.reviewerAvatarUrl,
            stars = domain.stars,
            dateText = domain.dateText,
            content = domain.content
        )
    }
}
