/**
 * @File: FeedLinePostEntity.kt
 * @Package: org.example.project.data.database.entity.feedline
 * @Description: 朋友圈动态帖子的 Room 本地数据库实体类与领域对象转换
 * @Author: 何聚敛
 * @Date: 2026-08-05
 */
package org.example.project.data.database.entity.feedline

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.example.project.domain.model.feedline.FeedLineComment
import org.example.project.domain.model.feedline.FeedLineMedia
import org.example.project.domain.model.feedline.FeedLinePost
import org.example.project.domain.model.feedline.FeedLineUser

@Entity(tableName = "feedline_posts")
data class FeedLinePostEntity(
    @PrimaryKey val id: String,
    val postUserJson: String,
    val content: String,
    val mediaListJson: String,
    val likedUsersJson: String,
    val commentsListJson: String,
    val isLiked: Boolean,
    val createTime: Long,
    val unreadNotificationCount: Int,
) {
    fun toDomainModel(): FeedLinePost {
        val json = Json { ignoreUnknownKeys = true; isLenient = true }
        return FeedLinePost(
            id = id,
            postUser = json.decodeFromString<FeedLineUser>(postUserJson),
            content = content,
            mediaList = json.decodeFromString<List<FeedLineMedia>>(mediaListJson),
            likedUsers = json.decodeFromString<List<FeedLineUser>>(likedUsersJson),
            commentsList = json.decodeFromString<List<FeedLineComment>>(commentsListJson),
            isLiked = isLiked,
            createTime = createTime,
            unreadNotificationCount = unreadNotificationCount,
        )
    }

    companion object {
        fun fromDomainModel(domain: FeedLinePost): FeedLinePostEntity {
            val json = Json { ignoreUnknownKeys = true; isLenient = true }
            return FeedLinePostEntity(
                id = domain.id,
                postUserJson = json.encodeToString(domain.postUser),
                content = domain.content,
                mediaListJson = json.encodeToString(domain.mediaList),
                likedUsersJson = json.encodeToString(domain.likedUsers),
                commentsListJson = json.encodeToString(domain.commentsList),
                isLiked = domain.isLiked,
                createTime = domain.createTime,
                unreadNotificationCount = domain.unreadNotificationCount,
            )
        }
    }
}
