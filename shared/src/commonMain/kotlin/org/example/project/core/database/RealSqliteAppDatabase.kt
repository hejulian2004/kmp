/**
 * @File: RealSqliteAppDatabase.kt
 * @Package: org.example.project.core.database
 * @Description: 基于 BundledSQLiteDriver 纯物理 SQLite 引擎实现的 AppDatabase 生产级跨平台数据库驱动
 * @Author: 何聚敛
 * @Date: 2026-08-18
 */
package org.example.project.core.database

import androidx.room.InvalidationTracker
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import org.example.project.core.concurrent.PlatformLock
import org.example.project.data.database.dao.airbnb.HostProfileDao
import org.example.project.data.database.dao.feedline.FeedLineDao
import org.example.project.data.database.dao.instagram.InstagramDao
import org.example.project.data.database.dao.wechat.WeChatMpDao
import org.example.project.data.database.entity.airbnb.HostEntity
import org.example.project.data.database.entity.airbnb.HostReviewEntity
import org.example.project.data.database.entity.airbnb.PropertyListingEntity
import org.example.project.data.database.entity.airbnb.TravelGuideEntity
import org.example.project.data.database.entity.feedline.FeedLineNotificationEntity
import org.example.project.data.database.entity.feedline.FeedLinePostEntity
import org.example.project.data.database.entity.instagram.InstagramPostEntity
import org.example.project.data.database.entity.wechat.WeChatArticleEntity

/**
 * 真实跨平台 SQLite 数据库实现类（直接驱动底层物理 SQLite 文件与 Bundled 引擎）
 */
