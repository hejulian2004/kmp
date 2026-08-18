/**
 * @File: FakeFeedLineDao.kt
 * @Package: org.example.project.core.database
 * @Description: 仅供单元测试使用的 FakeFeedLineDao 内存响应式模拟实现
 * @Author: 何聚敛
 * @Date: 2026-08-18
 */
package org.example.project.core.database

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.example.project.data.database.dao.feedline.FeedLineDao
import org.example.project.data.database.entity.feedline.FeedLineNotificationEntity
import org.example.project.data.database.entity.feedline.FeedLinePostEntity

class FakeFeedLineDao : FeedLineDao {
    private val postsFlow = MutableStateFlow<List<FeedLinePostEntity>>(emptyList())
    private val notificationsFlow = MutableStateFlow<List<FeedLineNotificationEntity>>(emptyList())

    override fun observePosts(): Flow<List<FeedLinePostEntity>> = postsFlow.asStateFlow()

    override suspend fun insertPosts(posts: List<FeedLinePostEntity>) {
        postsFlow.update { current ->
            val mutable = current.toMutableList()
            posts.forEach { incoming ->
                mutable.removeAll { it.id == incoming.id }
                mutable.add(incoming)
            }
            mutable
        }
    }

    override suspend fun deletePost(postId: String) {
        postsFlow.update { current ->
            current.filterNot { it.id == postId }
        }
    }

    override fun observeNotifications(): Flow<List<FeedLineNotificationEntity>> = notificationsFlow.asStateFlow()

    override suspend fun insertNotifications(notifications: List<FeedLineNotificationEntity>) {
        notificationsFlow.update { current ->
            val mutable = current.toMutableList()
            notifications.forEach { incoming ->
                mutable.removeAll { it.id == incoming.id }
                mutable.add(incoming)
            }
            mutable
        }
    }

    override suspend fun clearPosts() {
        postsFlow.value = emptyList()
    }

    override suspend fun clearNotifications() {
        notificationsFlow.value = emptyList()
    }
}
