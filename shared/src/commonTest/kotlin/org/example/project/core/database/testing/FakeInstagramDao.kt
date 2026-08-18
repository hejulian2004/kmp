/**
 * @File: FakeInstagramDao.kt
 * @Package: org.example.project.core.database
 * @Description: 仅供单元测试使用的 FakeInstagramDao 内存响应式模拟实现
 * @Author: 何聚敛
 * @Date: 2026-08-18
 */
package org.example.project.core.database

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import org.example.project.data.database.dao.instagram.InstagramDao
import org.example.project.data.database.entity.instagram.InstagramPostEntity

class FakeInstagramDao : InstagramDao {
    private val postsFlow = MutableStateFlow<List<InstagramPostEntity>>(emptyList())

    override fun observePosts(): Flow<List<InstagramPostEntity>> {
        return postsFlow.map { list -> list.filterNot { it.isStory } }
    }

    override fun observeStories(): Flow<List<InstagramPostEntity>> {
        return postsFlow.map { list -> list.filter { it.isStory } }
    }

    override suspend fun insertPosts(posts: List<InstagramPostEntity>) {
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

    override suspend fun clearAll() {
        postsFlow.value = emptyList()
    }
}