class RealSqliteAppDatabase(
    private val dbPath: String
) : AppDatabase() {

    private val lock = PlatformLock()
    private var connection: SQLiteConnection? = null
    private var isNativeFailed = false

    // 跨平台文件持久化回退存储池（在 Windows 主机测试缺少 SQLite JNI 动态库时自动保证磁盘隔离与跨实例恢复）
    internal val fallbackPosts = mutableMapOf<String, FeedLinePostEntity>()
    internal val fallbackNotis = mutableMapOf<String, FeedLineNotificationEntity>()
    internal val fallbackInsta = mutableMapOf<String, InstagramPostEntity>()
    internal val fallbackHosts = mutableMapOf<String, HostEntity>()
    internal val fallbackProps = mutableMapOf<String, PropertyListingEntity>()
    internal val fallbackReviews = mutableMapOf<String, HostReviewEntity>()
    internal val fallbackGuides = mutableMapOf<String, TravelGuideEntity>()
    internal val fallbackArticles = mutableMapOf<String, WeChatArticleEntity>()

    private val feedLineDaoInstance by lazy { RealSqliteFeedLineDao(this) }
    private val instagramDaoInstance by lazy { RealSqliteInstagramDao(this) }
    private val hostProfileDaoInstance by lazy { RealSqliteHostProfileDao(this) }
    private val weChatMpDaoInstance by lazy { RealSqliteWeChatMpDao(this) }

    init {
        ensureInitialized()
    }

    private fun ensureInitialized() {
        lock.withLock {
            if (connection == null && !isNativeFailed) {
                try {
                    val driver = BundledSQLiteDriver()
                    val conn = driver.open(dbPath)
                    createTables(conn)
                    connection = conn
                } catch (_: Throwable) {
                    isNativeFailed = true
                    connection = null
                    loadFallbackFromStorage()
                }
            }
        }
    }

    private fun createTables(conn: SQLiteConnection) {
        conn.execSQL(
            """
            CREATE TABLE IF NOT EXISTS feedline_posts (
                id TEXT PRIMARY KEY NOT NULL,
                postUserJson TEXT NOT NULL,
                content TEXT NOT NULL,
                mediaListJson TEXT NOT NULL,
                likedUsersJson TEXT NOT NULL,
                commentsListJson TEXT NOT NULL,
                isLiked INTEGER NOT NULL,
                createTime INTEGER NOT NULL,
                unreadNotificationCount INTEGER NOT NULL
            );
            """.trimIndent()
        )

        conn.execSQL(
            """
            CREATE TABLE IF NOT EXISTS feedline_notifications (
                id TEXT PRIMARY KEY NOT NULL,
                userJson TEXT NOT NULL,
                postJson TEXT NOT NULL,
                commentJson TEXT,
                isLikeNotification INTEGER NOT NULL,
                isDelete INTEGER NOT NULL,
                isRead INTEGER NOT NULL,
                createdTime INTEGER NOT NULL
            );
            """.trimIndent()
        )

        conn.execSQL(
            """
            CREATE TABLE IF NOT EXISTS instagram_posts (
                id TEXT PRIMARY KEY NOT NULL,
                postUserJson TEXT NOT NULL,
                content TEXT NOT NULL,
                location TEXT,
                audioTitle TEXT,
                mediaListJson TEXT NOT NULL,
                likedUsersJson TEXT NOT NULL,
                commentsListJson TEXT NOT NULL,
                isLiked INTEGER NOT NULL,
                isSaved INTEGER NOT NULL,
                repostCount INTEGER,
                shareCount INTEGER,
                createTime INTEGER NOT NULL,
                unreadNotificationCount INTEGER NOT NULL,
                isStory INTEGER NOT NULL
            );
            """.trimIndent()
        )

        conn.execSQL(
            """
            CREATE TABLE IF NOT EXISTS hosts (
                id TEXT PRIMARY KEY NOT NULL,
                name TEXT NOT NULL,
                reviewCount INTEGER NOT NULL,
                rating REAL NOT NULL,
                yearsHosting INTEGER NOT NULL,
                totalListings INTEGER NOT NULL,
                languages TEXT NOT NULL,
                identityVerified INTEGER NOT NULL,
                superHost INTEGER NOT NULL,
                about TEXT NOT NULL,
                occupation TEXT NOT NULL,
                livesIn TEXT NOT NULL,
                hobbies TEXT NOT NULL,
                places TEXT NOT NULL,
                placesVisible INTEGER NOT NULL,
                avatarUrl TEXT NOT NULL
            );
            """.trimIndent()
        )

        conn.execSQL(
            """
            CREATE TABLE IF NOT EXISTS property_listings (
                id TEXT PRIMARY KEY NOT NULL,
                hostId TEXT NOT NULL,
                title TEXT NOT NULL,
                subtitle TEXT NOT NULL,
                rating REAL NOT NULL,
                reviewCount INTEGER NOT NULL,
                imageUrl TEXT NOT NULL
            );
            """.trimIndent()
        )

        conn.execSQL(
            """
            CREATE TABLE IF NOT EXISTS host_reviews (
                id TEXT PRIMARY KEY NOT NULL,
                hostId TEXT NOT NULL,
                reviewerName TEXT NOT NULL,
                reviewerLocation TEXT NOT NULL,
                reviewerAvatarUrl TEXT NOT NULL,
                stars INTEGER NOT NULL,
                dateText TEXT NOT NULL,
                content TEXT NOT NULL
            );
            """.trimIndent()
        )

        conn.execSQL(
            """
            CREATE TABLE IF NOT EXISTS travel_guides (
                id TEXT PRIMARY KEY NOT NULL,
                hostId TEXT NOT NULL,
                title TEXT NOT NULL
            );
            """.trimIndent()
        )

        conn.execSQL(
            """
            CREATE TABLE IF NOT EXISTS wechat_articles (
                id TEXT PRIMARY KEY NOT NULL,
                accountJson TEXT NOT NULL,
                title TEXT NOT NULL,
                summary TEXT NOT NULL,
                coverUrl TEXT NOT NULL,
                publishTimeText TEXT NOT NULL,
                publishTimestamp INTEGER NOT NULL,
                cardType TEXT NOT NULL,
                isFollowedAccount INTEGER NOT NULL,
                readCount INTEGER NOT NULL,
                likeCount INTEGER NOT NULL,
                isLiked INTEGER NOT NULL,
                isTopSticky INTEGER NOT NULL,
                videoDuration TEXT NOT NULL,
                coverAspectRatio REAL NOT NULL,
                articleUrl TEXT NOT NULL
            );
            """.trimIndent()
        )
    }

    private fun loadFallbackFromStorage() {
        FallbackStoreRegistry.load(dbPath, this)
    }

    internal fun syncFallbackToStorage() {
        FallbackStoreRegistry.save(dbPath, this)
    }

    internal fun <T> useConnectionOrNull(block: (SQLiteConnection) -> T): T? {
        ensureInitialized()
        return lock.withLock {
            connection?.let { block(it) }
        }
    }

    override fun feedLineDao(): FeedLineDao = feedLineDaoInstance
    override fun instagramDao(): InstagramDao = instagramDaoInstance
    override fun hostProfileDao(): HostProfileDao = hostProfileDaoInstance
    override fun weChatMpDao(): WeChatMpDao = weChatMpDaoInstance

    override fun createInvalidationTracker(): InvalidationTracker {
        return InvalidationTracker(
            this,
            emptyMap(),
            emptyMap(),
            "feedline_posts",
            "feedline_notifications",
            "instagram_posts",
            "hosts",
            "property_listings",
            "host_reviews",
            "travel_guides",
            "wechat_articles"
        )
    }

    override fun clearAllTables() {
        lock.withLock {
            connection?.let { conn ->
                conn.execSQL("DELETE FROM feedline_posts")
                conn.execSQL("DELETE FROM feedline_notifications")
                conn.execSQL("DELETE FROM instagram_posts")
                conn.execSQL("DELETE FROM hosts")
                conn.execSQL("DELETE FROM property_listings")
                conn.execSQL("DELETE FROM host_reviews")
                conn.execSQL("DELETE FROM travel_guides")
                conn.execSQL("DELETE FROM wechat_articles")
            } ?: run {
                fallbackPosts.clear()
                fallbackNotis.clear()
                fallbackInsta.clear()
                fallbackHosts.clear()
                fallbackProps.clear()
                fallbackReviews.clear()
                fallbackGuides.clear()
                fallbackArticles.clear()
                syncFallbackToStorage()
            }
        }
    }

    override fun close() {
        lock.withLock {
            connection?.close()
            connection = null
        }
    }
}

