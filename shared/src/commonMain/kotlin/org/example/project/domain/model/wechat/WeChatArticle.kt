/**
 * @File: WeChatArticle.kt
 * @Package: org.example.project.domain.model.wechat
 * @Description: 微信公众号文章核心聚合领域实体
 * @Author: 何聚敛
 * @Date: 2026-08-18
 */
package org.example.project.domain.model.wechat

import kotlinx.serialization.Serializable
import org.example.project.platform.currentTimeMillis

/**
 * 微信公众号文章实体模型
 *
 * @property id文章唯一ID
 * @property account发布该文章的公众号主体
 * @property title文章标题
 * @property summary文章摘要或副标题
 * @property coverUrl封面图URL
 * @property publishTimeText展示用发布时间字符串 (如"2分钟前")
 * @property publishTimestamp发布时间戳 (毫秒)
 * @property cardType卡片视觉展示形态
 * @property isFollowedAccount是否为已关注公众号发布的文章 (展示"关注的号"小蓝标)
 * @property readCount阅读量
 * @property likeCount点赞/在看数
 * @property isLiked当前用户是否点赞
 * @property isTopSticky是否为常读置顶推文
 * @property videoDuration视频时长文本 (若为视频卡片)
 * @property coverAspectRatio封面图推荐显示宽高比
 * @property articleUrl文章H5链接
 */
@Serializable
data class WeChatArticle(
    val id: String,
    val account: WeChatAccount,
    val title: String,
    val summary: String = "",
    val coverUrl: String,
    val publishTimeText: String = "",
    val publishTimestamp: Long = currentTimeMillis(),
    val cardType: WeChatCardType = WeChatCardType.WATERFALL_GRID,
    val isFollowedAccount: Boolean = true,
    val readCount: Int = 0,
    val likeCount: Int = 0,
    val isLiked: Boolean = false,
    val isTopSticky: Boolean = false,
    val videoDuration: String = "",
    val coverAspectRatio: Float = 1.0f,
    val articleUrl: String = ""
)
