/**
 * @File: FakeInstagramDao.kt
 * @Package: org.example.project.core.database
 * @Description: Instagram DAO 离线内存与响应式 Flow 持久化实现
 * @Author: 何聚敛
 * @Date: 2026-08-11
 */
package org.example.project.core.database

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import org.example.project.data.database.dao.instagram.InstagramDao
import org.example.project.data.database.entity.instagram.InstagramPostEntity

class FakeInstagramDao : InstagramDao {
    private val postsTable = MutableStateFlow<List<InstagramPostEntity>>(emptyList())

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
            map.values.toList()
        }
    }

    override suspend fun deletePost(postId: String) {
        postsTable.update { current ->
            current.filterNot { it.id == postId }
        }
    }

    override suspend fun clearAll() {
        postsTable.value = emptyList()
    }
}
