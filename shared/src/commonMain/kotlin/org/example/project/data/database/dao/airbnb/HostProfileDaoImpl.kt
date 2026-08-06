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
import org.example.project.platform.readStorageFile
import org.example.project.platform.writeStorageFile

class HostProfileDaoImpl : HostProfileDao {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }
    private val hostsFile = "airbnb_hosts_db.json"
    private val propertiesFile = "airbnb_properties_db.json"
    private val reviewsFile = "airbnb_reviews_db.json"
    private val guidesFile = "airbnb_guides_db.json"

    private val hostsTable = MutableStateFlow<List<HostEntity>>(loadInitialHosts())
    private val propertiesTable = MutableStateFlow<List<PropertyListingEntity>>(loadInitialProperties())
    private val reviewsTable = MutableStateFlow<List<HostReviewEntity>>(loadInitialReviews())
    private val guidesTable = MutableStateFlow<List<TravelGuideEntity>>(loadInitialGuides())

    private fun loadInitialHosts(): List<HostEntity> {
        val content = readStorageFile(hostsFile)
        return if (!content.isNullOrBlank()) {
            runCatching { json.decodeFromString<List<HostEntity>>(content) }.getOrDefault(emptyList())
        } else emptyList()
    }

    private fun loadInitialProperties(): List<PropertyListingEntity> {
        val content = readStorageFile(propertiesFile)
        return if (!content.isNullOrBlank()) {
            runCatching { json.decodeFromString<List<PropertyListingEntity>>(content) }.getOrDefault(emptyList())
        } else emptyList()
    }

    private fun loadInitialReviews(): List<HostReviewEntity> {
        val content = readStorageFile(reviewsFile)
        return if (!content.isNullOrBlank()) {
            runCatching { json.decodeFromString<List<HostReviewEntity>>(content) }.getOrDefault(emptyList())
        } else emptyList()
    }

    private fun loadInitialGuides(): List<TravelGuideEntity> {
        val content = readStorageFile(guidesFile)
        return if (!content.isNullOrBlank()) {
            runCatching { json.decodeFromString<List<TravelGuideEntity>>(content) }.getOrDefault(emptyList())
        } else emptyList()
    }

    private fun persistHosts(hosts: List<HostEntity>) {
        runCatching { writeStorageFile(hostsFile, json.encodeToString(hosts)) }
    }

    private fun persistProperties(properties: List<PropertyListingEntity>) {
        runCatching { writeStorageFile(propertiesFile, json.encodeToString(properties)) }
    }

    private fun persistReviews(reviews: List<HostReviewEntity>) {
        runCatching { writeStorageFile(reviewsFile, json.encodeToString(reviews)) }
    }

    private fun persistGuides(guides: List<TravelGuideEntity>) {
        runCatching { writeStorageFile(guidesFile, json.encodeToString(guides)) }
    }

    override fun observeHosts(): Flow<List<HostEntity>> = hostsTable.asStateFlow()

    override suspend fun insertHosts(hosts: List<HostEntity>) {
        hostsTable.update { current ->
            val map = current.associateBy { it.id }.toMutableMap()
            hosts.forEach { map[it.id] = it }
            val updated = map.values.toList()
            persistHosts(updated)
            updated
        }
    }

    override fun observeProperties(): Flow<List<PropertyListingEntity>> = propertiesTable.asStateFlow()

    override suspend fun insertProperties(properties: List<PropertyListingEntity>) {
        propertiesTable.update { current ->
            val map = current.associateBy { it.id }.toMutableMap()
            properties.forEach { map[it.id] = it }
            val updated = map.values.toList()
            persistProperties(updated)
            updated
        }
    }

    override fun observeReviews(): Flow<List<HostReviewEntity>> = reviewsTable.asStateFlow()

    override suspend fun insertReviews(reviews: List<HostReviewEntity>) {
        reviewsTable.update { current ->
            val map = current.associateBy { it.id }.toMutableMap()
            reviews.forEach { map[it.id] = it }
            val updated = map.values.toList()
            persistReviews(updated)
            updated
        }
    }

    override fun observeGuides(): Flow<List<TravelGuideEntity>> = guidesTable.asStateFlow()

    override suspend fun insertGuides(guides: List<TravelGuideEntity>) {
        guidesTable.update { current ->
            val map = current.associateBy { it.id }.toMutableMap()
            guides.forEach { map[it.id] = it }
            val updated = map.values.toList()
            persistGuides(updated)
            updated
        }
    }

    override suspend fun clearHosts() {
        hostsTable.value = emptyList()
        persistHosts(emptyList())
    }

    override suspend fun clearProperties() {
        propertiesTable.value = emptyList()
        persistProperties(emptyList())
    }

    override suspend fun clearReviews() {
        reviewsTable.value = emptyList()
        persistReviews(emptyList())
    }

    override suspend fun clearGuides() {
        guidesTable.value = emptyList()
        persistGuides(emptyList())
    }
}
