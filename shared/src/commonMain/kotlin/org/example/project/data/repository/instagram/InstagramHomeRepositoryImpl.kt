/**
 * @File: InstagramHomeRepositoryImpl.kt
 * @Package: org.example.project.data.repository.instagram
 * @Description: Instagram首页数据仓库实现（结合Room KMP本地数据库持久化）
 * @Author: 何聚敛
 * @Date: 2026-08-18
 */
package org.example.project.data.repository.instagram

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
import org.example.project.data.database.dao.instagram.InstagramDao
import org.example.project.data.database.entity.instagram.InstagramPostEntity
import org.example.project.domain.model.instagram.InstagramComment
import org.example.project.domain.model.instagram.InstagramMedia
import org.example.project.domain.model.instagram.InstagramPost
import org.example.project.domain.model.instagram.ProfileUser
import org.example.project.domain.repository.instagram.InstagramHomeRepository
import org.example.project.platform.currentTimeMillis
import kotlin.random.Random

class InstagramHomeRepositoryImpl(
    private val instagramDao: InstagramDao,
    private val networkContainer: NetworkContainer? = null
) : InstagramHomeRepository {

    private val scope = CoroutineScope(Dispatchers.Default)
    private val postsFlow = MutableStateFlow<List<InstagramPost>>(emptyList())
    private val storiesFlow = MutableStateFlow<List<InstagramPost>>(emptyList())

    init {
        scope.launch {
            val existingPosts = instagramDao.observePosts().first()
            val existingStories = instagramDao.observeStories().first()
            if (existingPosts.isEmpty() && existingStories.isEmpty()) {
                val initialPosts = createFakeInstagramPosts()
                val initialStories = createFakeInstagramStories()
                postsFlow.value = initialPosts
                storiesFlow.value = initialStories
                instagramDao.insertPosts(
                    initialPosts.map { InstagramPostEntity.fromDomainModel(it, isStory = false) } +
                    initialStories.map { InstagramPostEntity.fromDomainModel(it, isStory = true) }
                )
            } else {
                postsFlow.value = existingPosts.map { it.toDomainModel() }
                storiesFlow.value = existingStories.map { it.toDomainModel() }
            }
        }
    }

    override fun getHomePosts(): Flow<List<InstagramPost>> {
        return instagramDao.observePosts().map { entities ->
            if (entities.isEmpty()) {
                val initialPosts = createFakeInstagramPosts()
                instagramDao.insertPosts(initialPosts.map { InstagramPostEntity.fromDomainModel(it, isStory = false) })
                initialPosts
            } else {
                entities.map { it.toDomainModel() }
            }
        }
    }

    override fun getStories(): Flow<List<InstagramPost>> {
        return instagramDao.observeStories().map { entities ->
            if (entities.isEmpty()) {
                val initialStories = createFakeInstagramStories()
                instagramDao.insertPosts(initialStories.map { InstagramPostEntity.fromDomainModel(it, isStory = true) })
                initialStories
            } else {
                entities.map { it.toDomainModel() }
            }
        }
    }

    override suspend fun refreshHome() {
        val container = networkContainer
        if (container != null) {
            runCatching {
                val remotePosts = container.authorizedClient
                    .get(ApiEndpoints.Instagram.HOME_FEED)
                    .body<List<InstagramPost>>()
                if (remotePosts.isNotEmpty()) {
                    postsFlow.value = remotePosts
                    instagramDao.insertPosts(remotePosts.map { InstagramPostEntity.fromDomainModel(it, isStory = false) })
                }
            }.onFailure {
                delay(600)
                var updatedPosts: List<InstagramPost> = emptyList()
                postsFlow.update { current ->
                    updatedPosts = current.map { post ->
                        if (Random.nextBoolean()) {
                            post.copy(unreadNotificationCount = (0..3).random())
                        } else post
                    }
                    updatedPosts
                }
                instagramDao.insertPosts(updatedPosts.map { InstagramPostEntity.fromDomainModel(it, isStory = false) })
            }
        } else {
            delay(600)
            var updatedPosts: List<InstagramPost> = emptyList()
            postsFlow.update { current ->
                updatedPosts = current.map { post ->
                    if (Random.nextBoolean()) {
                        post.copy(unreadNotificationCount = (0..3).random())
                    } else post
                }
                updatedPosts
            }
            instagramDao.insertPosts(updatedPosts.map { InstagramPostEntity.fromDomainModel(it, isStory = false) })
        }
    }

    override suspend fun likePost(postId: String, currentUser: ProfileUser) {
        var updatedPost: InstagramPost? = null
        postsFlow.update { current ->
            current.map { post ->
                if (post.id == postId) {
                    val newLikedUsers = if (post.likedUsers.none { it.userId == currentUser.userId }) {
                        post.likedUsers + currentUser
                    } else post.likedUsers
                    val updated = post.copy(isLiked = true, likedUsers = newLikedUsers)
                    updatedPost = updated
                    updated
                } else post
            }
        }
        updatedPost?.let {
            instagramDao.insertPosts(listOf(InstagramPostEntity.fromDomainModel(it, isStory = false)))
        }
    }

    override suspend fun unlikePost(postId: String, currentUser: ProfileUser) {
        var updatedPost: InstagramPost? = null
        postsFlow.update { current ->
            current.map { post ->
                if (post.id == postId) {
                    val newLikedUsers = post.likedUsers.filterNot { it.userId == currentUser.userId }
                    val updated = post.copy(isLiked = false, likedUsers = newLikedUsers)
                    updatedPost = updated
                    updated
                } else post
            }
        }
        updatedPost?.let {
            instagramDao.insertPosts(listOf(InstagramPostEntity.fromDomainModel(it, isStory = false)))
        }
    }

    override suspend fun savePost(postId: String) {
        var updatedPost: InstagramPost? = null
        postsFlow.update { current ->
            current.map { post ->
                if (post.id == postId) {
                    val updated = post.copy(isSaved = true)
                    updatedPost = updated
                    updated
                } else post
            }
        }
        updatedPost?.let {
            instagramDao.insertPosts(listOf(InstagramPostEntity.fromDomainModel(it, isStory = false)))
        }
    }

    override suspend fun unsavePost(postId: String) {
        var updatedPost: InstagramPost? = null
        postsFlow.update { current ->
            current.map { post ->
                if (post.id == postId) {
                    val updated = post.copy(isSaved = false)
                    updatedPost = updated
                    updated
                } else post
            }
        }
        updatedPost?.let {
            instagramDao.insertPosts(listOf(InstagramPostEntity.fromDomainModel(it, isStory = false)))
        }
    }

    override suspend fun addComment(postId: String, currentUser: ProfileUser, content: String) {
        val newComment = InstagramComment(
            id = generateUuid(),
            postId = postId,
            commentUser = currentUser,
            content = content,
            createTime = currentTimeMillis()
        )
        var updatedPost: InstagramPost? = null
        postsFlow.update { current ->
            current.map { post ->
                if (post.id == postId) {
                    val updated = post.copy(commentsList = post.commentsList + newComment)
                    updatedPost = updated
                    updated
                } else post
            }
        }
        updatedPost?.let {
            instagramDao.insertPosts(listOf(InstagramPostEntity.fromDomainModel(it, isStory = false)))
        }
    }

    override suspend fun deleteComment(postId: String, commentId: String) {
        var updatedPost: InstagramPost? = null
        postsFlow.update { current ->
            current.map { post ->
                if (post.id == postId) {
                    val updated = post.copy(commentsList = post.commentsList.filterNot { it.id == commentId })
                    updatedPost = updated
                    updated
                } else post
            }
        }
        updatedPost?.let {
            instagramDao.insertPosts(listOf(InstagramPostEntity.fromDomainModel(it, isStory = false)))
        }
    }

    override suspend fun deletePost(postId: String) {
        postsFlow.update { current -> current.filterNot { it.id == postId } }
        instagramDao.deletePost(postId)
    }

    override suspend fun createPost(
        user: ProfileUser,
        content: String,
        mediaList: List<InstagramMedia>,
        location: String?
    ) {
        val newPost = InstagramPost(
            id = generateUuid(),
            postUser = user,
            content = content,
            mediaList = mediaList,
            location = location,
            createTime = currentTimeMillis()
        )
        postsFlow.update { listOf(newPost) + it }
        instagramDao.insertPosts(listOf(InstagramPostEntity.fromDomainModel(newPost, isStory = false)))
    }
}

