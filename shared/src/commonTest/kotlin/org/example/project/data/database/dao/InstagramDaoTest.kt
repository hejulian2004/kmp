/**
 * @File: InstagramDaoTest.kt
 * @Package: org.example.project.data.database.dao
 * @Description: InstagramDao 动态与 Story 增删改查及响应式 Flow 监听单元测试套件
 * @Author: 何聚敛
 * @Date: 2026-08-11
 */
package org.example.project.data.database.dao

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.example.project.core.database.FakeInstagramDao
import org.example.project.data.database.entity.instagram.InstagramPostEntity
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class InstagramDaoTest {

    private lateinit var dao: FakeInstagramDao

    @BeforeTest
    fun setUp() = runTest {
        dao = FakeInstagramDao()
        dao.clearAll()
    }

    @Test
    fun testObservePostsAndObserveStoriesFiltering() = runTest {
        val normalPost = InstagramPostEntity(
            id = "insta_post_1",
            postUserJson = "{}",
            content = "Normal Feed Post",
            location = "Tokyo",
            audioTitle = null,
            mediaListJson = "[]",
            likedUsersJson = "[]",
            commentsListJson = "[]",
            isLiked = false,
            isSaved = false,
            repostCount = 0L,
            shareCount = 0L,
            createTime = 1000L,
            unreadNotificationCount = 0,
            isStory = false
        )

        val storyPost = InstagramPostEntity(
            id = "insta_story_1",
            postUserJson = "{}",
            content = "24h Story",
            location = null,
            audioTitle = null,
            mediaListJson = "[]",
            likedUsersJson = "[]",
            commentsListJson = "[]",
            isLiked = false,
            isSaved = false,
            repostCount = 0L,
            shareCount = 0L,
            createTime = 1005L,
            unreadNotificationCount = 0,
            isStory = true
        )

        dao.insertPosts(listOf(normalPost, storyPost))

        val posts = dao.observePosts().first()
        val stories = dao.observeStories().first()

        assertEquals(1, posts.size)
        assertEquals("insta_post_1", posts.first().id)

        assertEquals(1, stories.size)
        assertEquals("insta_story_1", stories.first().id)
    }

    @Test
    fun testDeletePost() = runTest {
        val post = InstagramPostEntity(
            id = "insta_del_1",
            postUserJson = "{}",
            content = "Delete test",
            location = null,
            audioTitle = null,
            mediaListJson = "[]",
            likedUsersJson = "[]",
            commentsListJson = "[]",
            isLiked = false,
            isSaved = false,
            repostCount = null,
            shareCount = null,
            createTime = 2000L,
            unreadNotificationCount = 0,
            isStory = false
        )

        dao.insertPosts(listOf(post))
        assertEquals(1, dao.observePosts().first().size)

        dao.deletePost("insta_del_1")
        assertEquals(0, dao.observePosts().first().size)
    }

    @Test
    fun testClearAll() = runTest {
        val post = InstagramPostEntity(
            id = "p1",
            postUserJson = "{}",
            content = "Content 1",
            location = null,
            audioTitle = null,
            mediaListJson = "[]",
            likedUsersJson = "[]",
            commentsListJson = "[]",
            isLiked = false,
            isSaved = false,
            repostCount = null,
            shareCount = null,
            createTime = 1000L,
            unreadNotificationCount = 0,
            isStory = false
        )

        dao.insertPosts(listOf(post))
        dao.clearAll()

        assertEquals(0, dao.observePosts().first().size)
        assertEquals(0, dao.observeStories().first().size)
    }
}
