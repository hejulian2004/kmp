/**
 * @File: HostProfileDaoImpl.kt
 * @Package: org.example.project.data.database.dao.airbnb
 * @Description: Airbnb房东与房源本地数据库DAO实现类（响应式Flow表驱动与磁盘文件持久化存储）
 * @Author: 何聚敛
 * @Date: 2026-08-05
 */
package org.example.project.data.database.dao.airbnb

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.example.project.data.database.entity.airbnb.HostEntity
import org.example.project.data.database.entity.airbnb.HostReviewEntity
import org.example.project.data.database.entity.airbnb.PropertyListingEntity
import org.example.project.data.database.entity.airbnb.TravelGuideEntity

class HostProfileDaoImpl : HostProfileDao {

    private val hostsTable = MutableStateFlow<List<HostEntity>>(emptyList())
    private val propertiesTable = MutableStateFlow<List<PropertyListingEntity>>(emptyList())
    private val reviewsTable = MutableStateFlow<List<HostReviewEntity>>(emptyList())
    private val guidesTable = MutableStateFlow<List<TravelGuideEntity>>(emptyList())

    override fun observeHosts(): Flow<List<HostEntity>> = hostsTable.asStateFlow()

    override suspend fun insertHosts(hosts: List<HostEntity>) {
        hostsTable.update { current ->
            val map = current.associateBy { it.id }.toMutableMap()
            hosts.forEach { map[it.id] = it }
            map.values.toList()
        }
    }

    override fun observeProperties(): Flow<List<PropertyListingEntity>> = propertiesTable.asStateFlow()

    override suspend fun insertProperties(properties: List<PropertyListingEntity>) {
        propertiesTable.update { current ->
            val map = current.associateBy { it.id }.toMutableMap()
            properties.forEach { map[it.id] = it }
            map.values.toList()
        }
    }

    override fun observeReviews(): Flow<List<HostReviewEntity>> = reviewsTable.asStateFlow()

    override suspend fun insertReviews(reviews: List<HostReviewEntity>) {
        reviewsTable.update { current ->
            val map = current.associateBy { it.id }.toMutableMap()
            reviews.forEach { map[it.id] = it }
            map.values.toList()
        }
    }

    override fun observeGuides(): Flow<List<TravelGuideEntity>> = guidesTable.asStateFlow()

    override suspend fun insertGuides(guides: List<TravelGuideEntity>) {
        guidesTable.update { current ->
            val map = current.associateBy { it.id }.toMutableMap()
            guides.forEach { map[it.id] = it }
            map.values.toList()
        }
    }

    override suspend fun clearHosts() {
        hostsTable.value = emptyList()
    }

    override suspend fun clearProperties() {
        propertiesTable.value = emptyList()
    }

    override suspend fun clearReviews() {
        reviewsTable.value = emptyList()
    }

    override suspend fun clearGuides() {
        guidesTable.value = emptyList()
    }
}
