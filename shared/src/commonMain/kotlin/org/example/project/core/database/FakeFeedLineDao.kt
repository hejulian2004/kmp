/**
 * @File: FakeFeedLineDao.kt
 * @Package: org.example.project.core.database
 * @Description: 朋友圈 DAO 离线内存与响应式 Flow 持久化实现
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
    private val postsTable = MutableStateFlow<List<FeedLinePostEntity>>(emptyList())
    private val notificationsTable = MutableStateFlow<List<FeedLineNotificationEntity>>(emptyList())

    override fun observePosts(): Flow<List<FeedLinePostEntity>> = postsTable.asStateFlow()

    override suspend fun insertPosts(posts: List<FeedLinePostEntity>) {
        postsTable.update { current ->
            val map = current.associateBy { it.id }.toMutableMap()
            posts.forEach { map[it.id] = it }
            map.values.toList()
        }
    }

    override suspend fun deletePost(postId: String) {
        postsTable.update { current ->
            current.filterNot { it.id == postId }
        }
        notificationsTable.update { current ->
            current.filterNot { it.postJson.contains(postId) }
        }
    }

    override fun observeNotifications(): Flow<List<FeedLineNotificationEntity>> = notificationsTable.asStateFlow()

    override suspend fun insertNotifications(notifications: List<FeedLineNotificationEntity>) {
        notificationsTable.update { current ->
            val map = current.associateBy { it.id }.toMutableMap()
            notifications.forEach { map[it.id] = it }
            map.values.toList()
        }
    }

    override suspend fun clearPosts() {
        postsTable.value = emptyList()
    }

    override suspend fun clearNotifications() {
        notificationsTable.value = emptyList()
    }
}
