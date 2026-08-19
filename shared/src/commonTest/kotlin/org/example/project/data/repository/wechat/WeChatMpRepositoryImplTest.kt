/**
 * @File: WeChatMpRepositoryImplTest.kt
 * @Package: org.example.project.data.repository.wechat
 * @Description: WeChatMpRepositoryImpl微信公众号数据仓库与持久化同步单元测试（包含不感兴趣负反馈持久化校验）
 * @Author: 何聚敛
 * @Date: 2026-08-18
 */
package org.example.project.data.repository.wechat

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.example.project.core.config.AppMockConfig
import org.example.project.core.database.FakeWeChatMpDao
import org.example.project.core.network.auth.InMemorySecureStorage
import org.example.project.core.network.auth.TokenRefresher
import org.example.project.core.network.auth.TokenStore
import org.example.project.core.network.client.DefaultNetworkContainer
import org.example.project.core.network.client.NetworkClientFactoryImpl
import org.example.project.core.storage.api.StorageArea
import org.example.project.core.storage.api.StoragePath
import org.example.project.core.storage.internal.DefaultFileStorage
import org.example.project.core.storage.testing.TestStorageDirectories
import org.example.project.domain.model.wechat.WeChatAccount
import org.example.project.domain.model.wechat.WeChatArticle
import org.example.project.domain.model.wechat.WeChatCardType
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class WeChatMpRepositoryImplTest {

    private lateinit var dao: FakeWeChatMpDao
    private lateinit var repository: WeChatMpRepositoryImpl

    @BeforeTest
    fun setUp() = runTest {
        dao = FakeWeChatMpDao()
        dao.clearAll()
        repository = WeChatMpRepositoryImpl(weChatMpDao = dao)
    }

    @AfterTest
    fun tearDown() {
        AppMockConfig.resetToDefaults()
    }

    @Test
    fun testInitialDataSeedingOnEmptyDatabase() = runTest {
        val accounts = repository.observeFrequentlyReadAccounts().first()
        val featured = repository.observeFeaturedArticle().first()
        val waterfall = repository.observeWaterfallArticles().first()

        assertNotNull(featured)
        assertEquals("art_featured_01", featured.id)
        assertTrue(featured.isTopSticky)
        assertTrue(accounts.isNotEmpty())
        assertTrue(waterfall.isNotEmpty())

        val daoWaterfall = dao.observeWaterfallArticles().first()
        assertTrue(daoWaterfall.isNotEmpty())
    }

    @Test
    fun testRefreshDataUpdatesArticles() = runTest {
        val result = repository.refreshData()
        assertTrue(result.isSuccess)

        val featured = repository.observeFeaturedArticle().first()
        assertNotNull(featured)

        val waterfall = repository.observeWaterfallArticles().first()
        assertTrue(waterfall.isNotEmpty())
    }

    @Test
    fun testLoadMoreArticlesAppendsToList() = runTest {
        val initialList = repository.observeWaterfallArticles().first()
        val initialSize = initialList.size

        val loadResult = repository.loadMoreArticles()
        assertTrue(loadResult.isSuccess)
        val newItems = loadResult.getOrNull()
        assertNotNull(newItems)
        assertEquals(2, newItems.size)

        val updatedList = repository.observeWaterfallArticles().first()
        assertEquals(initialSize + 2, updatedList.size)
    }

    @Test
    fun testPaginationCursorPersistsAcrossRepositoryRecreation() = runTest {
        val testDirs = TestStorageDirectories()
        val fileStorage = DefaultFileStorage(directories = testDirs)
        val repo1 = WeChatMpRepositoryImpl(weChatMpDao = dao, fileStorage = fileStorage)

        assertTrue(repo1.refreshData().isSuccess)
        assertTrue(repo1.loadMoreArticles().isSuccess)

        val cursorAfterFirstLoad = fileStorage
            .read(StorageArea.PERSISTENT, StoragePath("wechat_mp/pagination.json"))
            .decodeToString()
        assertEquals("{\"nextPage\":3}", cursorAfterFirstLoad)

        val repo2 = WeChatMpRepositoryImpl(weChatMpDao = dao, fileStorage = fileStorage)
        assertTrue(repo2.loadMoreArticles().isSuccess)

        val cursorAfterRecreatedLoad = fileStorage
            .read(StorageArea.PERSISTENT, StoragePath("wechat_mp/pagination.json"))
            .decodeToString()
        assertEquals("{\"nextPage\":4}", cursorAfterRecreatedLoad)
    }

    @Test
    fun testRefreshFailurePreservesCursorAfterRepositoryRecreation() = runTest {
        val testDirs = TestStorageDirectories()
        val fileStorage = DefaultFileStorage(directories = testDirs)
        val repo1 = WeChatMpRepositoryImpl(weChatMpDao = dao, fileStorage = fileStorage)

        assertTrue(repo1.refreshData().isSuccess)
        assertTrue(repo1.loadMoreArticles().isSuccess)
        assertEquals(
            "{\"nextPage\":3}",
            fileStorage.read(
                StorageArea.PERSISTENT,
                StoragePath("wechat_mp/pagination.json")
            ).decodeToString()
        )

        val pageThreeArticle = WeChatArticle(
            id = "art_page_3",
            account = WeChatAccount(
                id = "acc_page_3",
                name = "分页测试号",
                avatarUrl = "https://example.com/avatar.png"
            ),
            title = "第三页文章",
            coverUrl = "https://example.com/cover.png",
            cardType = WeChatCardType.WATERFALL_GRID
        )
        val pageThreeResponse = Json.encodeToString(listOf(pageThreeArticle))
        val requestedPages = mutableListOf<Int?>()
        val mockEngine = MockEngine { request ->
            val page = request.url.parameters["page"]?.toIntOrNull()
            requestedPages += page
            if (page == 3) {
                respond(
                    content = pageThreeResponse,
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            } else {
                respond(
                    content = "{\"error\":\"refresh failed\"}",
                    status = HttpStatusCode.InternalServerError,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            }
        }
        val tokenStore = TokenStore(InMemorySecureStorage())
        val networkContainer = DefaultNetworkContainer(
            NetworkClientFactoryImpl(
                tokenStore = tokenStore,
                tokenRefresher = TokenRefresher(tokenStore),
                customEngine = mockEngine
            )
        )

        AppMockConfig.isWeChatMpMockEnabled = false
        val repo2 = WeChatMpRepositoryImpl(
            weChatMpDao = dao,
            networkContainer = networkContainer,
            fileStorage = fileStorage
        )

        try {
            assertTrue(repo2.refreshData().isFailure)
            assertEquals(
                "{\"nextPage\":3}",
                fileStorage.read(
                    StorageArea.PERSISTENT,
                    StoragePath("wechat_mp/pagination.json")
                ).decodeToString()
            )

            assertTrue(repo2.loadMoreArticles().isSuccess)
            assertEquals(3, requestedPages.last())
            assertEquals(
                "{\"nextPage\":4}",
                fileStorage.read(
                    StorageArea.PERSISTENT,
                    StoragePath("wechat_mp/pagination.json")
                ).decodeToString()
            )
        } finally {
            networkContainer.close()
        }
    }

    @Test
    fun testDislikeArticleRemovesFromListAndDao() = runTest {
        val initialList = repository.observeWaterfallArticles().first()
        assertTrue(initialList.isNotEmpty())
        val targetId = initialList.first().id

        val dislikeResult = repository.dislikeArticle(targetId, "广告软文")
        assertTrue(dislikeResult.isSuccess)

        val updatedList = repository.observeWaterfallArticles().first()
        assertFalse(updatedList.any { it.id == targetId })

        val daoList = dao.observeWaterfallArticles().first()
        assertFalse(daoList.any { it.id == targetId })
    }

    @Test
    fun testDislikePersistenceAcrossRepositoryRecreation() = runTest {
        val testDirs = TestStorageDirectories()
        val fileStorage = DefaultFileStorage(directories = testDirs)
        val repo1 = WeChatMpRepositoryImpl(weChatMpDao = dao, fileStorage = fileStorage)

        val initialList = repo1.observeWaterfallArticles().first()
        assertTrue(initialList.isNotEmpty())
        val targetId = initialList.first().id

        val dislikeResult = repo1.dislikeArticle(targetId, "不感兴趣")
        assertTrue(dislikeResult.isSuccess)

        // 验证持久化文件已写入
        assertTrue(fileStorage.exists(StorageArea.PERSISTENT, StoragePath("wechat_mp/dislikes.json")))

        // 重建 Repository 模拟进程重启
        val repo2 = WeChatMpRepositoryImpl(weChatMpDao = dao, fileStorage = fileStorage)
        val listAfterRestart = repo2.observeWaterfallArticles().first()
        assertFalse(listAfterRestart.any { it.id == targetId })
    }

    @Test
    fun testDislikedArticleDoesNotReappearOnRefresh() = runTest {
        val testDirs = TestStorageDirectories()
        val fileStorage = DefaultFileStorage(directories = testDirs)
        val repo = WeChatMpRepositoryImpl(weChatMpDao = dao, fileStorage = fileStorage)

        val initialList = repo.observeWaterfallArticles().first()
        val targetId = initialList.first().id

        repo.dislikeArticle(targetId, "减少此类推荐")
        assertFalse(repo.observeWaterfallArticles().first().any { it.id == targetId })

        // 触发刷新
        val refreshResult = repo.refreshData()
        assertTrue(refreshResult.isSuccess)

        // 刷新后依然不出现
        assertFalse(repo.observeWaterfallArticles().first().any { it.id == targetId })
    }

    @Test
    fun testMarkAccountAsRead() = runTest {
        val accounts = repository.observeFrequentlyReadAccounts().first()
        assertTrue(accounts.isNotEmpty())
        val targetAccount = accounts.first { it.hasUnread }

        val markResult = repository.markAccountAsRead(targetAccount.id)
        assertTrue(markResult.isSuccess)

        val updatedAccounts = repository.observeFrequentlyReadAccounts().first()
        val updated = updatedAccounts.find { it.id == targetAccount.id }
        assertNotNull(updated)
        assertFalse(updated.hasUnread)
    }

    @Test
    fun testToggleFollowAccount() = runTest {
        val accounts = repository.observeFrequentlyReadAccounts().first()
        assertTrue(accounts.isNotEmpty())
        val target = accounts.first()
        val initialFollowed = target.isFollowed

        val toggleResult = repository.toggleFollowAccount(target.id)
        assertTrue(toggleResult.isSuccess)
        assertEquals(!initialFollowed, toggleResult.getOrNull())

        val updatedAccounts = repository.observeFrequentlyReadAccounts().first()
        val updated = updatedAccounts.find { it.id == target.id }
        assertNotNull(updated)
        assertEquals(!initialFollowed, updated.isFollowed)
    }

    @Test
    fun testFileStorageCacheSnapshotPersistence() = runTest {
        val testDirs = TestStorageDirectories()
        val fileStorage = DefaultFileStorage(directories = testDirs)
        val fileRepo = WeChatMpRepositoryImpl(weChatMpDao = dao, fileStorage = fileStorage)

        val refreshResult = fileRepo.refreshData()
        assertTrue(refreshResult.isSuccess)

        val cacheBytes = fileStorage.read(StorageArea.CACHE, StoragePath("wechat_mp/articles_cache.json"))
        assertNotNull(cacheBytes)
        assertTrue(cacheBytes.isNotEmpty())
    }
}
