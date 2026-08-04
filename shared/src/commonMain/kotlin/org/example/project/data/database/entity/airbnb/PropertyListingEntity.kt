/**
 * @File: PropertyListingEntity.kt
 * @Package: org.example.project.data.database.entity.airbnb
 * @Description: Airbnb房源列表的Room本地数据库实体
 * @Author: 何聚敛
 * @Date: 2026-08-04
 */
package org.example.project.data.database.entity.airbnb

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.example.project.domain.model.airbnb.PropertyListing

@Entity(tableName = "property_listings")
data class PropertyListingEntity(
    @PrimaryKey val id: String,
    val hostId: String,
    val title: String,
    val subtitle: String,
    val rating: Double,
    val reviewCount: Int,
    val imageUrl: String,
) {
    fun toDomainModel(): PropertyListing = PropertyListing(
        id = id,
        hostId = hostId,
        title = title,
        subtitle = subtitle,
        rating = rating,
        reviewCount = reviewCount,
        imageUrl = imageUrl
    )

    companion object {
        fun fromDomainModel(domain: PropertyListing): PropertyListingEntity = PropertyListingEntity(
            id = domain.id,
            hostId = domain.hostId,
            title = domain.title,
            subtitle = domain.subtitle,
            rating = domain.rating,
            reviewCount = domain.reviewCount,
            imageUrl = domain.imageUrl
        )
    }
}
