/**
 * @File: FeedLineDaoImpl.kt
 * @Package: org.example.project.data.database.dao.feedline
 * @Description: 朋友圈 DAO 响应式表状态实现类
 * @Author: 何聚敛
 * @Date: 2026-08-04
 */
package org.example.project.data.database.dao.feedline

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.example.project.data.database.entity.feedline.FeedLineNotificationEntity
import org.example.project.data.database.entity.feedline.FeedLinePostEntity

class FeedLineDaoImpl : FeedLineDao {
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
