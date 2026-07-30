/**
 * @File: FeedRepositoryImpl.kt
 * @Package: org.example.project.data.repository.feedline
 * @Description: 朋友圈数据仓库的具体内存与网络实现
 * @Author: 何聚敛
 * @Date: 2026-07-20
 */
package org.example.project.data.repository.feedline

import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import org.example.project.core.network.client.NetworkContainer
import org.example.project.core.network.config.ApiEndpoints
import org.example.project.core.network.config.createFakeFeedPosts
import org.example.project.domain.error.toAppError
import org.example.project.domain.model.feedline.FeedLineComment
import org.example.project.domain.model.feedline.FeedLineMedia
import org.example.project.domain.model.feedline.FeedLineNotification
import org.example.project.domain.model.feedline.FeedLinePost
import org.example.project.domain.model.feedline.FeedLineUser
import org.example.project.domain.repository.feedline.FeedLineRepository
import kotlin.math.abs
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
fun generateUUID(): String {
    return Uuid.random().toString()
}

class FeedRepositoryImpl(
    private val networkContainer: NetworkContainer? = null
) : FeedLineRepository {
    // 内存流状态结构
    private val _feedPosts = MutableStateFlow<List<FeedLinePost>>(emptyList())
    private val _feedNotifications = MutableStateFlow<List<FeedLineNotification>>(emptyList())

    init {
        _feedPosts.value = createFakeFeedPosts()
    }

    override fun getFeedPosts(): Flow<List<FeedLinePost>> {
        return _feedPosts.map { posts ->
            posts.sortedByDescending { it.createTime }
        }
    }

    override fun getFeedPost(postId: String): Flow<FeedLinePost?> {
        return _feedPosts.map { posts ->
            posts.find { it.id == postId }
        }
    }

    override suspend fun refreshFeed() {
        val container = networkContainer
        if (container != null) {
            runCatching {
                val remotePosts = container.authorizedClient
                    .get(ApiEndpoints.FeedLine.GET_POSTS)
                    .body<List<FeedLinePost>>()
                if (remotePosts.isNotEmpty()) {
                    _feedPosts.value = remotePosts.sortedByDescending { it.createTime }
                } else {
                    // 开发阶段接口无数据时，默认填充Mock假数据
                    _feedPosts.value = createFakeFeedPosts().sortedByDescending { it.createTime }
                }
            }.onFailure {
                // 开发阶段无真实后端服务时，默认正常返回假数据
                delay(500.milliseconds)
                _feedPosts.value = createFakeFeedPosts().sortedByDescending { it.createTime }
            }
        } else {
            delay(500.milliseconds)
            _feedPosts.value = createFakeFeedPosts().sortedByDescending { it.createTime }
        }
    }

    override suspend fun likePost(
        postId: String,
        user: FeedLineUser
    ): String {
        val post = _feedPosts.value.find { it.id == postId }
        if (post == null) {
            return "点赞失败，找不到该帖子"
        }
        _feedPosts.update { posts ->
            posts.map {
                if (it.id == postId) {
                    it.copy(
                        isLiked = true,
                        likedUsers = it.likedUsers + user
                    )
                } else it
            }
        }
        return "点赞成功"
    }

    override suspend fun getLikedUsers(postId: String): List<FeedLineUser> {
        val post = _feedPosts.value.find { it.id == postId }
        return post?.likedUsers ?: emptyList()
    }

    override suspend fun unlikePost(
        postId: String,
        user: FeedLineUser
    ): String {
        val post = _feedPosts.value.find { it.id == postId }
        if (post == null) {
            return "取消点赞失败，找不到该帖子"
        }
        _feedPosts.update { posts ->
            posts.map { p ->
                if (p.id == postId) {
                    p.copy(
                        isLiked = false,
                        likedUsers = p.likedUsers.filterNot { likedUser ->
                            likedUser.id == user.id
                        }
                    )
                } else p
            }
        }
        return "取消点赞成功"
    }

    override suspend fun addComment(
        postId: String,
        commentUser: FeedLineUser,
        content: String
    ): String {
        val newComment = FeedLineComment(
            id = generateUUID(),
            postId = postId,
            commentUser = commentUser,
            content = content
        )
        _feedPosts.update { posts ->
            posts.map {
                if (it.id == postId) {
                    it.copy(commentsList = it.commentsList + newComment)
                } else it
            }
        }
        return "评论发布成功"
    }

    override suspend fun getComments(postId: String): List<FeedLineComment> {
        return _feedPosts.value.find { it.id == postId }?.commentsList ?: emptyList()
    }

    override suspend fun deleteComment(comment: FeedLineComment): String {
        _feedPosts.update { posts ->
            posts.map { post ->
                if (post.id == comment.postId) {
                    post.copy(commentsList = post.commentsList.filter { it.id != comment.id })
                } else post
            }
        }
        return "评论删除成功"
    }

    override suspend fun createPost(
        user: FeedLineUser,
        content: String,
        mediaList: List<FeedLineMedia>
    ) {
        val newPost = FeedLinePost(
            id = generateUUID(),
            postUser = user,
            content = content,
            mediaList = mediaList,
        )
        _feedPosts.update { posts ->
            posts + newPost
        }
    }

    override suspend fun deletePost(postId: String) {
        _feedPosts.update { posts ->
            posts.filterNot { it.id == postId }
        }
        _feedNotifications.update { notifications ->
            notifications.filterNot { it.post.id == postId }
        }
    }

    override suspend fun updatePost(
        postId: String,
        content: String,
        mediaList: List<FeedLineMedia>
    ) {
        _feedPosts.update { posts ->
            posts.map { post ->
                if (post.id == postId) {
                    post.copy(content = content, mediaList = mediaList)
                } else post
            }
        }
    }

    override suspend fun addNotification(feedNotification: FeedLineNotification) {
        _feedNotifications.update { notifications ->
            val exists = notifications.any {
                it.id == feedNotification.id || (
                    feedNotification.isLikeNotification &&
                    it.isLikeNotification &&
                    !it.isDelete &&
                    it.post.id == feedNotification.post.id &&
                    it.user.id == feedNotification.user.id
                )
            }
            if (exists) notifications else notifications + feedNotification
        }
    }

    override suspend fun deleteCommentNotification(feedNotification: FeedLineNotification) {
        _feedNotifications.update { notifications ->
            notifications.map { notification ->
                if (notification.id == feedNotification.id) {
                    notification.copy(isDelete = true)
                } else notification
            }
        }
    }

    override suspend fun deleteLikeNotification(feedNotification: FeedLineNotification) {
        _feedNotifications.update { notifications ->
            notifications.filterNot { it.id == feedNotification.id || (it.post.id == feedNotification.post.id && it.user.id == feedNotification.user.id && it.isLikeNotification) }
        }
    }

    override fun getNotifications(): Flow<List<FeedLineNotification>> {
        return _feedNotifications.map { notifications ->
            notifications.sortedByDescending { it.createdTime }
        }
    }

    override suspend fun markAllNotificationsAsRead() {
        _feedNotifications.update { notifications ->
            notifications.map { it.copy(isRead = true) }
        }
    }
}


