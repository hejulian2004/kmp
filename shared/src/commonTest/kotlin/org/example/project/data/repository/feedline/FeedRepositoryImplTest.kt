/**
 * @File: FeedRepositoryImplTest.kt
 * @Package: org.example.project.data.repository.feedline
 * @Description: FeedRepositoryImpl发布动态、通知与互动持久化逻辑单元测试
 * @Author: 何聚敛
 * @Date: 2026-08-18
 */
package org.example.project.data.repository.feedline

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.example.project.core.database.FakeFeedLineDao
import org.example.project.domain.model.feedline.FeedLineMedia
import org.example.project.domain.model.feedline.FeedLineNotification
import org.example.project.domain.model.feedline.FeedLinePost
import org.example.project.domain.model.feedline.FeedLineUser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class FeedRepositoryImplTest {

    @Test
    fun testCreatePostSavesToDatabaseSuccessfully() = runTest {
        val mockDao = FakeFeedLineDao()
        val repository = FeedRepositoryImpl(feedLineDao = mockDao)

        val testUser = FeedLineUser(
            id = "user_test_101",
            name = "测试用户",
            avatarUrl = "https://example.com/avatar.jpg"
        )
        val testContent = "测试朋友圈发布动态内容"
        val testMedia = listOf(
            FeedLineMedia.Image(url = "https://example.com/image1.jpg")
        )

        repository.createPost(
            user = testUser,
            content = testContent,
            mediaList = testMedia
        )

        val posts = repository.getFeedPosts().first()
        val createdPost = posts.find { it.content == testContent }

        assertNotNull(createdPost)
        assertEquals("user_test_101", createdPost.postUser.id)
        assertEquals("测试用户", createdPost.postUser.name)
        assertEquals(1, createdPost.mediaList.size)
        assertTrue(createdPost.mediaList.first() is FeedLineMedia.Image)
        assertEquals("https://example.com/image1.jpg", (createdPost.mediaList.first() as FeedLineMedia.Image).url)

        val savedEntities = mockDao.observePosts().first()
        val savedEntity = savedEntities.find { it.id == createdPost.id }
        assertNotNull(savedEntity)
        assertEquals(testContent, savedEntity.content)
    }

    @Test
    fun testPostsPersistAcrossAppRestarts() = runTest {
        val daoInstance1 = FakeFeedLineDao()
        val repository1 = FeedRepositoryImpl(feedLineDao = daoInstance1)

        val testUser = FeedLineUser(id = "u_restart", name = "重启测试用户", avatarUrl = "")
        val testContent = "重启持久化验证内容"

        repository1.createPost(user = testUser, content = testContent, mediaList = emptyList())

        // 模拟应用重新启动（复用持久化 DAO 实例创建新 Repository）
        val repository2 = FeedRepositoryImpl(feedLineDao = daoInstance1)

        val postsAfterRestart = repository2.getFeedPosts().first()
        val restoredPost = postsAfterRestart.find { it.content == testContent }

        assertNotNull(restoredPost)
        assertEquals("u_restart", restoredPost.postUser.id)
    }

    @Test
    fun testNotificationPersistenceAndReadStatus() = runTest {
        val dao1 = FakeFeedLineDao()
        val repo1 = FeedRepositoryImpl(feedLineDao = dao1)

        val user = FeedLineUser("u_notify", "通知用户", "")
        val post = FeedLinePost(id = "post_notify_1", postUser = user, content = "通知测试帖子")
        val notification = FeedLineNotification(
            id = "notify_101",
            user = user,
            post = post,
            isLikeNotification = true,
            isRead = false
        )

        repo1.addNotification(notification)

        val notifications1 = repo1.getNotifications().first()
        assertTrue(notifications1.any { it.id == "notify_101" })

        repo1.markAllNotificationsAsRead()

        // 模拟重启恢复验证通知持久化与已读状态
        val repo2 = FeedRepositoryImpl(feedLineDao = dao1)

        val notifications2 = repo2.getNotifications().first()
        val restoredNotify = notifications2.find { it.id == "notify_101" }

        assertNotNull(restoredNotify)
        assertTrue(restoredNotify.isRead)
    }
}
