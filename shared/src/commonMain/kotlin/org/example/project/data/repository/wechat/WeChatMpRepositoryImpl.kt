/**
 * @File: WeChatMpRepositoryImpl.kt
 * @Package: org.example.project.data.repository.wechat
 * @Description: 微信公众号数据仓库实现类（结合AppMockConfig假数据开关、Room KMP与FileStorage文件缓存）
 * @Author: 何聚敛
 * @Date: 2026-08-18
 */
package org.example.project.data.repository.wechat

import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.example.project.core.config.AppMockConfig
import org.example.project.core.config.MockModule
import org.example.project.core.network.client.NetworkContainer
import org.example.project.core.network.config.ApiEndpoints
import org.example.project.core.storage.api.FileStorage
import org.example.project.core.storage.api.StorageArea
import org.example.project.core.storage.api.StoragePath
import org.example.project.data.database.dao.wechat.WeChatMpDao
import org.example.project.data.database.entity.wechat.WeChatArticleEntity
import org.example.project.domain.model.wechat.WeChatAccount
import org.example.project.domain.model.wechat.WeChatArticle
import org.example.project.domain.model.wechat.WeChatCardType
import org.example.project.domain.repository.wechat.WeChatMpRepository
import org.example.project.platform.currentTimeMillis
import kotlin.random.Random

@Serializable
private data class WeChatMpCacheSnapshot(
    val accounts: List<WeChatAccount>,
    val featuredArticle: WeChatArticle?,
    val waterfallArticles: List<WeChatArticle>
)