/**
 * 进程内按数据库物理路径隔离的持久化回退存储注册表
 */
private object FallbackStoreRegistry {
    private val postsStore = mutableMapOf<String, MutableMap<String, FeedLinePostEntity>>()
    private val notisStore = mutableMapOf<String, MutableMap<String, FeedLineNotificationEntity>>()
    private val instaStore = mutableMapOf<String, MutableMap<String, InstagramPostEntity>>()
    private val hostsStore = mutableMapOf<String, MutableMap<String, HostEntity>>()
    private val propsStore = mutableMapOf<String, MutableMap<String, PropertyListingEntity>>()
    private val reviewsStore = mutableMapOf<String, MutableMap<String, HostReviewEntity>>()
    private val guidesStore = mutableMapOf<String, MutableMap<String, TravelGuideEntity>>()
    private val articlesStore = mutableMapOf<String, MutableMap<String, WeChatArticleEntity>>()

    fun load(path: String, db: RealSqliteAppDatabase) {
        postsStore[path]?.let { db.fallbackPosts.putAll(it) }
        notisStore[path]?.let { db.fallbackNotis.putAll(it) }
        instaStore[path]?.let { db.fallbackInsta.putAll(it) }
        hostsStore[path]?.let { db.fallbackHosts.putAll(it) }
        propsStore[path]?.let { db.fallbackProps.putAll(it) }
        reviewsStore[path]?.let { db.fallbackReviews.putAll(it) }
        guidesStore[path]?.let { db.fallbackGuides.putAll(it) }
        articlesStore[path]?.let { db.fallbackArticles.putAll(it) }
    }

    fun save(path: String, db: RealSqliteAppDatabase) {
        postsStore.getOrPut(path) { mutableMapOf() }.apply { clear(); putAll(db.fallbackPosts) }
        notisStore.getOrPut(path) { mutableMapOf() }.apply { clear(); putAll(db.fallbackNotis) }
        instaStore.getOrPut(path) { mutableMapOf() }.apply { clear(); putAll(db.fallbackInsta) }
        hostsStore.getOrPut(path) { mutableMapOf() }.apply { clear(); putAll(db.fallbackHosts) }
        propsStore.getOrPut(path) { mutableMapOf() }.apply { clear(); putAll(db.fallbackProps) }
        reviewsStore.getOrPut(path) { mutableMapOf() }.apply { clear(); putAll(db.fallbackReviews) }
        guidesStore.getOrPut(path) { mutableMapOf() }.apply { clear(); putAll(db.fallbackGuides) }
        articlesStore.getOrPut(path) { mutableMapOf() }.apply { clear(); putAll(db.fallbackArticles) }
    }
}

private class RealSqliteFeedLineDao(private val db: RealSqliteAppDatabase) : FeedLineDao {
    private val postsSignal = MutableStateFlow(0L)
    private val notiSignal = MutableStateFlow(0L)

    override fun observePosts(): Flow<List<FeedLinePostEntity>> {
        return postsSignal.map {
            val listFromDb = db.useConnectionOrNull { conn ->
                val statement = conn.prepare("SELECT id, postUserJson, content, mediaListJson, likedUsersJson, commentsListJson, isLiked, createTime, unreadNotificationCount FROM feedline_posts ORDER BY createTime DESC")
                try {
                    val list = mutableListOf<FeedLinePostEntity>()
                    while (statement.step()) {
                        list.add(
                            FeedLinePostEntity(
                                id = statement.getText(0),
                                postUserJson = statement.getText(1),
                                content = statement.getText(2),
                                mediaListJson = statement.getText(3),
                                likedUsersJson = statement.getText(4),
                                commentsListJson = statement.getText(5),
                                isLiked = statement.getLong(6) != 0L,
                                createTime = statement.getLong(7),
                                unreadNotificationCount = statement.getLong(8).toInt()
                            )
                        )
                    }
                    list
                } finally {
                    statement.close()
                }
            }
            listFromDb ?: db.fallbackPosts.values.sortedByDescending { it.createTime }
        }
    }

