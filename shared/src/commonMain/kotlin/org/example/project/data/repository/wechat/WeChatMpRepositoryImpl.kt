/**
 * @File: WeChatMpRepositoryImpl.kt
 * @Package: org.example.project.data.repository.wechat
 * @Description: 微信公众号数据仓库实现类（结合AppMockConfig假数据开关、Room KMP、FileStorage文件缓存与负反馈持久化）
 * @Author: 何聚敛
 * @Date: 2026-08-18
 */
package org.example.project.data.repository.wechat

import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
import org.example.project.core.storage.api.WriteMode
import org.example.project.data.database.dao.wechat.WeChatMpDao
import org.example.project.data.database.entity.wechat.WeChatArticleEntity
import org.example.project.domain.model.wechat.WeChatAccount
import org.example.project.domain.model.wechat.WeChatArticle
import org.example.project.domain.model.wechat.WeChatCardType
import org.example.project.domain.repository.wechat.WeChatMpRepository
import org.example.project.platform.currentTimeMillis
import kotlin.random.Random

@Serializable
data class WeChatDislikeRecord(
    val articleId: String,
    val reason: String,
    val createdAt: Long
)

@Serializable
private data class WeChatMpCacheSnapshot(
    val accounts: List<WeChatAccount>,
    val featuredArticle: WeChatArticle?,
    val waterfallArticles: List<WeChatArticle>
)

@Serializable
private data class WeChatMpPagination(
    val nextPage: Int
)

