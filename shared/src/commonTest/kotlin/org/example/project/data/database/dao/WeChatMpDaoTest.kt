/**
 * @File: WeChatMpDaoTest.kt
 * @Package: org.example.project.data.database.dao
 * @Description: 微信公众号本地Room DAO数据持久化与响应式Flow监听单元测试套件
 * @Author: 何聚敛
 * @Date: 2026-08-18
 */
package org.example.project.data.database.dao

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.example.project.core.database.FakeWeChatMpDao
import org.example.project.data.database.entity.wechat.WeChatArticleEntity
import org.example.project.domain.model.wechat.WeChatAccount
import org.example.project.domain.model.wechat.WeChatArticle
import org.example.project.domain.model.wechat.WeChatCardType
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WeChatMpDaoTest {

    private lateinit var dao: FakeWeChatMpDao

    @BeforeTest
    fun setUp() = runTest {
        dao = FakeWeChatMpDao()
        dao.clearAll()
    }

    @Test
    fun testInsertAndObserveWaterfallArticles() = runTest {
        val article1 = WeChatArticle(
            id = "art_1",
            account = WeChatAccount(id = "acc_1", name = "钛媒体", avatarUrl = "https://test.png", isFollowed = true),
            title = "科技新前沿观察",
            summary = "观察摘要",
            coverUrl = "https://cover1.png",
            publishTimeText = "10分钟前",
            publishTimestamp = 1000L,
            cardType = WeChatCardType.WATERFALL_GRID,
            isFollowedAccount = true,
            readCount = 12000,
            likeCount = 300,
            isLiked = false,
            isTopSticky = false,
            videoDuration = "",
            coverAspectRatio = 1.0f,
            articleUrl = ""
        )
        val article2 = WeChatArticle(
            id = "art_2",
            account = WeChatAccount(id = "acc_2", name = "极客公园", avatarUrl = "https://test2.png", isFollowed = false),
            title = "大模型赋能落地实践",
            summary = "大模型摘要",
            coverUrl = "https://cover2.png",
            publishTimeText = "1小时前",
            publishTimestamp = 2000L,
            cardType = WeChatCardType.HORIZONTAL_ROW,
            isFollowedAccount = false,
            readCount = 8500,
            likeCount = 210,
            isLiked = false,
            isTopSticky = false,
            videoDuration = "",
            coverAspectRatio = 1.2f,
            articleUrl = ""
        )

        dao.insertArticles(listOf(
            WeChatArticleEntity.fromDomainModel(article1),
            WeChatArticleEntity.fromDomainModel(article2)
        ))

        val list = dao.observeWaterfallArticles().first()
        assertEquals(2, list.size)
        assertEquals("art_2", list[0].id)
        assertEquals("大模型赋能落地实践", list[0].title)
        assertEquals("art_1", list[1].id)
    }

    @Test
    fun testInsertAndObserveFeaturedArticle() = runTest {
        val featured = WeChatArticle(
            id = "art_feat_1",
            account = WeChatAccount(id = "acc_csdn", name = "CSDN", avatarUrl = "https://csdn.png", isFollowed = true),
            title = "头条重要资讯置顶推荐",
            summary = "置顶摘要",
            coverUrl = "https://featured_cover.png",
            publishTimeText = "置顶",
            publishTimestamp = 5000L,
            cardType = WeChatCardType.FEATURED_BANNER,
            isFollowedAccount = true,
            readCount = 99999,
            likeCount = 5000,
            isLiked = false,
            isTopSticky = true,
            videoDuration = "",
            coverAspectRatio = 1.77f,
            articleUrl = ""
        )

        dao.insertArticles(listOf(WeChatArticleEntity.fromDomainModel(featured)))

        val observed = dao.observeFeaturedArticle().first()
        assertNotNull(observed)
        assertEquals("art_feat_1", observed.id)
        assertTrue(observed.isTopSticky)
        assertEquals("头条重要资讯置顶推荐", observed.title)
    }

    @Test
    fun testDeleteArticle() = runTest {
        val article = WeChatArticle(
            id = "art_del_target",
            account = WeChatAccount(id = "acc_temp", name = "临时", avatarUrl = ""),
            title = "即将被删除的文章",
            coverUrl = "",
            publishTimeText = "刚刚",
            publishTimestamp = 3000L,
            cardType = WeChatCardType.WATERFALL_GRID,
            isFollowedAccount = false,
            isTopSticky = false,
            coverAspectRatio = 1.0f
        )

        dao.insertArticles(listOf(WeChatArticleEntity.fromDomainModel(article)))
        assertEquals(1, dao.observeWaterfallArticles().first().size)

        dao.deleteArticle("art_del_target")
        assertEquals(0, dao.observeWaterfallArticles().first().size)
    }

    @Test
    fun testClearAll() = runTest {
        val featured = WeChatArticle(
            id = "feat_clear",
            account = WeChatAccount(id = "acc_f", name = "置顶", avatarUrl = ""),
            title = "置顶内容",
            coverUrl = "",
            publishTimeText = "置顶",
            publishTimestamp = 1000L,
            cardType = WeChatCardType.FEATURED_BANNER,
            isTopSticky = true
        )
        val waterfall = WeChatArticle(
            id = "water_clear",
            account = WeChatAccount(id = "acc_w", name = "瀑布", avatarUrl = ""),
            title = "瀑布流内容",
            coverUrl = "",
            publishTimeText = "刚刚",
            publishTimestamp = 1000L,
            cardType = WeChatCardType.WATERFALL_GRID,
            isTopSticky = false
        )

        dao.insertArticles(listOf(
            WeChatArticleEntity.fromDomainModel(featured),
            WeChatArticleEntity.fromDomainModel(waterfall)
        ))
        assertEquals(1, dao.observeWaterfallArticles().first().size)
        assertNotNull(dao.observeFeaturedArticle().first())

        dao.clearAll()
        assertEquals(0, dao.observeWaterfallArticles().first().size)
        assertNull(dao.observeFeaturedArticle().first())
    }
}
