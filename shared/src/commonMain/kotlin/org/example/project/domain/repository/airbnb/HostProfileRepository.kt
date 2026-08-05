/**
 * @File: HostProfileRepository.kt
 * @Package: org.example.project.domain.repository.airbnb
 * @Description: Airbnb房东与房源数据仓库抽象接口（支持 SWR 响应式状态流与直接数据拉取）
 * @Author: 何聚敛
 * @Date: 2026-08-05
 */
package org.example.project.domain.repository.airbnb

import kotlinx.coroutines.flow.Flow
import org.example.project.core.data.ResourceState
import org.example.project.domain.model.airbnb.Host
import org.example.project.domain.model.airbnb.HostReview
import org.example.project.domain.model.airbnb.PropertyListing
import org.example.project.domain.model.airbnb.TravelGuide

interface HostProfileRepository {
    fun getHostsResource(): Flow<ResourceState<List<Host>>>
    fun getPropertiesResource(): Flow<ResourceState<List<PropertyListing>>>
    fun getReviewsResource(): Flow<ResourceState<List<HostReview>>>
    fun getGuidesResource(): Flow<ResourceState<List<TravelGuide>>>

    suspend fun getHosts(): List<Host>
    suspend fun getProperties(): List<PropertyListing>
    suspend fun getReviews(): List<HostReview>
    suspend fun getGuides(): List<TravelGuide>
}
