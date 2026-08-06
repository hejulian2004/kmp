/**
 * @File: InstagramDaoImpl.kt
 * @Package: org.example.project.data.database.dao.instagram
 * @Description: Instagram DAO响应式表状态与本地磁盘持久化实现类
 * @Author: 何聚敛
 * @Date: 2026-08-05
 */
package org.example.project.data.database.dao.instagram

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.example.project.data.database.entity.instagram.InstagramPostEntity
import org.example.project.platform.readStorageFile
import org.example.project.platform.writeStorageFile

class InstagramDaoImpl : InstagramDao {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }
    private val postsFile = "instagram_posts_db.json"

    private val postsTable = MutableStateFlow<List<InstagramPostEntity>>(loadInitialPosts())

    private fun loadInitialPosts(): List<InstagramPostEntity> {
        val content = readStorageFile(postsFile)
        return if (!content.isNullOrBlank()) {
            runCatching { json.decodeFromString<List<InstagramPostEntity>>(content) }.getOrDefault(emptyList())
        } else emptyList()
    }

    private fun persistPosts(posts: List<InstagramPostEntity>) {
        runCatching {
            writeStorageFile(postsFile, json.encodeToString(posts))
        }
    }

    override fun observePosts(): Flow<List<InstagramPostEntity>> {
        return postsTable.map { list -> list.filter { !it.isStory } }
    }

    override fun observeStories(): Flow<List<InstagramPostEntity>> {
        return postsTable.map { list -> list.filter { it.isStory } }
    }

    override suspend fun insertPosts(posts: List<InstagramPostEntity>) {
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
    }

    override suspend fun clearAll() {
        postsTable.value = emptyList()
        persistPosts(emptyList())
    }
}