private fun generateUuid(): String = "id_" + Random.nextLong(100000, 999999)

fun createFakeInstagramStories(): List<InstagramPost> {
    val users = listOf(
        ProfileUser("u_me", "Your story", "https://picsum.photos/seed/me/200/200", "Creative Designer", "42", "1.2k", "340"),
        ProfileUser("u1", "alexa_art", "https://picsum.photos/seed/u1/200/200", "Digital Artist", "120", "15.4k", "420"),
        ProfileUser("u2", "travel_bug", "https://picsum.photos/seed/u2/200/200", "Exploring Earth 🌍", "350", "89.1k", "120"),
        ProfileUser("u3", "tech_insider", "https://picsum.photos/seed/u3/200/200", "AI & Dev News 🚀", "89", "5.6k", "89"),
        ProfileUser("u4", "gourmet_chef", "https://picsum.photos/seed/u4/200/200", "Foodie Vibes 🍜", "210", "42.8k", "210"),
        ProfileUser("u5", "fitness_pro", "https://picsum.photos/seed/u5/200/200", "Daily Motivation 💪", "430", "102k", "305"),
        ProfileUser("u6", "sunset_chaser", "https://picsum.photos/seed/u6/200/200", "Golden Hour Photos 🌅", "95", "12.3k", "88")
    )

    return listOf(
        InstagramPost(
            id = "story_0",
            postUser = users[0],
            content = "My Story Update",
            mediaList = listOf(InstagramMedia.Image("https://picsum.photos/seed/s0/600/1000")),
            unreadNotificationCount = 0
        ),
        InstagramPost(
            id = "story_1",
            postUser = users[1],
            content = "Live stream story!",
            mediaList = listOf(InstagramMedia.Image("https://picsum.photos/seed/s1/600/1000")),
            unreadNotificationCount = 1,
            audioTitle = "LIVE"
        ),
        InstagramPost(
            id = "story_2",
            postUser = users[2],
            content = "Travel story update",
            mediaList = listOf(InstagramMedia.Image("https://picsum.photos/seed/s2/600/1000")),
            unreadNotificationCount = 1
        ),
        InstagramPost(
            id = "story_3",
            postUser = users[3],
            content = "Tech story update",
            mediaList = listOf(InstagramMedia.Image("https://picsum.photos/seed/s3/600/1000")),
            unreadNotificationCount = 1
        ),
        InstagramPost(
            id = "story_4",
            postUser = users[4],
            content = "Food story update",
            mediaList = listOf(InstagramMedia.Image("https://picsum.photos/seed/s4/600/1000")),
            unreadNotificationCount = 0
        ),
        InstagramPost(
            id = "story_5",
            postUser = users[5],
            content = "Fitness story update",
            mediaList = listOf(InstagramMedia.Image("https://picsum.photos/seed/s5/600/1000")),
            unreadNotificationCount = 1
        ),
        InstagramPost(
            id = "story_6",
            postUser = users[6],
            content = "Sunset story update",
            mediaList = listOf(InstagramMedia.Image("https://picsum.photos/seed/s6/600/1000")),
            unreadNotificationCount = 0
        )
    )
}

