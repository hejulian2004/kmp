/**
 * @File: WeChatMpRepositoryImpl.kt
 * @Package: org.example.project.data.repository.wechat
 * @Description: 微信公众号数据仓库实现类（结合Room KMP与响应式Mock数据流）
 * @Author: 何聚敛
 * @Date: 2026-08-18
 */
package org.example.project.data.repository.wechat

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.core.network.client.NetworkContainer
import org.example.project.data.database.dao.wechat.WeChatMpDao
import org.example.project.data.database.entity.wechat.WeChatArticleEntity
import org.example.project.domain.model.wechat.WeChatAccount
import org.example.project.domain.model.wechat.WeChatArticle
import org.example.project.domain.model.wechat.WeChatCardType
import org.example.project.domain.repository.wechat.WeChatMpRepository
import org.example.project.platform.currentTimeMillis
import kotlin.random.Random

class WeChatMpRepositoryImpl(
    private val weChatMpDao: WeChatMpDao,
    private val networkContainer: NetworkContainer? = null
) : WeChatMpRepository {

    private val scope = CoroutineScope(Dispatchers.Default)
    private val accountsFlow = MutableStateFlow<List<WeChatAccount>>(emptyList())
    private val featuredFlow = MutableStateFlow<WeChatArticle?>(null)
    private val waterfallFlow = MutableStateFlow<List<WeChatArticle>>(emptyList())

    init {
        scope.launch {
            val existingArticles = weChatMpDao.observeWaterfallArticles().first()
            val existingFeatured = weChatMpDao.observeFeaturedArticle().first()

            if (existingArticles.isEmpty() && existingFeatured == null) {
                val initialAccounts = createMockWeChatAccounts()
                val initialFeatured = createMockFeaturedArticle()
                val initialWaterfall = createMockWaterfallArticles()

                accountsFlow.value = initialAccounts
                featuredFlow.value = initialFeatured
                waterfallFlow.value = initialWaterfall

                weChatMpDao.insertArticles(
                    listOf(WeChatArticleEntity.fromDomainModel(initialFeatured)) +
                    initialWaterfall.map { WeChatArticleEntity.fromDomainModel(it) }
                )
            } else {
                accountsFlow.value = createMockWeChatAccounts()
                featuredFlow.value = existingFeatured?.toDomainModel() ?: createMockFeaturedArticle()
                waterfallFlow.value = existingArticles.map { it.toDomainModel() }
            }
        }
    }

    override fun observeFrequentlyReadAccounts(): Flow<List<WeChatAccount>> = accountsFlow

    override fun observeFeaturedArticle(): Flow<WeChatArticle?> {
        return weChatMpDao.observeFeaturedArticle().map { entity ->
            entity?.toDomainModel() ?: featuredFlow.value
        }
    }

    override fun observeWaterfallArticles(): Flow<List<WeChatArticle>> {
        return weChatMpDao.observeWaterfallArticles().map { entities ->
            if (entities.isEmpty()) {
                waterfallFlow.value
            } else {
                entities.map { it.toDomainModel() }
            }
        }
    }

    override suspend fun refreshData(): Result<Unit> {
        return runCatching {
            delay(500)
            val refreshedAccounts = accountsFlow.value.map {
                it.copy(hasUnread = Random.nextBoolean())
            }
            accountsFlow.value = refreshedAccounts

            val newFeatured = createMockFeaturedArticle().copy(
                publishTimestamp = currentTimeMillis(),
                readCount = (30000..90000).random()
            )
            featuredFlow.value = newFeatured

            val currentWaterfall = createMockWaterfallArticles().shuffled()
            waterfallFlow.value = currentWaterfall

            weChatMpDao.clearAll()
            weChatMpDao.insertArticles(
                listOf(WeChatArticleEntity.fromDomainModel(newFeatured)) +
                currentWaterfall.map { WeChatArticleEntity.fromDomainModel(it) }
            )
        }
    }

    override suspend fun loadMoreArticles(): Result<List<WeChatArticle>> {
        return runCatching {
            delay(700)
            val now = currentTimeMillis()
            val moreArticles = listOf(
                WeChatArticle(
                    id = "art_more_${Random.nextInt(1000, 9999)}",
                    account = WeChatAccount(
                        id = "acc_geek_${Random.nextInt(10, 99)}",
                        name = "极客视界",
                        avatarUrl = "https://picsum.photos/seed/geekview/120/120",
                        isFollowed = true
                    ),
                    title = "次世代AI智能体时代已来：深度解读跨平台技术革命",
                    coverUrl = "https://picsum.photos/seed/more_ai/400/500",
                    publishTimeText = "昨天",
                    publishTimestamp = now - 86400_000,
                    cardType = WeChatCardType.WATERFALL_GRID,
                    isFollowedAccount = true,
                    readCount = 28300,
                    coverAspectRatio = 0.88f
                ),
                WeChatArticle(
                    id = "art_more_${Random.nextInt(1000, 9999)}",
                    account = WeChatAccount(
                        id = "acc_36kr_${Random.nextInt(10, 99)}",
                        name = "36氪",
                        avatarUrl = "https://picsum.photos/seed/36kr/120/120",
                        isFollowed = false
                    ),
                    title = "大模型落地应用最新观察：企业级方案如何降本增效？",
                    coverUrl = "https://picsum.photos/seed/more_biz/300/300",
                    publishTimeText = "昨天",
                    publishTimestamp = now - 90000_000,
                    cardType = WeChatCardType.HORIZONTAL_ROW,
                    isFollowedAccount = false,
                    readCount = 49100,
                    coverAspectRatio = 1.0f
                )
            )
            waterfallFlow.update { it + moreArticles }
            weChatMpDao.insertArticles(moreArticles.map { WeChatArticleEntity.fromDomainModel(it) })
            moreArticles
        }
    }

    override suspend fun dislikeArticle(articleId: String, reason: String): Result<Unit> {
        return runCatching {
            waterfallFlow.update { list -> list.filterNot { it.id == articleId } }
            weChatMpDao.deleteArticle(articleId)
        }
    }

    override suspend fun markAccountAsRead(accountId: String): Result<Unit> {
        return runCatching {
            accountsFlow.update { list ->
                list.map { if (it.id == accountId) it.copy(hasUnread = false) else it }
            }
        }
    }

    override suspend fun toggleFollowAccount(accountId: String): Result<Boolean> {
        return runCatching {
            var newStatus = false
            accountsFlow.update { list ->
                list.map {
                    if (it.id == accountId) {
                        newStatus = !it.isFollowed
                        it.copy(isFollowed = newStatus)
                    } else it
                }
            }
            newStatus
        }
    }
}
