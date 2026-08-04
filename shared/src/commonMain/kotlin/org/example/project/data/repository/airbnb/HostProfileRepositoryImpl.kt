/**
 * @File: HostProfileRepositoryImpl.kt
 * @Package: org.example.project.data.repository.airbnb
 * @Description: Airbnb房东与房源数据仓库实现类（遵循网络架构规范，结合 NetworkContainer、Room DAO 与 SWR 离线同步管道）
 * @Author: 何聚敛
 * @Date: 2026-08-04
 */
package org.example.project.data.repository.airbnb

import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import org.example.project.core.data.ResourceState
import org.example.project.core.data.networkBoundResource
import org.example.project.core.network.client.NetworkContainer
import org.example.project.core.network.config.ApiEndpoints
import org.example.project.data.database.dao.airbnb.HostProfileDao
import org.example.project.data.database.entity.airbnb.HostEntity
import org.example.project.data.database.entity.airbnb.HostReviewEntity
import org.example.project.data.database.entity.airbnb.PropertyListingEntity
import org.example.project.data.database.entity.airbnb.TravelGuideEntity
import org.example.project.domain.model.airbnb.Host
import org.example.project.domain.model.airbnb.HostReview
import org.example.project.domain.model.airbnb.PropertyListing
import org.example.project.domain.model.airbnb.TravelGuide
import org.example.project.domain.repository.airbnb.HostProfileRepository