fun createFakeInstagramPosts(): List<InstagramPost> {
    val uAlexa = ProfileUser("u1", "alexa_art", "https://picsum.photos/seed/u1/200/200", "Digital Artist", "120", "15.4k", "420")
    val uTravel = ProfileUser("u2", "travel_bug", "https://picsum.photos/seed/u2/200/200", "Exploring Earth 🌍", "350", "89.1k", "120")
    val uTech = ProfileUser("u3", "tech_insider", "https://picsum.photos/seed/u3/200/200", "AI & Dev News 🚀", "89", "5.6k", "89")
    val uChef = ProfileUser("u4", "gourmet_chef", "https://picsum.photos/seed/u4/200/200", "Foodie Vibes 🍜", "210", "42.8k", "210")
    val uMe = ProfileUser("u_me", "hejulian", "https://picsum.photos/seed/me/200/200", "Kotlin Developer", "18", "450", "320")

    val now = currentTimeMillis()

    return listOf(
        InstagramPost(
            id = "post_101",
            postUser = uTravel,
            content = "Lost in the magic of Kyoto 🌸 What's your dream travel destination for 2026? #Kyoto #JapanTravel #Wanderlust #CherryBlossom",
            location = "Kyoto, Japan",
            audioTitle = "Kyoto Sunset - Original Audio",
            mediaList = listOf(
                InstagramMedia.Image("https://picsum.photos/seed/kyoto1/1080/1080"),
                InstagramMedia.Image("https://picsum.photos/seed/kyoto2/1080/1080"),
                InstagramMedia.Image("https://picsum.photos/seed/kyoto3/1080/1080")
            ),
            likedUsers = listOf(uAlexa, uTech, uMe, uChef),
            commentsList = listOf(
                InstagramComment("c1", "post_101", uAlexa, "The colors in picture 2 are absolutely breathtaking! 😍"),
                InstagramComment("c2", "post_101", uTech, "Adding this to my bucket list right now 🔥")
            ),
            isLiked = true,
            isSaved = true,
            repostCount = 8702L,
            shareCount = 21000L,
            createTime = now - 2 * 3600 * 1000L
        ),
        InstagramPost(
            id = "post_102",
            postUser = uAlexa,
            content = "Just finished my latest 3D render artwork! Spent over 40 hours on lighting details. Swipe to check out the initial wireframe sketch ✨🎨",
            location = "San Francisco, CA",
            audioTitle = "Synthwave Chill - CyberArt",
            mediaList = listOf(
                InstagramMedia.Image("https://picsum.photos/seed/art1/1080/1080"),
                InstagramMedia.Image("https://picsum.photos/seed/art2/1080/1080")
            ),
            likedUsers = listOf(uTravel, uMe),
            commentsList = listOf(
                InstagramComment("c3", "post_102", uChef, "Masterpiece!! How did you achieve that volumetric fog effect?")
            ),
            isLiked = false,
            isSaved = false,
            repostCount = 41000L,
            shareCount = 33000L,
            createTime = now - 5 * 3600 * 1000L
        ),
        InstagramPost(
            id = "post_103",
            postUser = uChef,
            content = "Handmade Tonkotsu Ramen with 18-hour slow cooked broth 🍜 Fresh chashu pork belly and tamago egg. Perfection in a bowl!",
            location = "Tokyo, Japan",
            audioTitle = "Foodie Beats - Culinary Sound",
            mediaList = listOf(
                InstagramMedia.Image("https://picsum.photos/seed/ramen1/1080/1350")
            ),
            likedUsers = listOf(uTravel, uAlexa, uTech, uMe),
            commentsList = listOf(
                InstagramComment("c4", "post_103", uMe, "Looks so delicious! Save me a portion 🤤")
            ),
            isLiked = true,
            isSaved = false,
            createTime = now - 12 * 3600 * 1000L
        ),
        InstagramPost(
            id = "post_104",
            postUser = uTech,
            content = "Kotlin Multiplatform is revolutionizing cross-platform app engineering in 2026. Code once, run natively everywhere with Compose Multiplatform! 🚀📱💻",
            location = "Tech Hub, Silicon Valley",
            audioTitle = "Code & Lo-Fi Beats",
            mediaList = listOf(
                InstagramMedia.Image("https://picsum.photos/seed/kmp1/1080/1080"),
                InstagramMedia.Image("https://picsum.photos/seed/kmp2/1080/1080")
            ),
            likedUsers = listOf(uMe, uAlexa),
            commentsList = listOf(
                InstagramComment("c5", "post_104", uMe, "KMP is truly amazing for shared UI and architecture!")
            ),
            isLiked = false,
            isSaved = true,
            createTime = now - 24 * 3600 * 1000L
        )
    )
}