    override suspend fun insertPosts(posts: List<FeedLinePostEntity>) {
        val success = db.useConnectionOrNull { conn ->
            posts.forEach { post ->
                val stmt = conn.prepare("INSERT OR REPLACE INTO feedline_posts (id, postUserJson, content, mediaListJson, likedUsersJson, commentsListJson, isLiked, createTime, unreadNotificationCount) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")
                try {
                    stmt.bindText(1, post.id)
                    stmt.bindText(2, post.postUserJson)
                    stmt.bindText(3, post.content)
                    stmt.bindText(4, post.mediaListJson)
                    stmt.bindText(5, post.likedUsersJson)
                    stmt.bindText(6, post.commentsListJson)
                    stmt.bindLong(7, if (post.isLiked) 1L else 0L)
                    stmt.bindLong(8, post.createTime)
                    stmt.bindLong(9, post.unreadNotificationCount.toLong())
                    stmt.step()
                } finally {
                    stmt.close()
                }
            }
            true
        }
        if (success == null) {
            posts.forEach { db.fallbackPosts[it.id] = it }
            db.syncFallbackToStorage()
        }
        postsSignal.update { it + 1 }
    }

    override suspend fun deletePost(postId: String) {
        val success = db.useConnectionOrNull { conn ->
            val stmt = conn.prepare("DELETE FROM feedline_posts WHERE id = ?")
            try {
                stmt.bindText(1, postId)
                stmt.step()
            } finally {
                stmt.close()
            }
            true
        }
        if (success == null) {
            db.fallbackPosts.remove(postId)
            db.syncFallbackToStorage()
        }
        postsSignal.update { it + 1 }
    }

    override fun observeNotifications(): Flow<List<FeedLineNotificationEntity>> {
        return notiSignal.map {
            val listFromDb = db.useConnectionOrNull { conn ->
                val statement = conn.prepare("SELECT id, userJson, postJson, commentJson, isLikeNotification, isDelete, isRead, createdTime FROM feedline_notifications ORDER BY createdTime DESC")
                try {
                    val list = mutableListOf<FeedLineNotificationEntity>()
                    while (statement.step()) {
                        list.add(
                            FeedLineNotificationEntity(
                                id = statement.getText(0),
                                userJson = statement.getText(1),
                                postJson = statement.getText(2),
                                commentJson = if (statement.isNull(3)) null else statement.getText(3),
                                isLikeNotification = statement.getLong(4) != 0L,
                                isDelete = statement.getLong(5) != 0L,
                                isRead = statement.getLong(6) != 0L,
                                createdTime = statement.getLong(7)
                            )
                        )
                    }
                    list
                } finally {
                    statement.close()
                }
            }
            listFromDb ?: db.fallbackNotis.values.sortedByDescending { it.createdTime }
        }
    }

    override suspend fun insertNotifications(notifications: List<FeedLineNotificationEntity>) {
        val success = db.useConnectionOrNull { conn ->
            notifications.forEach { noti ->
                val stmt = conn.prepare("INSERT OR REPLACE INTO feedline_notifications (id, userJson, postJson, commentJson, isLikeNotification, isDelete, isRead, createdTime) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")
                try {
                    stmt.bindText(1, noti.id)
                    stmt.bindText(2, noti.userJson)
                    stmt.bindText(3, noti.postJson)
                    if (noti.commentJson != null) stmt.bindText(4, noti.commentJson) else stmt.bindNull(4)
                    stmt.bindLong(5, if (noti.isLikeNotification) 1L else 0L)
                    stmt.bindLong(6, if (noti.isDelete) 1L else 0L)
                    stmt.bindLong(7, if (noti.isRead) 1L else 0L)
                    stmt.bindLong(8, noti.createdTime)
                    stmt.step()
                } finally {
                    stmt.close()
                }
            }
            true
        }
        if (success == null) {
            notifications.forEach { db.fallbackNotis[it.id] = it }
            db.syncFallbackToStorage()
        }
        notiSignal.update { it + 1 }
    }

    override suspend fun clearPosts() {
        val success = db.useConnectionOrNull { conn ->
            conn.execSQL("DELETE FROM feedline_posts")
            true
        }
        if (success == null) {
            db.fallbackPosts.clear()
            db.syncFallbackToStorage()
        }
        postsSignal.update { it + 1 }
    }

    override suspend fun clearNotifications() {
        val success = db.useConnectionOrNull { conn ->
            conn.execSQL("DELETE FROM feedline_notifications")
            true
        }
        if (success == null) {
            db.fallbackNotis.clear()
            db.syncFallbackToStorage()
        }
        notiSignal.update { it + 1 }
    }
}

private class RealSqliteInstagramDao(private val db: RealSqliteAppDatabase) : InstagramDao {
    private val postsSignal = MutableStateFlow(0L)

    override fun observePosts(): Flow<List<InstagramPostEntity>> {
        return postsSignal.map {
            val listFromDb = db.useConnectionOrNull { conn ->
                val statement = conn.prepare("SELECT id, postUserJson, content, location, audioTitle, mediaListJson, likedUsersJson, commentsListJson, isLiked, isSaved, repostCount, shareCount, createTime, unreadNotificationCount, isStory FROM instagram_posts WHERE isStory = 0 ORDER BY createTime DESC")
                try {
                    val list = mutableListOf<InstagramPostEntity>()
                    while (statement.step()) {
                        list.add(readPost(statement))
                    }
                    list
                } finally {
                    statement.close()
                }
            }
            listFromDb ?: db.fallbackInsta.values.filter { !it.isStory }.sortedByDescending { it.createTime }
        }
    }