class WeChatMpRepositoryImpl(
    private val weChatMpDao: WeChatMpDao,
    private val networkContainer: NetworkContainer? = null,
    private val fileStorage: FileStorage? = null
) : WeChatMpRepository {

    private val isMockActive: Boolean
        get() = AppMockConfig.isMockActiveFor(MockModule.WECHAT_MP)

    private val json = Json { ignoreUnknownKeys = true; isLenient = true; prettyPrint = false }
    private val cachePath = StoragePath("wechat_mp/articles_cache.json")
    private val scope = CoroutineScope(Dispatchers.Default)

    private val accountsFlow = MutableStateFlow<List<WeChatAccount>>(
        if (isMockActive) createMockWeChatAccounts() else emptyList()
    )
    private val featuredFlow = MutableStateFlow<WeChatArticle?>(
        if (isMockActive) createMockFeaturedArticle() else null
    )
    private val waterfallFlow = MutableStateFlow<List<WeChatArticle>>(
        if (isMockActive) createMockWaterfallArticles() else emptyList()
    )

    init {
        scope.launch {
            val existingArticles = weChatMpDao.observeWaterfallArticles().first()
            val existingFeatured = weChatMpDao.observeFeaturedArticle().first()

            if (existingArticles.isEmpty() && existingFeatured == null) {
                // 优先尝试从 FileStorage 文件磁盘缓存中读取快照
                val cachedSnapshot = loadFromDiskCache()
                if (cachedSnapshot != null) {
                    accountsFlow.value = cachedSnapshot.accounts
                    featuredFlow.value = cachedSnapshot.featuredArticle
                    waterfallFlow.value = cachedSnapshot.waterfallArticles

                    val entities = buildList {
                        cachedSnapshot.featuredArticle?.let { add(WeChatArticleEntity.fromDomainModel(it)) }
                        addAll(cachedSnapshot.waterfallArticles.map { WeChatArticleEntity.fromDomainModel(it) })
                    }
                    weChatMpDao.insertArticles(entities)
                } else if (isMockActive) {
                    // 无本地文件缓存且处于 Mock 模式时采用预置 Mock 种子数据并落地写入文件缓存
                    val currentFeatured = featuredFlow.value ?: createMockFeaturedArticle()
                    val currentWaterfall = waterfallFlow.value.ifEmpty { createMockWaterfallArticles() }
                    featuredFlow.value = currentFeatured
                    waterfallFlow.value = currentWaterfall

                    weChatMpDao.insertArticles(
                        listOf(WeChatArticleEntity.fromDomainModel(currentFeatured)) +
                        currentWaterfall.map { WeChatArticleEntity.fromDomainModel(it) }
                    )
                    saveToDiskCache(accountsFlow.value, currentFeatured, currentWaterfall)
                }
            } else {
                if (existingFeatured != null) {
                    featuredFlow.value = existingFeatured.toDomainModel()
                }
                if (existingArticles.isNotEmpty()) {
                    waterfallFlow.value = existingArticles.map { it.toDomainModel() }
                }
            }
        }
    }

    private suspend fun loadFromDiskCache(): WeChatMpCacheSnapshot? {
        val storage = fileStorage ?: return null
        return runCatching {
            if (!storage.exists(StorageArea.CACHE, cachePath)) return@runCatching null
            val bytes = storage.read(StorageArea.CACHE, cachePath)
            val content = bytes.decodeToString()
            json.decodeFromString<WeChatMpCacheSnapshot>(content)
        }.getOrNull()
    }

    private suspend fun saveToDiskCache(
        accounts: List<WeChatAccount>,
        featured: WeChatArticle?,
        waterfall: List<WeChatArticle>
    ) {
        val storage = fileStorage ?: return
        runCatching {
            val snapshot = WeChatMpCacheSnapshot(accounts, featured, waterfall)
            val bytes = json.encodeToString(snapshot).encodeToByteArray()
            storage.write(StorageArea.CACHE, cachePath, bytes)
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
            if (isMockActive) {
                delay(AppMockConfig.mockNetworkDelayMs)
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

                saveToDiskCache(refreshedAccounts, newFeatured, currentWaterfall)
            } else {
                val client = networkContainer?.authorizedClient ?: error("NetworkContainer 尚未配置，无法发起真实网络请求")
                val accounts: List<WeChatAccount> = client.get("${ApiEndpoints.BASE_URL}${ApiEndpoints.WeChatMp.GET_ACCOUNTS}").body()
                val featured: WeChatArticle? = client.get("${ApiEndpoints.BASE_URL}${ApiEndpoints.WeChatMp.GET_FEATURED}").body()
                val waterfall: List<WeChatArticle> = client.get("${ApiEndpoints.BASE_URL}${ApiEndpoints.WeChatMp.GET_WATERFALL}").body()

                accountsFlow.value = accounts
                featuredFlow.value = featured
                waterfallFlow.value = waterfall

                weChatMpDao.clearAll()
                val entities = buildList {
                    featured?.let { add(WeChatArticleEntity.fromDomainModel(it)) }
                    addAll(waterfall.map { WeChatArticleEntity.fromDomainModel(it) })
                }
                weChatMpDao.insertArticles(entities)
                saveToDiskCache(accounts, featured, waterfall)
            }
        }
    }

    override suspend fun loadMoreArticles(): Result<List<WeChatArticle>> {
        return runCatching {
            if (isMockActive) {
                delay(AppMockConfig.mockNetworkDelayMs)
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

                saveToDiskCache(accountsFlow.value, featuredFlow.value, waterfallFlow.value)
                moreArticles
            } else {
                val client = networkContainer?.authorizedClient ?: error("NetworkContainer 尚未配置")
                val page = (waterfallFlow.value.size / 10) + 1
                val moreArticles: List<WeChatArticle> = client.get("${ApiEndpoints.BASE_URL}${ApiEndpoints.WeChatMp.GET_WATERFALL}?page=$page").body()

                waterfallFlow.update { it + moreArticles }
                weChatMpDao.insertArticles(moreArticles.map { WeChatArticleEntity.fromDomainModel(it) })
                saveToDiskCache(accountsFlow.value, featuredFlow.value, waterfallFlow.value)
                moreArticles
            }
        }
    }

    override suspend fun dislikeArticle(articleId: String, reason: String): Result<Unit> {
        return runCatching {
            waterfallFlow.update { list -> list.filterNot { it.id == articleId } }
            weChatMpDao.deleteArticle(articleId)
            saveToDiskCache(accountsFlow.value, featuredFlow.value, waterfallFlow.value)
        }
    }

    override suspend fun markAccountAsRead(accountId: String): Result<Unit> {
        return runCatching {
            accountsFlow.update { list ->
                list.map { if (it.id == accountId) it.copy(hasUnread = false) else it }
            }
            saveToDiskCache(accountsFlow.value, featuredFlow.value, waterfallFlow.value)
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
            saveToDiskCache(accountsFlow.value, featuredFlow.value, waterfallFlow.value)
            newStatus
        }
    }
}
