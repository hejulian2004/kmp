package org.example.project.data

import org.example.project.BaseApplication
import org.example.project.data.database.LocalDatabaseHelper
import org.example.project.domain.model.FeedComment
import org.example.project.domain.model.FeedMedia
import org.example.project.domain.model.FeedNotification
import org.example.project.domain.model.FeedPost
import org.example.project.domain.model.FeedUser
import org.example.project.domain.repository.FeedRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.time.Duration.Companion.milliseconds

class FeedRepositoryImpl : FeedRepository {
    private val dbHelper = LocalDatabaseHelper(BaseApplication.context)

    private val _feedPosts = MutableStateFlow<List<FeedPost>>(emptyList())

    private val _feedNotifications = MutableStateFlow<List<FeedNotification>>(emptyList())

    init {
        _feedPosts.value = dbHelper.getAllPosts()
        _feedNotifications.value = dbHelper.getAllNotifications()
    }

    override fun getFeedPosts(): Flow<List<FeedPost>> {
        return _feedPosts.map { posts ->
            posts.sortedByDescending { it.createTime }
        }
    }

    override fun getFeedPost(postId: String): Flow<FeedPost?> {
        return _feedPosts.map { posts ->
            posts.find { it.id == postId }
        }
    }

    override suspend fun refreshFeed() {
        delay(1000.milliseconds)
        val networkPosts = try {
            org.example.project.data.network.RetrofitClient.service.getFeedPosts()
        } catch (e: Exception) {
            null
        }

        if (networkPosts != null) {
            dbHelper.deleteAllPosts()
            dbHelper.insertPosts(networkPosts)
            _feedPosts.value = networkPosts.sortedByDescending { it.createTime }
        } else {
            val localPosts = dbHelper.getAllPosts()
            if (localPosts.isNotEmpty()) {
                _feedPosts.value = localPosts
            } else {
                _feedPosts.value = createFakeData().sortedByDescending { it.createTime }
            }
        }
    }

    private suspend fun persistPostsToDb() {
        withContext(Dispatchers.IO) {
            dbHelper.deleteAllPosts()
            dbHelper.insertPosts(_feedPosts.value)
        }
    }

    override suspend fun likePost(
        postId: String,
        user: FeedUser
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
        persistPostsToDb()
        return "点赞成功"
    }

    override suspend fun getLikedUsers(postId: String): List<FeedUser> {
        val post = _feedPosts.value.find { it.id == postId }
        return post?.likedUsers ?: emptyList()
    }

    override suspend fun unlikePost(
        postId: String,
        user: FeedUser
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
        persistPostsToDb()
        return "取消点赞成功"
    }

    override suspend fun addComment(
        postId: String,
        commentUser: FeedUser,
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
        persistPostsToDb()
        return "评论发布成功"
    }

    override suspend fun getComments(postId: String): List<FeedComment> {
        return _feedPosts.value.find { it.id == postId }?.commentsList ?: emptyList()
    }

    override suspend fun deleteComment(comment: FeedComment): String {
        _feedPosts.update { posts ->
            posts.map { post ->
                if (post.id == comment.postId) {
                    post.copy(commentsList = post.commentsList.filter { it.id != comment.id })
                } else post
            }
        }
        persistPostsToDb()
        return "评论删除成功"
    }

    override suspend fun createPost(
        user: FeedUser,
        content: String,
        mediaList: List<FeedMedia>
    ) {
        val newPost = FeedPost(
            id = UUID.randomUUID().toString(),
            postUser = user,
            content = content,
            mediaList = mediaList,
        )
        _feedPosts.update { posts ->
            posts + newPost
        }
        persistPostsToDb()
    }

    override suspend fun deletePost(postId: String) {
        _feedPosts.update { posts ->
            posts.filterNot { it.id == postId }
        }
        _feedNotifications.update { notifications ->
            notifications.filterNot { it.post.id == postId }
        }
        persistPostsToDb()
        persistNotificationsToDb()
    }

    override suspend fun updatePost(
        postId: String,
        content: String,
        mediaList: List<FeedMedia>
    ) {
        _feedPosts.update { posts ->
            posts.map { post ->
                if (post.id == postId) {
                    post.copy(content = content, mediaList = mediaList)
                } else post
            }
        }
        persistPostsToDb()
    }

    private suspend fun persistNotificationsToDb() {
        withContext(Dispatchers.IO) {
            dbHelper.deleteAllNotifications()
            dbHelper.insertNotifications(_feedNotifications.value)
        }
    }

    override suspend fun addNotification(feedNotification: FeedNotification) {
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
        persistNotificationsToDb()
    }