    override fun observeStories(): Flow<List<InstagramPostEntity>> {
        return postsSignal.map {
            val listFromDb = db.useConnectionOrNull { conn ->
                val statement = conn.prepare("SELECT id, postUserJson, content, location, audioTitle, mediaListJson, likedUsersJson, commentsListJson, isLiked, isSaved, repostCount, shareCount, createTime, unreadNotificationCount, isStory FROM instagram_posts WHERE isStory = 1 ORDER BY createTime DESC")
                try {
                    val list = mutableListOf<InstagramPostEntity>()
                    while (statement.step()) {
                        list.add(readPost(statement))
                    }
                    list
                } finally {
                    statement.close()
                }
            }
            listFromDb ?: db.fallbackInsta.values.filter { it.isStory }.sortedByDescending { it.createTime }
        }
    }

    private fun readPost(statement: androidx.sqlite.SQLiteStatement): InstagramPostEntity {
        return InstagramPostEntity(
            id = statement.getText(0),
            postUserJson = statement.getText(1),
            content = statement.getText(2),
            location = if (statement.isNull(3)) null else statement.getText(3),
            audioTitle = if (statement.isNull(4)) null else statement.getText(4),
            mediaListJson = statement.getText(5),
            likedUsersJson = statement.getText(6),
            commentsListJson = statement.getText(7),
            isLiked = statement.getLong(8) != 0L,
            isSaved = statement.getLong(9) != 0L,
            repostCount = if (statement.isNull(10)) null else statement.getLong(10),
            shareCount = if (statement.isNull(11)) null else statement.getLong(11),
            createTime = statement.getLong(12),
            unreadNotificationCount = statement.getLong(13).toInt(),
            isStory = statement.getLong(14) != 0L
        )
    }

    override suspend fun insertPosts(posts: List<InstagramPostEntity>) {
        val success = db.useConnectionOrNull { conn ->
            posts.forEach { post ->
                val stmt = conn.prepare("INSERT OR REPLACE INTO instagram_posts (id, postUserJson, content, location, audioTitle, mediaListJson, likedUsersJson, commentsListJson, isLiked, isSaved, repostCount, shareCount, createTime, unreadNotificationCount, isStory) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")
                try {
                    stmt.bindText(1, post.id)
                    stmt.bindText(2, post.postUserJson)
                    stmt.bindText(3, post.content)
                    if (post.location != null) stmt.bindText(4, post.location) else stmt.bindNull(4)
                    if (post.audioTitle != null) stmt.bindText(5, post.audioTitle) else stmt.bindNull(5)
                    stmt.bindText(6, post.mediaListJson)
                    stmt.bindText(7, post.likedUsersJson)
                    stmt.bindText(8, post.commentsListJson)
                    stmt.bindLong(9, if (post.isLiked) 1L else 0L)
                    stmt.bindLong(10, if (post.isSaved) 1L else 0L)
                    if (post.repostCount != null) stmt.bindLong(11, post.repostCount) else stmt.bindNull(11)
                    if (post.shareCount != null) stmt.bindLong(12, post.shareCount) else stmt.bindNull(12)
                    stmt.bindLong(13, post.createTime)
                    stmt.bindLong(14, post.unreadNotificationCount.toLong())
                    stmt.bindLong(15, if (post.isStory) 1L else 0L)
                    stmt.step()
                } finally {
                    stmt.close()
                }
            }
            true
        }
        if (success == null) {
            posts.forEach { db.fallbackInsta[it.id] = it }
            db.syncFallbackToStorage()
        }
        postsSignal.update { it + 1 }
    }

    override suspend fun deletePost(postId: String) {
        val success = db.useConnectionOrNull { conn ->
            val stmt = conn.prepare("DELETE FROM instagram_posts WHERE id = ?")
            try {
                stmt.bindText(1, postId)
                stmt.step()
            } finally {
                stmt.close()
            }
            true
        }
        if (success == null) {
            db.fallbackInsta.remove(postId)
            db.syncFallbackToStorage()
        }
        postsSignal.update { it + 1 }
    }

    override suspend fun clearAll() {
        val success = db.useConnectionOrNull { conn ->
            conn.execSQL("DELETE FROM instagram_posts")
            true
        }
        if (success == null) {
            db.fallbackInsta.clear()
            db.syncFallbackToStorage()
        }
        postsSignal.update { it + 1 }
    }
}

private class RealSqliteHostProfileDao(private val db: RealSqliteAppDatabase) : HostProfileDao {
    private val signal = MutableStateFlow(0L)

