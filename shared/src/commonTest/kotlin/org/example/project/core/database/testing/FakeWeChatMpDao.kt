/**
 * @File: FakeWeChatMpDao.kt
 * @Package: org.example.project.core.database
 * @Description: 仅供单元测试使用的 FakeWeChatMpDao 内存响应式模拟实现
 * @Author: 何聚敛
 * @Date: 2026-08-18
 */
package org.example.project.core.database

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import org.example.project.data.database.dao.wechat.WeChatMpDao
import org.example.project.data.database.entity.wechat.WeChatArticleEntity

class FakeWeChatMpDao : WeChatMpDao {
    private val articlesFlow = MutableStateFlow<List<WeChatArticleEntity>>(emptyList())

    override fun observeWaterfallArticles(): Flow<List<WeChatArticleEntity>> {
        return articlesFlow.map { list ->
            list.filterNot { it.isTopSticky }
                .sortedByDescending { it.publishTimestamp }
        }
    }

    override fun observeFeaturedArticle(): Flow<WeChatArticleEntity?> {
        return articlesFlow.map { list ->
            list.firstOrNull { it.isTopSticky }
        }
    }

    override suspend fun insertArticles(articles: List<WeChatArticleEntity>) {
        articlesFlow.update { current ->
            val mutable = current.toMutableList()
            articles.forEach { incoming ->
                mutable.removeAll { it.id == incoming.id }
                mutable.add(incoming)
            }
            mutable
        }
    }

    override suspend fun deleteArticle(articleId: String) {
        articlesFlow.update { current ->
            current.filterNot { it.id == articleId }
        }
    }

    override suspend fun clearAll() {
        articlesFlow.value = emptyList()
    }
}
