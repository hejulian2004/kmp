/**
 * @File: WeChatArticleEntity.kt
 * @Package: org.example.project.data.database.entity.wechat
 * @Description: 微信公众号文章与常读号的Room本地数据库实体
 * @Author: 何聚敛
 * @Date: 2026-08-18
 */
package org.example.project.data.database.entity.wechat

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.example.project.domain.model.wechat.WeChatAccount
import org.example.project.domain.model.wechat.WeChatArticle
import org.example.project.domain.model.wechat.WeChatCardType

@Serializable
@Entity(tableName = "wechat_articles")
data class WeChatArticleEntity(
    @PrimaryKey val id: String,
    val accountJson: String,
    val title: String,
    val summary: String,
    val coverUrl: String,
    val publishTimeText: String,
    val publishTimestamp: Long,
    val cardType: String,
    val isFollowedAccount: Boolean,
    val readCount: Int,
    val likeCount: Int,
    val isLiked: Boolean,
    val isTopSticky: Boolean,
    val videoDuration: String,
    val coverAspectRatio: Float,
    val articleUrl: String
) {
    fun toDomainModel(): WeChatArticle {
        val json = Json { ignoreUnknownKeys = true; isLenient = true }
        val cardTypeEnum = try {
            WeChatCardType.valueOf(cardType)
        } catch (_: Exception) {
            WeChatCardType.WATERFALL_GRID
        }
        return WeChatArticle(
            id = id,
            account = json.decodeFromString<WeChatAccount>(accountJson),
            title = title,
            summary = summary,
            coverUrl = coverUrl,
            publishTimeText = publishTimeText,
            publishTimestamp = publishTimestamp,
            cardType = cardTypeEnum,
            isFollowedAccount = isFollowedAccount,
            readCount = readCount,
            likeCount = likeCount,
            isLiked = isLiked,
            isTopSticky = isTopSticky,
            videoDuration = videoDuration,
            coverAspectRatio = coverAspectRatio,
            articleUrl = articleUrl
        )
    }

    companion object {
        fun fromDomainModel(domain: WeChatArticle): WeChatArticleEntity {
            val json = Json { ignoreUnknownKeys = true; isLenient = true }
            return WeChatArticleEntity(
                id = domain.id,
                accountJson = json.encodeToString(domain.account),
                title = domain.title,
                summary = domain.summary,
                coverUrl = domain.coverUrl,
                publishTimeText = domain.publishTimeText,
                publishTimestamp = domain.publishTimestamp,
                cardType = domain.cardType.name,
                isFollowedAccount = domain.isFollowedAccount,
                readCount = domain.readCount,
                likeCount = domain.likeCount,
                isLiked = domain.isLiked,
                isTopSticky = domain.isTopSticky,
                videoDuration = domain.videoDuration,
                coverAspectRatio = domain.coverAspectRatio,
                articleUrl = domain.articleUrl
            )
        }
    }
}
