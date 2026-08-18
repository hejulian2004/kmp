/**
 * @File: AppDatabase.kt
 * @Package: org.example.project.core.database
 * @Description: 全局应用 Room 离线统一数据库定义
 * @Author: 何聚敛
 * @Date: 2026-08-18
 */
package org.example.project.core.database

import androidx.room.Database
import androidx.room.InvalidationTracker
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.example.project.data.database.converter.StringListConverter
import org.example.project.data.database.dao.airbnb.HostProfileDao
import org.example.project.data.database.dao.feedline.FeedLineDao
import org.example.project.data.database.dao.instagram.InstagramDao
import org.example.project.data.database.entity.airbnb.HostEntity
import org.example.project.data.database.entity.airbnb.HostReviewEntity
import org.example.project.data.database.entity.airbnb.PropertyListingEntity
import org.example.project.data.database.entity.airbnb.TravelGuideEntity
import org.example.project.data.database.entity.feedline.FeedLineNotificationEntity
import org.example.project.data.database.entity.feedline.FeedLinePostEntity
import org.example.project.data.database.entity.instagram.InstagramPostEntity

/**
 * 全局 Room KMP 数据库抽象类。
 */
@Database(
    entities = [
        FeedLinePostEntity::class,
        FeedLineNotificationEntity::class,
        InstagramPostEntity::class,
        HostEntity::class,
        PropertyListingEntity::class,
        HostReviewEntity::class,
        TravelGuideEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(StringListConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun feedLineDao(): FeedLineDao
    abstract fun instagramDao(): InstagramDao
    abstract fun hostProfileDao(): HostProfileDao

    override fun createInvalidationTracker(): InvalidationTracker {
        return InvalidationTracker(
            this,
            emptyMap(),
            emptyMap(),
            "FeedLinePostEntity",
            "FeedLineNotificationEntity",
            "InstagramPostEntity",
            "HostEntity",
            "PropertyListingEntity",
            "HostReviewEntity",
            "TravelGuideEntity"
        )
    }
}

private val sharedFeedLineDao by lazy { FakeFeedLineDao() }
private val sharedInstagramDao by lazy { FakeInstagramDao() }
private val sharedHostProfileDao by lazy { FakeHostProfileDao() }

/**
 * AppDatabase 默认数据访问派生实现。
 */
abstract class BaseDefaultAppDatabase : AppDatabase() {
    override fun feedLineDao(): FeedLineDao = sharedFeedLineDao
    override fun instagramDao(): InstagramDao = sharedInstagramDao
    override fun hostProfileDao(): HostProfileDao = sharedHostProfileDao
}

/**
 * 平台级 AppDatabase 构造工厂。
 */
expect fun createDefaultAppDatabase(): AppDatabase
