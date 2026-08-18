/**
 * @File: FeedRepositoryImpl.kt
 * @Package: org.example.project.data.repository.feedline
 * @Description: 朋友圈数据仓库的具体内存与网络实现（整合Room KMP本地DAO数据库持久化）
 * @Author: 何聚敛
 * @Date: 2026-08-18
 */
package org.example.project.data.repository.feedline

import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.core.network.client.NetworkContainer
import org.example.project.core.network.config.ApiEndpoints
import org.example.project.core.network.config.createFakeFeedPosts
import org.example.project.data.database.dao.feedline.FeedLineDao
import org.example.project.data.database.dao.feedline.FeedLineDaoImpl
import org.example.project.data.database.entity.feedline.FeedLineNotificationEntity
import org.example.project.data.database.entity.feedline.FeedLinePostEntity
import org.example.project.domain.model.feedline.FeedLineComment
import org.example.project.domain.model.feedline.FeedLineMedia
import org.example.project.domain.model.feedline.FeedLineNotification
import org.example.project.domain.model.feedline.FeedLinePost
import org.example.project.domain.model.feedline.FeedLineUser
import org.example.project.domain.repository.feedline.FeedLineRepository
import kotlin.time.Duration.Companion.milliseconds
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
fun generateUUID(): String {
    return Uuid.random().toString()
}