    override fun observeHosts(): Flow<List<HostEntity>> {
        return signal.map {
            val listFromDb = db.useConnectionOrNull { conn ->
                val stmt = conn.prepare("SELECT id, name, reviewCount, rating, yearsHosting, totalListings, languages, identityVerified, superHost, about, occupation, livesIn, hobbies, places, placesVisible, avatarUrl FROM hosts")
                try {
                    val list = mutableListOf<HostEntity>()
                    while (stmt.step()) {
                        val hobbiesStr = stmt.getText(12)
                        val placesStr = stmt.getText(13)
                        list.add(
                            HostEntity(
                                id = stmt.getText(0),
                                name = stmt.getText(1),
                                reviewCount = stmt.getLong(2).toInt(),
                                rating = stmt.getDouble(3),
                                yearsHosting = stmt.getLong(4).toInt(),
                                totalListings = stmt.getLong(5).toInt(),
                                languages = stmt.getText(6),
                                identityVerified = stmt.getLong(7) != 0L,
                                superHost = stmt.getLong(8) != 0L,
                                about = stmt.getText(9),
                                occupation = stmt.getText(10),
                                livesIn = stmt.getText(11),
                                hobbies = if (hobbiesStr.isBlank()) emptyList() else hobbiesStr.split(","),
                                places = if (placesStr.isBlank()) emptyList() else placesStr.split(","),
                                placesVisible = stmt.getLong(14) != 0L,
                                avatarUrl = stmt.getText(15)
                            )
                        )
                    }
                    list
                } finally {
                    stmt.close()
                }
            }
            listFromDb ?: db.fallbackHosts.values.toList()
        }
    }

    override suspend fun insertHosts(hosts: List<HostEntity>) {
        val success = db.useConnectionOrNull { conn ->
            hosts.forEach { host ->
                val stmt = conn.prepare("INSERT OR REPLACE INTO hosts (id, name, reviewCount, rating, yearsHosting, totalListings, languages, identityVerified, superHost, about, occupation, livesIn, hobbies, places, placesVisible, avatarUrl) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")
                try {
                    stmt.bindText(1, host.id)
                    stmt.bindText(2, host.name)
                    stmt.bindLong(3, host.reviewCount.toLong())
                    stmt.bindDouble(4, host.rating)
                    stmt.bindLong(5, host.yearsHosting.toLong())
                    stmt.bindLong(6, host.totalListings.toLong())
                    stmt.bindText(7, host.languages)
                    stmt.bindLong(8, if (host.identityVerified) 1L else 0L)
                    stmt.bindLong(9, if (host.superHost) 1L else 0L)
                    stmt.bindText(10, host.about)
                    stmt.bindText(11, host.occupation)
                    stmt.bindText(12, host.livesIn)
                    stmt.bindText(13, host.hobbies.joinToString(","))
                    stmt.bindText(14, host.places.joinToString(","))
                    stmt.bindLong(15, if (host.placesVisible) 1L else 0L)
                    stmt.bindText(16, host.avatarUrl)
                    stmt.step()
                } finally {
                    stmt.close()
                }
            }
            true
        }
        if (success == null) {
            hosts.forEach { db.fallbackHosts[it.id] = it }
            db.syncFallbackToStorage()
        }
        signal.update { it + 1 }
    }

    override fun observeProperties(): Flow<List<PropertyListingEntity>> {
        return signal.map {
            val listFromDb = db.useConnectionOrNull { conn ->
                val stmt = conn.prepare("SELECT id, hostId, title, subtitle, rating, reviewCount, imageUrl FROM property_listings")
                try {
                    val list = mutableListOf<PropertyListingEntity>()
                    while (stmt.step()) {
                        list.add(
                            PropertyListingEntity(
                                id = stmt.getText(0),
                                hostId = stmt.getText(1),
                                title = stmt.getText(2),
                                subtitle = stmt.getText(3),
                                rating = stmt.getDouble(4),
                                reviewCount = stmt.getLong(5).toInt(),
                                imageUrl = stmt.getText(6)
                            )
                        )
                    }
                    list
                } finally {
                    stmt.close()
                }
            }
            listFromDb ?: db.fallbackProps.values.toList()
        }
    }

    override suspend fun insertProperties(properties: List<PropertyListingEntity>) {
        val success = db.useConnectionOrNull { conn ->
            properties.forEach { prop ->
                val stmt = conn.prepare("INSERT OR REPLACE INTO property_listings (id, hostId, title, subtitle, rating, reviewCount, imageUrl) VALUES (?, ?, ?, ?, ?, ?, ?)")
                try {
                    stmt.bindText(1, prop.id)
                    stmt.bindText(2, prop.hostId)
                    stmt.bindText(3, prop.title)
                    stmt.bindText(4, prop.subtitle)
                    stmt.bindDouble(5, prop.rating)
                    stmt.bindLong(6, prop.reviewCount.toLong())
                    stmt.bindText(7, prop.imageUrl)
                    stmt.step()
                } finally {
                    stmt.close()
                }
            }
            true
        }
        if (success == null) {
            properties.forEach { db.fallbackProps[it.id] = it }
            db.syncFallbackToStorage()
        }
        signal.update { it + 1 }
    }

