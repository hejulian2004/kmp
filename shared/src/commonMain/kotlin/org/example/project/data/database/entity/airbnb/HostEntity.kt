/**
 * @File: HostEntity.kt
 * @Package: org.example.project.data.database.entity.airbnb
 * @Description: Airbnb房东信息的Room本地数据库实体
 * @Author: 何聚敛
 * @Date: 2026-08-05
 */
package org.example.project.data.database.entity.airbnb

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import org.example.project.domain.model.airbnb.Host

@Serializable
@Entity(tableName = "hosts")
data class HostEntity(
    @PrimaryKey val id: String,
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
) {
    fun toDomainModel(): Host = Host(
        id = id,
        name = name,
        reviewCount = reviewCount,
        rating = rating,
        yearsHosting = yearsHosting,
        totalListings = totalListings,
        languages = languages,
        identityVerified = identityVerified,
        superHost = superHost,
        about = about,
        occupation = occupation,
        livesIn = livesIn,
        hobbies = hobbies,
        places = places,
        placesVisible = placesVisible,
        avatarUrl = avatarUrl
    )

    companion object {
        fun fromDomainModel(domain: Host): HostEntity = HostEntity(
            id = domain.id,
            name = domain.name,
            reviewCount = domain.reviewCount,
            rating = domain.rating,
            yearsHosting = domain.yearsHosting,
            totalListings = domain.totalListings,
            languages = domain.languages,
            identityVerified = domain.identityVerified,
            superHost = domain.superHost,
            about = domain.about,
            occupation = domain.occupation,
            livesIn = domain.livesIn,
            hobbies = domain.hobbies,
            places = domain.places,
            placesVisible = domain.placesVisible,
            avatarUrl = domain.avatarUrl
        )
    }
}