class HostProfileRepositoryImpl(
    private val dao: HostProfileDao,
    private val networkContainer: NetworkContainer? = null,
) : HostProfileRepository {

    // 假数据预置（开发测试阶段当后端 API 未就绪或无网络时平滑降级兜底）
    private val initialHosts = listOf(
        Host(
            id = "art-room-hk",
            name = "ArtRoomHK",
            reviewCount = 2066,
            rating = 4.85,
            yearsHosting = 7,
            totalListings = 11,
            languages = "中文和英语",
            identityVerified = true,
            superHost = true,
            about = "ArtRoom 是一个极具艺术气息的空间。",
            occupation = "艺术家 / 策展人",
            livesIn = "香港",
            hobbies = listOf("艺术展览", "城市散步", "咖啡烘焙", "室内设计"),
            places = listOf("东京", "巴黎", "纽约", "巴厘岛"),
            placesVisible = true,
            avatarUrl = "https://picsum.photos/seed/arthost/320/320",
        ),
        Host(
            id = "jane-host",
            name = "Jane",
            reviewCount = 468,
            rating = 4.93,
            yearsHosting = 5,
            totalListings = 8,
            languages = "中文、English、日本語",
            identityVerified = true,
            superHost = true,
            about = "Jane 喜欢旅行与建筑摄影，希望你在这里感受到安全、干净和友好。",
            occupation = "建筑摄影师",
            livesIn = "上海",
            hobbies = listOf("瑜伽", "摄影", "做甜品", "骑行"),
            places = listOf("东京", "悉尼", "冰岛", "罗马"),
            placesVisible = true,
            avatarUrl = "https://picsum.photos/seed/janehost/320/320",
        )
    )

    private val initialProperties = listOf(
        PropertyListing(
            id = "p1",
            hostId = "art-room-hk",
            title = "酒店式公寓",
            subtitle = "ArtRoom 6 - 睡眠舱女生共享空间，位处市中心交通便捷，富有艺术气息。",
            rating = 4.84,
            reviewCount = 88,
            imageUrl = "https://picsum.photos/seed/artroom6/900/600",
        ),
        PropertyListing(
            id = "p2",
            hostId = "art-room-hk",
            title = "酒店式公寓",
            subtitle = "ArtRoom 7 - 睡眠舱女生共享空间，位处市中心交通便捷，安静舒适。",
            rating = 4.90,
            reviewCount = 123,
            imageUrl = "https://picsum.photos/seed/artroom7/900/600",
        ),
        PropertyListing(
            id = "p3",
            hostId = "jane-host",
            title = "简约设计公寓",
            subtitle = "Jane Loft 1 - 地铁 5 分钟，独立卫浴，适合短住与商旅。",
            rating = 4.95,
            reviewCount = 142,
            imageUrl = "https://picsum.photos/seed/janeloft1/900/600",
        ),
        PropertyListing(
            id = "p4",
            hostId = "jane-host",
            title = "城市景观套房",
            subtitle = "Jane View 2 - 高楼层夜景，开放式厨房，安静采光好。",
            rating = 4.89,
            reviewCount = 97,
            imageUrl = "https://picsum.photos/seed/janeview2/900/600",
        )
    )

    private val initialReviews = listOf(
        HostReview(
            id = "r1",
            hostId = "art-room-hk",
            reviewerName = "Yoshimi",
            reviewerLocation = "达拉斯, 德克萨斯州",
            reviewerAvatarUrl = "https://picsum.photos/seed/reviewer-yoshimi/200/200",
            stars = 5,
            dateText = "2周前",
            content = "很喜欢这里，这里只有女性房客，感觉非常安全干净。入住安排灵活，旅舍也非常整洁。",
        ),
        HostReview(
            id = "r2",
            hostId = "art-room-hk",
            reviewerName = "Min",
            reviewerLocation = "首尔, 韩国",
            reviewerAvatarUrl = "https://picsum.photos/seed/reviewer-min/200/200",
            stars = 5,
            dateText = "1个月前",
            content = "位置方便，公共区域维护得很好，房东沟通及时，整体体验非常安心。",
        ),
        HostReview(
            id = "r3",
            hostId = "jane-host",
            reviewerName = "Alice",
            reviewerLocation = "悉尼, 澳大利亚",
            reviewerAvatarUrl = "https://picsum.photos/seed/reviewer-alice/200/200",
            stars = 5,
            dateText = "3周前",
            content = "Jane 非常热情，入住说明很清晰，房间布置温馨，设备也很齐全。",
        ),
        HostReview(
            id = "r4",
            hostId = "jane-host",
            reviewerName = "Nora",
            reviewerLocation = "大阪, 日本",
            reviewerAvatarUrl = "https://picsum.photos/seed/reviewer-nora/200/200",
            stars = 5,
            dateText = "5天前",
            content = "交通便利，房间采光很好，夜间非常安静。下次来还会选择这套房子。",
        )
    )

    private val initialGuides = listOf(
        TravelGuide(id = "g1", hostId = "art-room-hk", title = "ArtRoom的旅行指南"),
        TravelGuide(id = "g2", hostId = "jane-host", title = "Jane的旅行指南")
    )

    override fun getHostsResource(): Flow<ResourceState<List<Host>>> {
        return networkBoundResource(
            key = "hosts",
            queryLocal = {
                dao.observeHosts().map { list -> list.map { it.toDomainModel() } }
            },
            fetchRemote = {
                val container = networkContainer
                if (container != null) {
                    runCatching {
                        val remote = container.authorizedClient
                            .get(ApiEndpoints.Airbnb.GET_HOSTS)
                            .body<List<Host>>()
                        if (remote.isNotEmpty()) remote else initialHosts
                    }.getOrDefault(initialHosts)
                } else {
                    initialHosts
                }
            },
            saveRemoteResult = { _, hosts ->
                dao.insertHosts(hosts.map { HostEntity.fromDomainModel(it) })
            }
        )
    }

    override fun getPropertiesResource(): Flow<ResourceState<List<PropertyListing>>> {
        return networkBoundResource(
            key = "properties",
            queryLocal = {
                dao.observeProperties().map { list -> list.map { it.toDomainModel() } }
            },
            fetchRemote = {
                val container = networkContainer
                if (container != null) {
                    runCatching {
                        val remote = container.authorizedClient
                            .get(ApiEndpoints.Airbnb.GET_PROPERTIES)
                            .body<List<PropertyListing>>()
                        if (remote.isNotEmpty()) remote else initialProperties
                    }.getOrDefault(initialProperties)
                } else {
                    initialProperties
                }
            },
            saveRemoteResult = { _, properties ->
                dao.insertProperties(properties.map { PropertyListingEntity.fromDomainModel(it) })
            }
        )
    }

    override fun getReviewsResource(): Flow<ResourceState<List<HostReview>>> {
        return networkBoundResource(
            key = "reviews",
            queryLocal = {
                dao.observeReviews().map { list -> list.map { it.toDomainModel() } }
            },
            fetchRemote = {
                val container = networkContainer
                if (container != null) {
                    runCatching {
                        val remote = container.authorizedClient
                            .get(ApiEndpoints.Airbnb.GET_REVIEWS)
                            .body<List<HostReview>>()
                        if (remote.isNotEmpty()) remote else initialReviews
                    }.getOrDefault(initialReviews)
                } else {
                    initialReviews
                }
            },
            saveRemoteResult = { _, reviews ->
                dao.insertReviews(reviews.map { HostReviewEntity.fromDomainModel(it) })
            }
        )
    }

    override fun getGuidesResource(): Flow<ResourceState<List<TravelGuide>>> {
        return networkBoundResource(
            key = "guides",
            queryLocal = {
                dao.observeGuides().map { list -> list.map { it.toDomainModel() } }
            },
            fetchRemote = {
                val container = networkContainer
                if (container != null) {
                    runCatching {
                        val remote = container.authorizedClient
                            .get(ApiEndpoints.Airbnb.GET_GUIDES)
                            .body<List<TravelGuide>>()
                        if (remote.isNotEmpty()) remote else initialGuides
                    }.getOrDefault(initialGuides)
                } else {
                    initialGuides
                }
            },
            saveRemoteResult = { _, guides ->
                dao.insertGuides(guides.map { TravelGuideEntity.fromDomainModel(it) })
            }
        )
    }

    override suspend fun getHosts(): List<Host> {
        val cached = dao.observeHosts().firstOrNull()
        return if (!cached.isNullOrEmpty()) {
            cached.map { it.toDomainModel() }
        } else {
            dao.insertHosts(initialHosts.map { HostEntity.fromDomainModel(it) })
            initialHosts
        }
    }

    override suspend fun getProperties(): List<PropertyListing> {
        val cached = dao.observeProperties().firstOrNull()
        return if (!cached.isNullOrEmpty()) {
            cached.map { it.toDomainModel() }
        } else {
            dao.insertProperties(initialProperties.map { PropertyListingEntity.fromDomainModel(it) })
            initialProperties
        }
    }

    override suspend fun getReviews(): List<HostReview> {
        val cached = dao.observeReviews().firstOrNull()
        return if (!cached.isNullOrEmpty()) {
            cached.map { it.toDomainModel() }
        } else {
            dao.insertReviews(initialReviews.map { HostReviewEntity.fromDomainModel(it) })
            initialReviews
        }
    }

    override suspend fun getGuides(): List<TravelGuide> {
        val cached = dao.observeGuides().firstOrNull()
        return if (!cached.isNullOrEmpty()) {
            cached.map { it.toDomainModel() }
        } else {
            dao.insertGuides(initialGuides.map { TravelGuideEntity.fromDomainModel(it) })
            initialGuides
        }
    }
}