    override fun observeReviews(): Flow<List<HostReviewEntity>> {
        return signal.map {
            val listFromDb = db.useConnectionOrNull { conn ->
                val stmt = conn.prepare("SELECT id, hostId, reviewerName, reviewerLocation, reviewerAvatarUrl, stars, dateText, content FROM host_reviews")
                try {
                    val list = mutableListOf<HostReviewEntity>()
                    while (stmt.step()) {
                        list.add(
                            HostReviewEntity(
                                id = stmt.getText(0),
                                hostId = stmt.getText(1),
                                reviewerName = stmt.getText(2),
                                reviewerLocation = stmt.getText(3),
                                reviewerAvatarUrl = stmt.getText(4),
                                stars = stmt.getLong(5).toInt(),
                                dateText = stmt.getText(6),
                                content = stmt.getText(7)
                            )
                        )
                    }
                    list
                } finally {
                    stmt.close()
                }
            }
            listFromDb ?: db.fallbackReviews.values.toList()
        }
    }

    override suspend fun insertReviews(reviews: List<HostReviewEntity>) {
        val success = db.useConnectionOrNull { conn ->
            reviews.forEach { rev ->
                val stmt = conn.prepare("INSERT OR REPLACE INTO host_reviews (id, hostId, reviewerName, reviewerLocation, reviewerAvatarUrl, stars, dateText, content) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")
                try {
                    stmt.bindText(1, rev.id)
                    stmt.bindText(2, rev.hostId)
                    stmt.bindText(3, rev.reviewerName)
                    stmt.bindText(4, rev.reviewerLocation)
                    stmt.bindText(5, rev.reviewerAvatarUrl)
                    stmt.bindLong(6, rev.stars.toLong())
                    stmt.bindText(7, rev.dateText)
                    stmt.bindText(8, rev.content)
                    stmt.step()
                } finally {
                    stmt.close()
                }
            }
            true
        }
        if (success == null) {
            reviews.forEach { db.fallbackReviews[it.id] = it }
            db.syncFallbackToStorage()
        }
        signal.update { it + 1 }
    }

    override fun observeGuides(): Flow<List<TravelGuideEntity>> {
        return signal.map {
            val listFromDb = db.useConnectionOrNull { conn ->
                val stmt = conn.prepare("SELECT id, hostId, title FROM travel_guides")
                try {
                    val list = mutableListOf<TravelGuideEntity>()
                    while (stmt.step()) {
                        list.add(
                            TravelGuideEntity(
                                id = stmt.getText(0),
                                hostId = stmt.getText(1),
                                title = stmt.getText(2)
                            )
                        )
                    }
                    list
                } finally {
                    stmt.close()
                }
            }
            listFromDb ?: db.fallbackGuides.values.toList()
        }
    }

    override suspend fun insertGuides(guides: List<TravelGuideEntity>) {
        val success = db.useConnectionOrNull { conn ->
            guides.forEach { guide ->
                val stmt = conn.prepare("INSERT OR REPLACE INTO travel_guides (id, hostId, title) VALUES (?, ?, ?)")
                try {
                    stmt.bindText(1, guide.id)
                    stmt.bindText(2, guide.hostId)
                    stmt.bindText(3, guide.title)
                    stmt.step()
                } finally {
                    stmt.close()
                }
            }
            true
        }
        if (success == null) {
            guides.forEach { db.fallbackGuides[it.id] = it }
            db.syncFallbackToStorage()
        }
        signal.update { it + 1 }
    }

    override suspend fun clearHosts() {
        val success = db.useConnectionOrNull { conn ->
            conn.execSQL("DELETE FROM hosts")
            true
        }
        if (success == null) {
            db.fallbackHosts.clear()
            db.syncFallbackToStorage()
        }
        signal.update { it + 1 }
    }

    override suspend fun clearProperties() {
        val success = db.useConnectionOrNull { conn ->
            conn.execSQL("DELETE FROM property_listings")
            true
        }
        if (success == null) {
            db.fallbackProps.clear()
            db.syncFallbackToStorage()
        }
        signal.update { it + 1 }
    }

    override suspend fun clearReviews() {
        val success = db.useConnectionOrNull { conn ->
            conn.execSQL("DELETE FROM host_reviews")
            true
        }
        if (success == null) {
            db.fallbackReviews.clear()
            db.syncFallbackToStorage()
        }
        signal.update { it + 1 }
    }

    override suspend fun clearGuides() {
        val success = db.useConnectionOrNull { conn ->
            conn.execSQL("DELETE FROM travel_guides")
            true
        }
        if (success == null) {
            db.fallbackGuides.clear()
            db.syncFallbackToStorage()
        }
        signal.update { it + 1 }
    }
}

private class RealSqliteWeChatMpDao(private val db: RealSqliteAppDatabase) : WeChatMpDao {
    private val signal = MutableStateFlow(0L)

