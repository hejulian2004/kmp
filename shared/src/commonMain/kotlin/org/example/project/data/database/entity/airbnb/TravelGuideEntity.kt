/**
 * @File: TravelGuideEntity.kt
 * @Package: org.example.project.data.database.entity.airbnb
 * @Description: Airbnb旅行指南信息的Room本地数据库实体
 * @Author: 何聚敛
 * @Date: 2026-08-05
 */
package org.example.project.data.database.entity.airbnb

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.example.project.domain.model.airbnb.TravelGuide

@Entity(tableName = "travel_guides")
data class TravelGuideEntity(
    @PrimaryKey val id: String,
    val hostId: String,
    val title: String,
) {
    fun toDomainModel(): TravelGuide = TravelGuide(
        id = id,
        hostId = hostId,
        title = title
    )

    companion object {
        fun fromDomainModel(domain: TravelGuide): TravelGuideEntity = TravelGuideEntity(
            id = domain.id,
            hostId = domain.hostId,
            title = domain.title
        )
    }
}
