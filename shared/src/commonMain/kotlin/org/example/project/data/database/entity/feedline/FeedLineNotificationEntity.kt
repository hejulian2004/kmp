/**
 * @File: FeedLineNotificationEntity.kt
 * @Package: org.example.project.data.database.entity.feedline
 * @Description: 朋友圈通知消息的Room本地数据库实体类
 * @Author: 何聚敛
 * @Date: 2026-08-05
 */
package org.example.project.data.database.entity.feedline

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.example.project.domain.model.feedline.FeedLineComment
import org.example.project.domain.model.feedline.FeedLineNotification
import org.example.project.domain.model.feedline.FeedLinePost
import org.example.project.domain.model.feedline.FeedLineUser

@Serializable
@Entity(tableName = "feedline_notifications")
data class FeedLineNotificationEntity(
    @PrimaryKey val id: String,
    val userJson: String,
    val postJson: String,
    val commentJson: String?,
    val isLikeNotification: Boolean,
    val isDelete: Boolean,
    val isRead: Boolean,
    val createdTime: Long,
) {
    fun toDomainModel(): FeedLineNotification {
        val json = Json { ignoreUnknownKeys = true; isLenient = true }
        return FeedLineNotification(
            id = id,
            user = json.decodeFromString<FeedLineUser>(userJson),
            post = json.decodeFromString<FeedLinePost>(postJson),
            comment = commentJson?.let { json.decodeFromString<FeedLineComment>(it) },
            isLikeNotification = isLikeNotification,
            isDelete = isDelete,
            isRead = isRead,
            createdTime = createdTime,
        )
    }

    companion object {
        fun fromDomainModel(domain: FeedLineNotification): FeedLineNotificationEntity {
            val json = Json { ignoreUnknownKeys = true; isLenient = true }
            return FeedLineNotificationEntity(
                id = domain.id,
                userJson = json.encodeToString(domain.user),
                postJson = json.encodeToString(domain.post),
                commentJson = domain.comment?.let { json.encodeToString(it) },
                isLikeNotification = domain.isLikeNotification,
                isDelete = domain.isDelete,
                isRead = domain.isRead,
                createdTime = domain.createdTime,
            )
        }
    }
}
