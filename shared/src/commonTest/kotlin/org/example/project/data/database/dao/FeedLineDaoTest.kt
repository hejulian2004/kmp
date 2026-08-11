/**
 * @File: FeedLineDaoTest.kt
 * @Package: org.example.project.data.database.dao
 * @Description: FeedLineDao 数据持久化与响应式 Flow 监听单元测试套件
 * @Author: 何聚敛
 * @Date: 2026-08-11
 */
package org.example.project.data.database.dao

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.example.project.data.database.dao.feedline.FeedLineDaoImpl
import org.example.project.data.database.entity.feedline.FeedLineNotificationEntity
import org.example.project.data.database.entity.feedline.FeedLinePostEntity
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FeedLineDaoTest {

    private lateinit var dao: FeedLineDaoImpl

    @BeforeTest
    fun setUp() = runTest {
        dao = FeedLineDaoImpl()
        dao.clearPosts()
        dao.clearNotifications()
    }

    @Test
    fun testInsertAndObservePosts() = runTest {
        val post1 = FeedLinePostEntity(
            id = "post_1",
            postUserJson = """{"id":"u1","name":"Alice"}""",
            content = "First feed line post content",
            mediaListJson = "[]",
            likedUsersJson = "[]",
            commentsListJson = "[]",
            isLiked = false,
            createTime = 1000L,
            unreadNotificationCount = 0
        )

        dao.insertPosts(listOf(post1))
        val posts = dao.observePosts().first()

        assertEquals(1, posts.size)
        assertEquals("post_1", posts.first().id)
        assertEquals("First feed line post content", posts.first().content)
    }

    @Test
    fun testDeletePost() = runTest {
        val post1 = FeedLinePostEntity(
            id = "post_del_1",
            postUserJson = "{}",
            content = "To be deleted",
            mediaListJson = "[]",
            likedUsersJson = "[]",
            commentsListJson = "[]",
            isLiked = false,
            createTime = 2000L,
            unreadNotificationCount = 0
        )

        dao.insertPosts(listOf(post1))
        assertEquals(1, dao.observePosts().first().size)

        dao.deletePost("post_del_1")
        assertEquals(0, dao.observePosts().first().size)
    }

    @Test
    fun testInsertAndObserveNotifications() = runTest {
        val notification = FeedLineNotificationEntity(
            id = "noti_1",
            userJson = """{"id":"u2","name":"Bob"}""",
            postJson = """{"id":"post_1"}""",
            commentJson = null,
            isLikeNotification = true,
            isDelete = false,
            isRead = false,
            createdTime = 3000L
        )

        dao.insertNotifications(listOf(notification))
        val notifications = dao.observeNotifications().first()

        assertEquals(1, notifications.size)
        assertEquals("noti_1", notifications.first().id)
        assertTrue(notifications.first().isLikeNotification)
    }

    @Test
    fun testClearPostsAndNotifications() = runTest {
        val post = FeedLinePostEntity(
            id = "p_clear",
            postUserJson = "{}",
            content = "Clear post",
            mediaListJson = "[]",
            likedUsersJson = "[]",
            commentsListJson = "[]",
            isLiked = false,
            createTime = 4000L,
            unreadNotificationCount = 0
        )
        val noti = FeedLineNotificationEntity(
            id = "n_clear",
            userJson = "{}",
            postJson = "{}",
            commentJson = null,
            isLikeNotification = false,
            isDelete = false,
            isRead = true,
            createdTime = 4000L
        )

        dao.insertPosts(listOf(post))
        dao.insertNotifications(listOf(noti))

        dao.clearPosts()
        dao.clearNotifications()

        assertEquals(0, dao.observePosts().first().size)
        assertEquals(0, dao.observeNotifications().first().size)
    }
}
