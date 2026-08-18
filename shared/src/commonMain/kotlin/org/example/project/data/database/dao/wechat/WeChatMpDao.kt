/**
 * @File: WeChatMpDao.kt
 * @Package: org.example.project.data.database.dao.wechat
 * @Description: 微信公众号文章Room DAO数据访问契约接口
 * @Author: 何聚敛
 * @Date: 2026-08-18
 */
package org.example.project.data.database.dao.wechat

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.example.project.data.database.entity.wechat.WeChatArticleEntity

@Dao
interface WeChatMpDao {
    @Query("SELECT * FROM wechat_articles WHERE isTopSticky = 0 ORDER BY publishTimestamp DESC")
    fun observeWaterfallArticles(): Flow<List<WeChatArticleEntity>>

    @Query("SELECT * FROM wechat_articles WHERE isTopSticky = 1 LIMIT 1")
    fun observeFeaturedArticle(): Flow<WeChatArticleEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticles(articles: List<WeChatArticleEntity>)

    @Query("DELETE FROM wechat_articles WHERE id = :articleId")
    suspend fun deleteArticle(articleId: String)

    @Query("DELETE FROM wechat_articles")
    suspend fun clearAll()
}