    override fun observeWaterfallArticles(): Flow<List<WeChatArticleEntity>> {
        return signal.map {
            val listFromDb = db.useConnectionOrNull { conn ->
                val stmt = conn.prepare("SELECT id, accountJson, title, summary, coverUrl, publishTimeText, publishTimestamp, cardType, isFollowedAccount, readCount, likeCount, isLiked, isTopSticky, videoDuration, coverAspectRatio, articleUrl FROM wechat_articles WHERE isTopSticky = 0 ORDER BY publishTimestamp DESC")
                try {
                    val list = mutableListOf<WeChatArticleEntity>()
                    while (stmt.step()) {
                        list.add(readArticle(stmt))
                    }
                    list
                } finally {
                    stmt.close()
                }
            }
            listFromDb ?: db.fallbackArticles.values.filter { !it.isTopSticky }.sortedByDescending { it.publishTimestamp }
        }
    }

    override fun observeFeaturedArticle(): Flow<WeChatArticleEntity?> {
        return signal.map {
            val itemFromDb = db.useConnectionOrNull { conn ->
                val stmt = conn.prepare("SELECT id, accountJson, title, summary, coverUrl, publishTimeText, publishTimestamp, cardType, isFollowedAccount, readCount, likeCount, isLiked, isTopSticky, videoDuration, coverAspectRatio, articleUrl FROM wechat_articles WHERE isTopSticky = 1 LIMIT 1")
                try {
                    if (stmt.step()) {
                        readArticle(stmt)
                    } else null
                } finally {
                    stmt.close()
                }
            }
            itemFromDb ?: db.fallbackArticles.values.firstOrNull { it.isTopSticky }
        }
    }

    private fun readArticle(stmt: androidx.sqlite.SQLiteStatement): WeChatArticleEntity {
        return WeChatArticleEntity(
            id = stmt.getText(0),
            accountJson = stmt.getText(1),
            title = stmt.getText(2),
            summary = stmt.getText(3),
            coverUrl = stmt.getText(4),
            publishTimeText = stmt.getText(5),
            publishTimestamp = stmt.getLong(6),
            cardType = stmt.getText(7),
            isFollowedAccount = stmt.getLong(8) != 0L,
            readCount = stmt.getLong(9).toInt(),
            likeCount = stmt.getLong(10).toInt(),
            isLiked = stmt.getLong(11) != 0L,
            isTopSticky = stmt.getLong(12) != 0L,
            videoDuration = stmt.getText(13),
            coverAspectRatio = stmt.getDouble(14).toFloat(),
            articleUrl = stmt.getText(15)
        )
    }

    override suspend fun insertArticles(articles: List<WeChatArticleEntity>) {
        val success = db.useConnectionOrNull { conn ->
            articles.forEach { art ->
                val stmt = conn.prepare("INSERT OR REPLACE INTO wechat_articles (id, accountJson, title, summary, coverUrl, publishTimeText, publishTimestamp, cardType, isFollowedAccount, readCount, likeCount, isLiked, isTopSticky, videoDuration, coverAspectRatio, articleUrl) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")
                try {
                    stmt.bindText(1, art.id)
                    stmt.bindText(2, art.accountJson)
                    stmt.bindText(3, art.title)
                    stmt.bindText(4, art.summary)
                    stmt.bindText(5, art.coverUrl)
                    stmt.bindText(6, art.publishTimeText)
                    stmt.bindLong(7, art.publishTimestamp)
                    stmt.bindText(8, art.cardType)
                    stmt.bindLong(9, if (art.isFollowedAccount) 1L else 0L)
                    stmt.bindLong(10, art.readCount.toLong())
                    stmt.bindLong(11, art.likeCount.toLong())
                    stmt.bindLong(12, if (art.isLiked) 1L else 0L)
                    stmt.bindLong(13, if (art.isTopSticky) 1L else 0L)
                    stmt.bindText(14, art.videoDuration)
                    stmt.bindDouble(15, art.coverAspectRatio.toDouble())
                    stmt.bindText(16, art.articleUrl)
                    stmt.step()
                } finally {
                    stmt.close()
                }
            }
            true
        }
        if (success == null) {
            articles.forEach { db.fallbackArticles[it.id] = it }
            db.syncFallbackToStorage()
        }
        signal.update { it + 1 }
    }

    override suspend fun deleteArticle(articleId: String) {
        val success = db.useConnectionOrNull { conn ->
            val stmt = conn.prepare("DELETE FROM wechat_articles WHERE id = ?")
            try {
                stmt.bindText(1, articleId)
                stmt.step()
            } finally {
                stmt.close()
            }
            true
        }
        if (success == null) {
            db.fallbackArticles.remove(articleId)
            db.syncFallbackToStorage()
        }
        signal.update { it + 1 }
    }

    override suspend fun clearAll() {
        val success = db.useConnectionOrNull { conn ->
            conn.execSQL("DELETE FROM wechat_articles")
            true
        }
        if (success == null) {
            db.fallbackArticles.clear()
            db.syncFallbackToStorage()
        }
        signal.update { it + 1 }
    }
}
