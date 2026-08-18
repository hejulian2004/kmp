/**
 * @File: FakeWeChatMpDao.kt
 * @Package: org.example.project.core.database
 * @Description: 微信公众号DAO离线内存与响应式Flow持久化实现
 * @Author: 何聚敛
 * @Date: 2026-08-18
 */
package org.example.project.core.database

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import org.example.project.data.database.dao.wechat.WeChatMpDao
import org.example.project.data.database.entity.wechat.WeChatArticleEntity

class FakeWeChatMpDao : WeChatMpDao {
    private val articlesTable = MutableStateFlow<List<WeChatArticleEntity>>(emptyList())

    override fun observeWaterfallArticles(): Flow<List<WeChatArticleEntity>> {
        return articlesTable.map { list ->
            list.filter { !it.isTopSticky }.sortedByDescending { it.publishTimestamp }
        }
    }

    override fun observeFeaturedArticle(): Flow<WeChatArticleEntity?> {
        return articlesTable.map { list ->
            list.firstOrNull { it.isTopSticky }
        }
    }

    override suspend fun insertArticles(articles: List<WeChatArticleEntity>) {
        articlesTable.update { current ->
            val map = current.associateBy { it.id }.toMutableMap()
            articles.forEach { map[it.id] = it }
            map.values.toList()
        }
    }

    override suspend fun deleteArticle(articleId: String) {
        articlesTable.update { current ->
            current.filterNot { it.id == articleId }
        }
    }

    override suspend fun clearAll() {
        articlesTable.value = emptyList()
    }
}