class FeedRepositoryImpl(
    private val networkContainer: NetworkContainer? = null,
    private val feedLineDao: FeedLineDao = FeedLineDaoImpl(),
) : FeedLineRepository {

    private val scope = CoroutineScope(Dispatchers.Default)
    private val _feedPosts = MutableStateFlow<List<FeedLinePost>>(emptyList())
    private val _feedNotifications = MutableStateFlow<List<FeedLineNotification>>(emptyList())

    init {
        scope.launch {
            val dbEntities = feedLineDao.observePosts().first()
            if (dbEntities.isEmpty()) {
                val initialPosts = createFakeFeedPosts()
                _feedPosts.value = initialPosts
                feedLineDao.insertPosts(initialPosts.map { FeedLinePostEntity.fromDomainModel(it) })
            } else {
                _feedPosts.value = dbEntities.map { it.toDomainModel() }
            }
        }
    }

    override fun getFeedPosts(): Flow<List<FeedLinePost>> {
        return feedLineDao.observePosts().map { entities ->
            if (entities.isEmpty()) {
                _feedPosts.value.sortedByDescending { it.createTime }
            } else {
                entities.map { it.toDomainModel() }.sortedByDescending { it.createTime }
            }
        }
    }

    override fun getFeedPost(postId: String): Flow<FeedLinePost?> {
        return getFeedPosts().map { posts -> posts.find { it.id == postId } }
    }

    override suspend fun refreshFeed() {
        val container = networkContainer
        if (container != null) {
            runCatching {
                val remotePosts = container.authorizedClient
                    .get(ApiEndpoints.FeedLine.GET_POSTS)
                    .body<List<FeedLinePost>>()
                val targetPosts = if (remotePosts.isNotEmpty()) remotePosts else createFakeFeedPosts()
                val userCreatedPosts = _feedPosts.value.filterNot { post ->
                    targetPosts.any { it.id == post.id } || post.id.startsWith("feed_post_")
                }
                val mergedPosts = userCreatedPosts + targetPosts
                _feedPosts.value = mergedPosts
                feedLineDao.insertPosts(mergedPosts.map { FeedLinePostEntity.fromDomainModel(it) })
            }.onFailure {
                delay(500.milliseconds)
                val mockPosts = createFakeFeedPosts()
                val userCreatedPosts = _feedPosts.value.filterNot { post ->
                    mockPosts.any { it.id == post.id } || post.id.startsWith("feed_post_")
                }
                val mergedPosts = userCreatedPosts + mockPosts
                _feedPosts.value = mergedPosts
                feedLineDao.insertPosts(mergedPosts.map { FeedLinePostEntity.fromDomainModel(it) })
            }
        } else {
            delay(500.milliseconds)
            val mockPosts = createFakeFeedPosts()
            val userCreatedPosts = _feedPosts.value.filterNot { post ->
                mockPosts.any { it.id == post.id } || post.id.startsWith("feed_post_")
            }
            val mergedPosts = userCreatedPosts + mockPosts
            _feedPosts.value = mergedPosts
            feedLineDao.insertPosts(mergedPosts.map { FeedLinePostEntity.fromDomainModel(it) })
        }
    }

    override suspend fun likePost(postId: String, user: FeedLineUser): String {
        var updatedPost: FeedLinePost? = null
        _feedPosts.update { posts ->
            posts.map { post ->
                if (post.id == postId) {
                    val updated = post.copy(isLiked = true, likedUsers = post.likedUsers + user)
                    updatedPost = updated
                    updated
                } else post
            }
        }
        updatedPost?.let {
            feedLineDao.insertPosts(listOf(FeedLinePostEntity.fromDomainModel(it)))
        }
        return "点赞成功"
    }

    override suspend fun getLikedUsers(postId: String): List<FeedLineUser> {
        val post = _feedPosts.value.find { it.id == postId }
        return post?.likedUsers ?: emptyList()
    }

    override suspend fun unlikePost(postId: String, user: FeedLineUser): String {
        var updatedPost: FeedLinePost? = null
        _feedPosts.update { posts ->
            posts.map { post ->
                if (post.id == postId) {
                    val updated = post.copy(
                        isLiked = false,
                        likedUsers = post.likedUsers.filterNot { it.id == user.id }
                    )
                    updatedPost = updated
                    updated
                } else post
            }
        }
        updatedPost?.let {
            feedLineDao.insertPosts(listOf(FeedLinePostEntity.fromDomainModel(it)))
        }
        return "取消点赞成功"
    }

    override suspend fun addComment(postId: String, commentUser: FeedLineUser, content: String): String {
        val newComment = FeedLineComment(
            id = generateUUID(),
            postId = postId,
            commentUser = commentUser,
            content = content
        )
        var updatedPost: FeedLinePost? = null
        _feedPosts.update { posts ->
            posts.map { post ->
                if (post.id == postId) {
                    val updated = post.copy(commentsList = post.commentsList + newComment)
                    updatedPost = updated
                    updated
                } else post
            }
        }
        updatedPost?.let {
            feedLineDao.insertPosts(listOf(FeedLinePostEntity.fromDomainModel(it)))
        }
        return "评论发布成功"
    }

    override suspend fun getComments(postId: String): List<FeedLineComment> {
        return _feedPosts.value.find { it.id == postId }?.commentsList ?: emptyList()
    }

    override suspend fun deleteComment(comment: FeedLineComment): String {
        var updatedPost: FeedLinePost? = null
        _feedPosts.update { posts ->
            posts.map { post ->
                if (post.id == comment.postId) {
                    val updated = post.copy(commentsList = post.commentsList.filter { it.id != comment.id })
                    updatedPost = updated
                    updated
                } else post
            }
        }
        updatedPost?.let {
            feedLineDao.insertPosts(listOf(FeedLinePostEntity.fromDomainModel(it)))
        }
        return "评论删除成功"
    }

    override suspend fun createPost(user: FeedLineUser, content: String, mediaList: List<FeedLineMedia>) {
        val newPost = FeedLinePost(
            id = generateUUID(),
            postUser = user,
            content = content,
            mediaList = mediaList,
        )
        _feedPosts.update { posts -> posts + newPost }
        feedLineDao.insertPosts(listOf(FeedLinePostEntity.fromDomainModel(newPost)))
    }

    override suspend fun deletePost(postId: String) {
        _feedPosts.update { posts -> posts.filterNot { it.id == postId } }
        feedLineDao.deletePost(postId)
    }

    override suspend fun updatePost(postId: String, content: String, mediaList: List<FeedLineMedia>) {
        var updatedPost: FeedLinePost? = null
        _feedPosts.update { posts ->
            posts.map { post ->
                if (post.id == postId) {
                    val updated = post.copy(content = content, mediaList = mediaList)
                    updatedPost = updated
                    updated
                } else post
            }
        }
        updatedPost?.let {
            feedLineDao.insertPosts(listOf(FeedLinePostEntity.fromDomainModel(it)))
        }
    }

    override suspend fun addNotification(feedNotification: FeedLineNotification) {
        _feedNotifications.update { notifications ->
            val exists = notifications.any { it.id == feedNotification.id }
            if (exists) notifications else notifications + feedNotification
        }
        feedLineDao.insertNotifications(listOf(FeedLineNotificationEntity.fromDomainModel(feedNotification)))
    }

    override suspend fun deleteCommentNotification(feedNotification: FeedLineNotification) {
        var updatedNotification: FeedLineNotification? = null
        _feedNotifications.update { notifications ->
            notifications.map { notification ->
                if (notification.id == feedNotification.id) {
                    val updated = notification.copy(isDelete = true)
                    updatedNotification = updated
                    updated
                } else notification
            }
        }
        updatedNotification?.let {
            feedLineDao.insertNotifications(listOf(FeedLineNotificationEntity.fromDomainModel(it)))
        }
    }

    override suspend fun deleteLikeNotification(feedNotification: FeedLineNotification) {
        _feedNotifications.update { notifications ->
            notifications.filterNot { it.id == feedNotification.id }
        }
        val updated = feedNotification.copy(isDelete = true)
        feedLineDao.insertNotifications(listOf(FeedLineNotificationEntity.fromDomainModel(updated)))
    }

    override fun getNotifications(): Flow<List<FeedLineNotification>> {
        return feedLineDao.observeNotifications().map { entities ->
            if (entities.isEmpty()) {
                _feedNotifications.value.sortedByDescending { it.createdTime }
            } else {
                entities.map { it.toDomainModel() }.sortedByDescending { it.createdTime }
            }
        }
    }

    override suspend fun markAllNotificationsAsRead() {
        var readList: List<FeedLineNotification> = emptyList()
        _feedNotifications.update { notifications ->
            readList = notifications.map { it.copy(isRead = true) }
            readList
        }
        feedLineDao.insertNotifications(readList.map { FeedLineNotificationEntity.fromDomainModel(it) })
    }
}
