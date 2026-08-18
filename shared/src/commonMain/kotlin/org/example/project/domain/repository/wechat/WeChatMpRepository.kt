/**
 * @File: WeChatMpRepository.kt
 * @Package: org.example.project.domain.repository.wechat
 * @Description: 微信公众号领域数据仓库契约接口
 * @Author: 何聚敛
 * @Date: 2026-08-18
 */
package org.example.project.domain.repository.wechat

import kotlinx.coroutines.flow.Flow
import org.example.project.domain.model.wechat.WeChatAccount
import org.example.project.domain.model.wechat.WeChatArticle

/**
 * 微信公众号仓库数据接口
 */
interface WeChatMpRepository {
    /** 观察常读公众号头像列表 */
    fun observeFrequentlyReadAccounts(): Flow<List<WeChatAccount>>

    /** 观察置顶常读推文大卡片 */
    fun observeFeaturedArticle(): Flow<WeChatArticle?>

    /** 观察看一看瀑布流文章列表 */
    fun observeWaterfallArticles(): Flow<List<WeChatArticle>>

    /** 下拉刷新数据 */
    suspend fun refreshData(): Result<Unit>

    /** 上拉加载更多分页数据 */
    suspend fun loadMoreArticles(): Result<List<WeChatArticle>>

    /** 屏蔽/不感兴趣文章 */
    suspend fun dislikeArticle(articleId: String, reason: String): Result<Unit>

    /** 标记公众号为已读 (清除小绿点) */
    suspend fun markAccountAsRead(accountId: String): Result<Unit>

    /** 关注/取关公众号 */
    suspend fun toggleFollowAccount(accountId: String): Result<Boolean>
}
