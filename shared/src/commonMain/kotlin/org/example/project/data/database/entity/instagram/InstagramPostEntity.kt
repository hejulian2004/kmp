/**
 * @File: InstagramPostEntity.kt
 * @Package: org.example.project.data.database.entity.instagram
 * @Description: Instagram动态与Story快拍的Room本地数据库实体
 * @Author: 何聚敛
 * @Date: 2026-08-05
 */
package org.example.project.data.database.entity.instagram

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.example.project.domain.model.instagram.InstagramComment
import org.example.project.domain.model.instagram.InstagramMedia
import org.example.project.domain.model.instagram.InstagramPost
import org.example.project.domain.model.instagram.ProfileUser

@Entity(tableName = "instagram_posts")
data class InstagramPostEntity(
    @PrimaryKey val id: String,
    val postUserJson: String,
    val content: String,
    val location: String?,
    val audioTitle: String?,
    val mediaListJson: String,
    val likedUsersJson: String,
    val commentsListJson: String,
    val isLiked: Boolean,
    val isSaved: Boolean,
    val repostCount: Long?,
    val shareCount: Long?,
    val createTime: Long,
    val unreadNotificationCount: Int,
    val isStory: Boolean,
) {
    fun toDomainModel(): InstagramPost {
        val json = Json { ignoreUnknownKeys = true; isLenient = true }
        return InstagramPost(
            id = id,
            postUser = json.decodeFromString<ProfileUser>(postUserJson),
            content = content,
            location = location,
            audioTitle = audioTitle,
            mediaList = json.decodeFromString<List<InstagramMedia>>(mediaListJson),
            likedUsers = json.decodeFromString<List<ProfileUser>>(likedUsersJson),
            commentsList = json.decodeFromString<List<InstagramComment>>(commentsListJson),
            isLiked = isLiked,
            isSaved = isSaved,
            repostCount = repostCount,
            shareCount = shareCount,
            createTime = createTime,
            unreadNotificationCount = unreadNotificationCount,
        )
    }

    companion object {
        fun fromDomainModel(domain: InstagramPost, isStory: Boolean = false): InstagramPostEntity {
            val json = Json { ignoreUnknownKeys = true; isLenient = true }
            return InstagramPostEntity(
                id = domain.id,
                postUserJson = json.encodeToString(domain.postUser),
                content = domain.content,
                location = domain.location,
                audioTitle = domain.audioTitle,
                mediaListJson = json.encodeToString(domain.mediaList),
                likedUsersJson = json.encodeToString(domain.likedUsers),
                commentsListJson = json.encodeToString(domain.commentsList),
                isLiked = domain.isLiked,
                isSaved = domain.isSaved,
                repostCount = domain.repostCount,
                shareCount = domain.shareCount,
                createTime = domain.createTime,
                unreadNotificationCount = domain.unreadNotificationCount,
                isStory = isStory,
            )
        }
    }
}
