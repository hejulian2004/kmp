/**
 * @File: FeedRepositoryImpl.kt
 * @Package: org.example.project.data.repository.feedline
 * @Description: 朋友圈数据仓库的具体内存与网络实现
 * @Author: 何聚敛
 * @Date: 2026-07-20
 */
package org.example.project.data.repository.feedline

import org.example.project.domain.model.feedline.FeedLineComment
import org.example.project.domain.model.feedline.FeedLineMedia
import org.example.project.domain.model.feedline.FeedLineNotification
import org.example.project.domain.model.feedline.FeedLinePost
import org.example.project.domain.model.feedline.FeedLineUser
import org.example.project.domain.repository.feedline.FeedLineRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlin.math.abs
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

fun generateUUID(): String {
    return Random.nextLong().toString()
}

class FeedRepositoryImpl : FeedLineRepository {
    // In-memory data structures
    private val _feedPosts = MutableStateFlow<List<FeedLinePost>>(emptyList())
    private val _feedNotifications = MutableStateFlow<List<FeedLineNotification>>(emptyList())

    init {
        _feedPosts.value = createFakeData()
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
        delay(1000.milliseconds)
        // TODO: Replace with Ktor call
        // val networkPosts = KtorClient.service.getFeedPosts()
        
        // Mock network delay and return fake data for now
        _feedPosts.value = createFakeData().sortedByDescending { it.createTime }
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
        val newComment = createComment(
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

fun createFakeData(): List<FeedLinePost> {
    val user = FeedLineUser(id = "1", name = "何聚敛1", avatarUrl = "https://i.pravatar.cc/300?t=" + abs(Random.nextLong() % 1000))
    return listOf(
        createFakePost(user),
        createFakePost(user.copy(id = "2", name = "何聚敛2", avatarUrl = "https://i.pravatar.cc/300?t=" + abs(Random.nextLong() % 1000))),
        createFakePost(user.copy(id = "3", name = "何聚敛3", avatarUrl = "https://i.pravatar.cc/300?t=" + abs(Random.nextLong() % 1000))),
        createFakePost(user.copy(id = "4", name = "何聚敛4", avatarUrl = "https://i.pravatar.cc/300?t=" + abs(Random.nextLong() % 1000))),
        createFakePost(user.copy(id = "5", name = "何聚敛5", avatarUrl = "https://i.pravatar.cc/300?t=" + abs(Random.nextLong() % 1000))),
        createFakePost(user.copy(id = "6", name = "何聚敛6", avatarUrl = "https://i.pravatar.cc/300?t=" + abs(Random.nextLong() % 1000))),
    )
}

private fun createComment(postId: String, commentUser: FeedLineUser, content: String): FeedLineComment {
    return FeedLineComment(
        id = generateUUID(),
        postId = postId,
        commentUser = commentUser,
        content = content
    )
}

fun createFakePost(user: FeedLineUser): FeedLinePost {
    val fakeLikedUser: List<FeedLineUser> = listOf(
        FeedLineUser(id = "11", name = "张三", avatarUrl = "https://i.pravatar.cc/300?img=1"),
        FeedLineUser(id = "22", name = "李四", avatarUrl = "https://i.pravatar.cc/300?img=2"),
        FeedLineUser(id = "33", name = "王五", avatarUrl = "https://i.pravatar.cc/300?img=3"),
        FeedLineUser(id = "44", name = "张三2", avatarUrl = "https://i.pravatar.cc/300?img=4"),
        FeedLineUser(id = "55", name = "李四2", avatarUrl = "https://i.pravatar.cc/300?img=5"),
        FeedLineUser(id = "66", name = "王五2", avatarUrl = "https://i.pravatar.cc/300?img=6"),
        FeedLineUser(id = "77", name = "张三3", avatarUrl = "https://i.pravatar.cc/300?img=7"),
        FeedLineUser(id = "88", name = "李四4", avatarUrl = "https://i.pravatar.cc/300?img=8"),
        FeedLineUser(id = "99", name = "王五5", avatarUrl = "https://i.pravatar.cc/300?img=9")
    )
    val postId = generateUUID()
    return FeedLinePost(
        id = postId,
        postUser = user,
        content = "这是一个测试内容,这是一个测试内容,这是一个测试内容,这是一个测试内容,这是一个测试内容-----------------------------------------------\n---\n---\n---\n" + abs(Random.nextLong() % 1000),
        likedUsers = fakeLikedUser,
        commentsList = listOf(
            FeedLineComment(
                id = generateUUID(),
                postId = postId,
                commentUser = FeedLineUser(id = "11", name = "张三", avatarUrl = "https://i.pravatar.cc/300?img=1"),
                content = "这个朋友圈写得不错"
            ),
            FeedLineComment(
                id = generateUUID(),
                postId = postId,
                commentUser = FeedLineUser(id = "22", name = "李四", avatarUrl = "https://i.pravatar.cc/300?img=2"),
                content = "这个朋友圈写得不错，这个朋友圈写得不错"
            ),
            FeedLineComment(
                id = generateUUID(),
                postId = postId,
                commentUser = FeedLineUser(id = "3", name = "王五", avatarUrl = "https://i.pravatar.cc/300?img=3"),
                content = "这个朋友圈写得不错，这个朋友圈写得不错，这个朋友圈写得不错"
            )
        )
    )
}