class WeChatMpRepositoryImpl(
    private val weChatMpDao: WeChatMpDao,
    private val networkContainer: NetworkContainer? = null,
    private val fileStorage: FileStorage? = null,
    coroutineDispatcher: CoroutineDispatcher = Dispatchers.Default
) : WeChatMpRepository {

    private val isMockActive: Boolean
        get() = AppMockConfig.isMockActiveFor(MockModule.WECHAT_MP)

    private val json = Json { ignoreUnknownKeys = true; isLenient = true; prettyPrint = false }
    private val cachePath = StoragePath("wechat_mp/articles_cache.json")
    private val dislikesStoragePath = StoragePath("wechat_mp/dislikes.json")
    private val paginationStoragePath = StoragePath("wechat_mp/pagination.json")
    private val scope = CoroutineScope(coroutineDispatcher)
    private val initialization = CompletableDeferred<Unit>()

    private val dislikesMutex = Mutex()
    private val paginationMutex = Mutex()
    private val dislikedArticleIds = mutableSetOf<String>()

    private var nextPage: Int = 1

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
            try {
                // 1. 优先加载已持久化的“不感兴趣”记录与分页游标
                loadDislikesFromStorage()
                loadPaginationFromStorage()

                val existingArticles = weChatMpDao.observeWaterfallArticles().first()
                    .map { it.toDomainModel() }
                    .filterNot { isDisliked(it.id) }
                val existingFeatured = weChatMpDao.observeFeaturedArticle().first()
                    ?.toDomainModel()
                    ?.takeUnless { isDisliked(it.id) }

                if (existingArticles.isEmpty() && existingFeatured == null) {
                    // 优先尝试从FileStorage文件磁盘缓存中读取快照
                    val cachedSnapshot = loadFromDiskCache()
                    if (cachedSnapshot != null) {
                        val filteredFeatured = cachedSnapshot.featuredArticle?.takeUnless { isDisliked(it.id) }
                        val filteredWaterfall = cachedSnapshot.waterfallArticles.filterNot { isDisliked(it.id) }

                        accountsFlow.value = cachedSnapshot.accounts
                        featuredFlow.value = filteredFeatured
                        waterfallFlow.value = filteredWaterfall

                        val entities = buildList {
                            filteredFeatured?.let { add(WeChatArticleEntity.fromDomainModel(it)) }
                            addAll(filteredWaterfall.map { WeChatArticleEntity.fromDomainModel(it) })
                        }
                        weChatMpDao.insertArticles(entities)
                    } else if (isMockActive) {
                        // 无本地文件缓存且处于Mock模式时采用预置Mock种子数据并落地写入文件缓存
                        val currentFeatured = (featuredFlow.value ?: createMockFeaturedArticle()).takeUnless { isDisliked(it.id) }
                        val currentWaterfall = (waterfallFlow.value.ifEmpty { createMockWaterfallArticles() }).filterNot { isDisliked(it.id) }
                        featuredFlow.value = currentFeatured
                        waterfallFlow.value = currentWaterfall

                        val entities = buildList {
                            currentFeatured?.let { add(WeChatArticleEntity.fromDomainModel(it)) }
                            addAll(currentWaterfall.map { WeChatArticleEntity.fromDomainModel(it) })
                        }
                        weChatMpDao.insertArticles(entities)
                        saveToDiskCache(accountsFlow.value, currentFeatured, currentWaterfall)
                    }
                } else {
                    if (existingFeatured != null) {
                        featuredFlow.value = existingFeatured
                    }
                    if (existingArticles.isNotEmpty()) {
                        waterfallFlow.value = existingArticles
                    }
                }
            } finally {
                initialization.complete(Unit)
            }
        }
    }

    private suspend fun awaitInitialization() {
        initialization.await()
    }

    private suspend fun loadDislikesFromStorage() {
        val storage = fileStorage ?: return
        dislikesMutex.withLock {
            runCatching {
                if (storage.exists(StorageArea.PERSISTENT, dislikesStoragePath)) {
                    val bytes = storage.read(StorageArea.PERSISTENT, dislikesStoragePath)
                    val records = json.decodeFromString<List<WeChatDislikeRecord>>(bytes.decodeToString())
                    dislikedArticleIds.clear()
                    dislikedArticleIds.addAll(records.map { it.articleId })
                }
            }
        }
    }

    private suspend fun appendDislikeRecordToStorage(record: WeChatDislikeRecord) {
        val storage = fileStorage ?: return
        dislikesMutex.withLock {
            runCatching {
                val currentRecords = if (storage.exists(StorageArea.PERSISTENT, dislikesStoragePath)) {
                    val bytes = storage.read(StorageArea.PERSISTENT, dislikesStoragePath)
                    json.decodeFromString<List<WeChatDislikeRecord>>(bytes.decodeToString()).toMutableList()
                } else {
                    mutableListOf()
                }
                currentRecords.removeAll { it.articleId == record.articleId }
                currentRecords.add(record)
                dislikedArticleIds.add(record.articleId)

                val bytes = json.encodeToString(currentRecords).encodeToByteArray()
                storage.write(StorageArea.PERSISTENT, dislikesStoragePath, bytes, WriteMode.ATOMIC)
            }
        }
    }

    private fun isDisliked(articleId: String): Boolean {
        return dislikedArticleIds.contains(articleId)
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

    private suspend fun loadPaginationFromStorage() {
        val storage = fileStorage ?: return
        paginationMutex.withLock {
            runCatching {
                if (storage.exists(StorageArea.PERSISTENT, paginationStoragePath)) {
                    val bytes = storage.read(StorageArea.PERSISTENT, paginationStoragePath)
                    val pagination = json.decodeFromString<WeChatMpPagination>(bytes.decodeToString())
                    nextPage = pagination.nextPage.coerceAtLeast(1)
                }
            }
        }
    }

    private suspend fun savePaginationToStorage(page: Int) {
        val storage = fileStorage ?: return
        runCatching {
            val pagination = WeChatMpPagination(nextPage = page.coerceAtLeast(1))
            val bytes = json.encodeToString(pagination).encodeToByteArray()
            storage.write(StorageArea.PERSISTENT, paginationStoragePath, bytes, WriteMode.ATOMIC)
        }
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
            storage.write(StorageArea.CACHE, cachePath, bytes, WriteMode.ATOMIC)
        }
    }

    override fun observeFrequentlyReadAccounts(): Flow<List<WeChatAccount>> = accountsFlow

    override fun observeFeaturedArticle(): Flow<WeChatArticle?> {
        return weChatMpDao.observeFeaturedArticle().map { entity ->
            val model = entity?.toDomainModel() ?: featuredFlow.value
            model?.takeUnless { isDisliked(it.id) }
        }
    }

    override fun observeWaterfallArticles(): Flow<List<WeChatArticle>> {
        return weChatMpDao.observeWaterfallArticles().map { entities ->
            val list = if (entities.isEmpty()) {
                waterfallFlow.value
            } else {
                entities.map { it.toDomainModel() }
            }
            list.filterNot { isDisliked(it.id) }
        }
    }

    override suspend fun refreshData(): Result<Unit> {
        awaitInitialization()
        return paginationMutex.withLock {
            runCatching {
                if (isMockActive) {
                    delay(AppMockConfig.mockNetworkDelayMs)
                    val refreshedAccounts = accountsFlow.value.map {
                        it.copy(hasUnread = Random.nextBoolean())
                    }
                    accountsFlow.value = refreshedAccounts

                    val rawFeatured = createMockFeaturedArticle().copy(
                        publishTimestamp = currentTimeMillis(),
                        readCount = (30000..90000).random()
                    )
                    val newFeatured = rawFeatured.takeUnless { isDisliked(it.id) }
                    featuredFlow.value = newFeatured

                    val currentWaterfall = createMockWaterfallArticles()
                        .shuffled()
                        .filterNot { isDisliked(it.id) }
                    waterfallFlow.value = currentWaterfall

                    weChatMpDao.clearAll()
                    val entities = buildList {
                        newFeatured?.let { add(WeChatArticleEntity.fromDomainModel(it)) }
                        addAll(currentWaterfall.map { WeChatArticleEntity.fromDomainModel(it) })
                    }
                    weChatMpDao.insertArticles(entities)

                    saveToDiskCache(refreshedAccounts, newFeatured, currentWaterfall)
                } else {
                    val client = networkContainer?.authorizedClient
                        ?: error("NetworkContainer尚未配置，无法发起真实网络请求")
                    val accounts: List<WeChatAccount> = client.get("${ApiEndpoints.BASE_URL}${ApiEndpoints.WeChatMp.GET_ACCOUNTS}").body()
                    val rawFeatured: WeChatArticle? = client.get("${ApiEndpoints.BASE_URL}${ApiEndpoints.WeChatMp.GET_FEATURED}").body()
                    val rawWaterfall: List<WeChatArticle> = client.get("${ApiEndpoints.BASE_URL}${ApiEndpoints.WeChatMp.GET_WATERFALL}").body()

                    val featured = rawFeatured?.takeUnless { isDisliked(it.id) }
                    val waterfall = rawWaterfall.filterNot { isDisliked(it.id) }

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
                // 只有刷新成功后才将游标推进到第二页，失败时保留原游标。
                nextPage = 2
                savePaginationToStorage(nextPage)
            }
        }
    }

    override suspend fun loadMoreArticles(): Result<List<WeChatArticle>> {
        awaitInitialization()
        return paginationMutex.withLock {
            runCatching {
                val requestPage = nextPage
                if (isMockActive) {
                    delay(AppMockConfig.mockNetworkDelayMs)
                    val now = currentTimeMillis()
                    val rawMoreArticles = listOf(
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
                    val moreArticles = rawMoreArticles.filterNot { isDisliked(it.id) }
                    waterfallFlow.update { it + moreArticles }
                    weChatMpDao.insertArticles(moreArticles.map { WeChatArticleEntity.fromDomainModel(it) })

                    saveToDiskCache(accountsFlow.value, featuredFlow.value, waterfallFlow.value)
                    nextPage = requestPage + 1
                    savePaginationToStorage(nextPage)
                    moreArticles
                } else {
                    val client = networkContainer?.authorizedClient ?: error("NetworkContainer尚未配置")
                    val rawMoreArticles: List<WeChatArticle> = client.get("${ApiEndpoints.BASE_URL}${ApiEndpoints.WeChatMp.GET_WATERFALL}?page=$requestPage").body()
                    val moreArticles = rawMoreArticles.filterNot { isDisliked(it.id) }

                    waterfallFlow.update { it + moreArticles }
                    weChatMpDao.insertArticles(moreArticles.map { WeChatArticleEntity.fromDomainModel(it) })
                    saveToDiskCache(accountsFlow.value, featuredFlow.value, waterfallFlow.value)
                    nextPage = requestPage + 1
                    savePaginationToStorage(nextPage)
                    moreArticles
                }
            }
        }
    }

    override suspend fun dislikeArticle(articleId: String, reason: String): Result<Unit> {
        return runCatching {
            val record = WeChatDislikeRecord(
                articleId = articleId,
                reason = reason,
                createdAt = currentTimeMillis()
            )
            appendDislikeRecordToStorage(record)

            waterfallFlow.update { list -> list.filterNot { it.id == articleId } }
            if (featuredFlow.value?.id == articleId) {
                featuredFlow.value = null
            }
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