    override suspend fun deleteCommentNotification(feedNotification: FeedNotification) {
        _feedNotifications.update { notifications ->
            notifications.map { notification ->
                if (notification.id == feedNotification.id) {
                    notification.copy(isDelete = true)
                } else notification
            }
        }
        persistNotificationsToDb()
    }

    override suspend fun deleteLikeNotification(feedNotification: FeedNotification) {
        _feedNotifications.update { notifications ->
            notifications.filterNot { it.id == feedNotification.id || (it.post.id == feedNotification.post.id && it.user.id == feedNotification.user.id && it.isLikeNotification) }
        }
        persistNotificationsToDb()
    }

    override fun getNotifications(): Flow<List<FeedNotification>> {
        return _feedNotifications.map { notifications ->
            notifications.sortedByDescending { it.createdTime }
        }
    }

    override suspend fun markAllNotificationsAsRead() {
        _feedNotifications.update { notifications ->
            notifications.map { it.copy(isRead = true) }
        }
        persistNotificationsToDb()
    }
}

fun createFakeData(): List<FeedPost> {
    val user = FeedUser(id = "1", name = "何聚敛1", avatarUrl = "https://i.pravatar.cc/300?t=" + System.currentTimeMillis())
    return listOf(
        createFakePost(user),
        createFakePost(user.copy(id = "2", name = "何聚敛2", avatarUrl = "https://i.pravatar.cc/300?t=1" + System.currentTimeMillis())),
        createFakePost(user.copy(id = "3", name = "何聚敛3", avatarUrl = "https://i.pravatar.cc/300?t=2" + System.currentTimeMillis())),
        createFakePost(user.copy(id = "4", name = "何聚敛4", avatarUrl = "https://i.pravatar.cc/300?t=3" + System.currentTimeMillis())),
        createFakePost(user.copy(id = "5", name = "何聚敛5", avatarUrl = "https://i.pravatar.cc/300?t=4" + System.currentTimeMillis())),
        createFakePost(user.copy(id = "6", name = "何聚敛6", avatarUrl = "https://i.pravatar.cc/300?t=5" + System.currentTimeMillis())),
    )
}

private fun createComment(postId: String, commentUser: FeedUser, content: String): FeedComment {
    return FeedComment(
        id = UUID.randomUUID().toString(),
        postId = postId,
        commentUser = commentUser,
        content = content
    )
}

fun createFakePost(user: FeedUser): FeedPost {
    val fakeLikedUser: List<FeedUser> = listOf(
        FeedUser(id = "11", name = "张三", avatarUrl = "https://i.pravatar.cc/300?img=1"),
        FeedUser(id = "22", name = "李四", avatarUrl = "https://i.pravatar.cc/300?img=2"),
        FeedUser(id = "33", name = "王五", avatarUrl = "https://i.pravatar.cc/300?img=3"),
        FeedUser(id = "44", name = "张三2", avatarUrl = "https://i.pravatar.cc/300?img=4"),
        FeedUser(id = "55", name = "李四2", avatarUrl = "https://i.pravatar.cc/300?img=5"),
        FeedUser(id = "66", name = "王五2", avatarUrl = "https://i.pravatar.cc/300?img=6"),
        FeedUser(id = "77", name = "张三3", avatarUrl = "https://i.pravatar.cc/300?img=7"),
        FeedUser(id = "88", name = "李四4", avatarUrl = "https://i.pravatar.cc/300?img=8"),
        FeedUser(id = "99", name = "王五5", avatarUrl = "https://i.pravatar.cc/300?img=9")
    )
    val postId = UUID.randomUUID().toString()
    return FeedPost(
        id = postId,
        postUser = user,
        content = "这是一个测试内容,这是一个测试内容,这是一个测试内容,这是一个测试内容,这是一个测试内容-----------------------------------------------\n---\n---\n---\n" + System.currentTimeMillis(),
        likedUsers = fakeLikedUser,
        commentsList = listOf(
            FeedComment(
                id = UUID.randomUUID().toString(),
                postId = postId,
                commentUser = FeedUser(id = "11", name = "张三", avatarUrl = "https://i.pravatar.cc/300?img=1"),
                content = "这个朋友圈写得不错"
            ),
            FeedComment(
                id = UUID.randomUUID().toString(),
                postId = postId,
                commentUser = FeedUser(id = "22", name = "李四", avatarUrl = "https://i.pravatar.cc/300?img=2"),
                content = "这个朋友圈写得不错，这个朋友圈写得不错"
            ),
            FeedComment(
                id = UUID.randomUUID().toString(),
                postId = postId,
                commentUser = FeedUser(id = "3", name = "王五", avatarUrl = "https://i.pravatar.cc/300?img=3"),
                content = "这个朋友圈写得不错，这个朋友圈写得不错，这个朋友圈写得不错"
            )
        )
    )
}
