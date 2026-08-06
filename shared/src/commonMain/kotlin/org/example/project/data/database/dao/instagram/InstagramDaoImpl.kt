/**
 * @File: InstagramDaoImpl.kt
 * @Package: org.example.project.data.database.dao.instagram
 * @Description: Instagram DAO响应式表状态实现类
 * @Author: 何聚敛
 * @Date: 2026-08-05
 */
package org.example.project.data.database.dao.instagram

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import org.example.project.data.database.entity.instagram.InstagramPostEntity

class InstagramDaoImpl : InstagramDao {
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
