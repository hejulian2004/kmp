/**
 * @File: FakeHostProfileDao.kt
 * @Package: org.example.project.core.database
 * @Description: 仅供单元测试使用的 FakeHostProfileDao 内存响应式模拟实现
 * @Author: 何聚敛
 * @Date: 2026-08-18
 */
package org.example.project.core.database

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.example.project.data.database.dao.airbnb.HostProfileDao
import org.example.project.data.database.entity.airbnb.HostEntity
import org.example.project.data.database.entity.airbnb.HostReviewEntity
import org.example.project.data.database.entity.airbnb.PropertyListingEntity
import org.example.project.data.database.entity.airbnb.TravelGuideEntity

class FakeHostProfileDao : HostProfileDao {
    private val hostsFlow = MutableStateFlow<List<HostEntity>>(emptyList())
    private val propertiesFlow = MutableStateFlow<List<PropertyListingEntity>>(emptyList())
    private val reviewsFlow = MutableStateFlow<List<HostReviewEntity>>(emptyList())
    private val guidesFlow = MutableStateFlow<List<TravelGuideEntity>>(emptyList())

    override fun observeHosts(): Flow<List<HostEntity>> = hostsFlow.asStateFlow()

    override suspend fun insertHosts(hosts: List<HostEntity>) {
        hostsFlow.update { current ->
            val mutable = current.toMutableList()
            hosts.forEach { incoming ->
                mutable.removeAll { it.id == incoming.id }
                mutable.add(incoming)
            }
            mutable
        }
    }

    override fun observeProperties(): Flow<List<PropertyListingEntity>> = propertiesFlow.asStateFlow()

    override suspend fun insertProperties(properties: List<PropertyListingEntity>) {
        propertiesFlow.update { current ->
            val mutable = current.toMutableList()
            properties.forEach { incoming ->
                mutable.removeAll { it.id == incoming.id }
                mutable.add(incoming)
            }
            mutable
        }
    }

    override fun observeReviews(): Flow<List<HostReviewEntity>> = reviewsFlow.asStateFlow()

    override suspend fun insertReviews(reviews: List<HostReviewEntity>) {
        reviewsFlow.update { current ->
            val mutable = current.toMutableList()
            reviews.forEach { incoming ->
                mutable.removeAll { it.id == incoming.id }
                mutable.add(incoming)
            }
            mutable
        }
    }

    override fun observeGuides(): Flow<List<TravelGuideEntity>> = guidesFlow.asStateFlow()

    override suspend fun insertGuides(guides: List<TravelGuideEntity>) {
        guidesFlow.update { current ->
            val mutable = current.toMutableList()
            guides.forEach { incoming ->
                mutable.removeAll { it.id == incoming.id }
                mutable.add(incoming)
            }
            mutable
        }
    }

    override suspend fun clearHosts() {
        hostsFlow.value = emptyList()
    }

    override suspend fun clearProperties() {
        propertiesFlow.value = emptyList()
    }

    override suspend fun clearReviews() {
        reviewsFlow.value = emptyList()
    }

    override suspend fun clearGuides() {
        guidesFlow.value = emptyList()
    }

    suspend fun clearAll() {
        clearHosts()
        clearProperties()
        clearReviews()
        clearGuides()
    }
}
