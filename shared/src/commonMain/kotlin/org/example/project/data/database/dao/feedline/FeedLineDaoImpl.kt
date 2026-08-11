/**
 * @File: FeedLineDaoImpl.kt
 * @Package: org.example.project.data.database.dao.feedline
 * @Description: 朋友圈DAO响应式表状态与本地磁盘持久化实现类
 * @Author: 何聚敛
 * @Date: 2026-08-05
 */
package org.example.project.data.database.dao.feedline

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.example.project.data.database.entity.feedline.FeedLineNotificationEntity
import org.example.project.data.database.entity.feedline.FeedLinePostEntity
import org.example.project.platform.readStorageFile
import org.example.project.platform.writeStorageFile

class FeedLineDaoImpl : FeedLineDao {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }
    private val postsFile = "feedline_posts_db.json"
    private val notificationsFile = "feedline_notifications_db.json"

    private val postsTable = MutableStateFlow<List<FeedLinePostEntity>>(loadInitialPosts())
    private val notificationsTable = MutableStateFlow<List<FeedLineNotificationEntity>>(loadInitialNotifications())

    private fun loadInitialPosts(): List<FeedLinePostEntity> {
        val content = readStorageFile(postsFile)
        return if (!content.isNullOrBlank()) {
            try {
                json.decodeFromString<List<FeedLinePostEntity>>(content)
            } catch (e: Exception) {
                println("[FeedLineDaoImpl] decode posts error: ${e.message}")
                e.printStackTrace()
                emptyList()
            }
        } else emptyList()
    }

    private fun loadInitialNotifications(): List<FeedLineNotificationEntity> {
        val content = readStorageFile(notificationsFile)
        return if (!content.isNullOrBlank()) {
            runCatching { json.decodeFromString<List<FeedLineNotificationEntity>>(content) }.getOrDefault(emptyList())
        } else emptyList()
    }

    private fun persistPosts(posts: List<FeedLinePostEntity>) {
        runCatching {
            writeStorageFile(postsFile, json.encodeToString(posts))
        }
    }

    private fun persistNotifications(notifications: List<FeedLineNotificationEntity>) {
        runCatching {
            writeStorageFile(notificationsFile, json.encodeToString(notifications))
        }
    }

    override fun observePosts(): Flow<List<FeedLinePostEntity>> = postsTable.asStateFlow()

    override suspend fun insertPosts(posts: List<FeedLinePostEntity>) {
        postsTable.update { current ->
            val map = current.associateBy { it.id }.toMutableMap()
            posts.forEach { map[it.id] = it }
            val updated = map.values.toList()
            persistPosts(updated)
            updated
        }
    }

    override suspend fun deletePost(postId: String) {
        postsTable.update { current ->
            val updated = current.filterNot { it.id == postId }
            persistPosts(updated)
            updated
        }
        notificationsTable.update { current ->
            val updated = current.filterNot { it.postJson.contains(postId) }
            persistNotifications(updated)
            updated
        }
    }

    override fun observeNotifications(): Flow<List<FeedLineNotificationEntity>> = notificationsTable.asStateFlow()

    override suspend fun insertNotifications(notifications: List<FeedLineNotificationEntity>) {
        notificationsTable.update { current ->
            val map = current.associateBy { it.id }.toMutableMap()
            notifications.forEach { map[it.id] = it }
            val updated = map.values.toList()
            persistNotifications(updated)
            updated
        }
    }

    override suspend fun clearPosts() {
        postsTable.value = emptyList()
        persistPosts(emptyList())
    }

    override suspend fun clearNotifications() {
        notificationsTable.value = emptyList()
        persistNotifications(emptyList())
    }
}
