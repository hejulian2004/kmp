/**
 * @File: InstagramHomeRepositoryImplTest.kt
 * @Package: org.example.project.data.repository.instagram
 * @Description: InstagramHomeRepositoryImpl发布帖子、点赞与磁盘持久化恢复单元测试
 * @Author: 何聚敛
 * @Date: 2026-08-04
 */
package org.example.project.data.repository.instagram

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.example.project.data.database.dao.instagram.InstagramDaoImpl
import org.example.project.domain.model.instagram.InstagramMedia
import org.example.project.domain.model.instagram.ProfileUser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class InstagramHomeRepositoryImplTest {

    @Test
    fun testCreatePostAndPersistenceAcrossRestarts() = runTest {
        val dao1 = InstagramDaoImpl()
        val repository1 = InstagramHomeRepositoryImpl(instagramDao = dao1)
        Thread.sleep(100)

        val user = ProfileUser("u_insta", "insta_user", "https://example.com/avatar.jpg", "Bio", "10", "100", "50")
        val content = "测试Instagram发布贴子"
        val mediaList = listOf(InstagramMedia.Image(url = "https://example.com/photo.jpg"))

        repository1.createPost(
            user = user,
            content = content,
            mediaList = mediaList,
            location = "Shanghai, China"
        )

        val posts1 = repository1.getHomePosts().first()
        val createdPost = posts1.find { it.content == content }

        assertNotNull(createdPost)
        assertEquals("insta_user", createdPost.postUser.username)
        assertEquals("Shanghai, China", createdPost.location)

        // 模拟重启（新建 DAO 与 Repository 实例）
        val dao2 = InstagramDaoImpl()
        val repository2 = InstagramHomeRepositoryImpl(instagramDao = dao2)
        Thread.sleep(100)

        val posts2 = repository2.getHomePosts().first()
        val restoredPost = posts2.find { it.content == content }

        assertNotNull(restoredPost)
        assertEquals("insta_user", restoredPost.postUser.username)
    }

    @Test
    fun testLikeAndSavePostPersistence() = runTest {
        val dao1 = InstagramDaoImpl()
        val repository1 = InstagramHomeRepositoryImpl(instagramDao = dao1)
        Thread.sleep(100)

        val posts = repository1.getHomePosts().first()
        val targetPost = posts.first()
        val currentUser = ProfileUser("u_me", "my_user", "", "", "", "", "")

        repository1.likePost(targetPost.id, currentUser)
        repository1.savePost(targetPost.id)
        Thread.sleep(100)

        // 模拟重启恢复验证点赞与收藏持久化
        val dao2 = InstagramDaoImpl()
        val repository2 = InstagramHomeRepositoryImpl(instagramDao = dao2)
        Thread.sleep(100)

        val restoredPosts = repository2.getHomePosts().first()
        val restoredTarget = restoredPosts.find { it.id == targetPost.id }

        assertNotNull(restoredTarget)
        assertTrue(restoredTarget.isLiked)
        assertTrue(restoredTarget.isSaved)
    }
}
