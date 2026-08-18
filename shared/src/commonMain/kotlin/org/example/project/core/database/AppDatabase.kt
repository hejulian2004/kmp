/**
 * @File: AppDatabase.kt
 * @Package: org.example.project.core.database
 * @Description: 全局应用 Room 离线统一数据库定义（基于Room KMP compiler/KSP代码生成与@ConstructedBy）
 * @Author: 何聚敛
 * @Date: 2026-08-18
 */
package org.example.project.core.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import org.example.project.data.database.converter.StringListConverter
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
        TravelGuideEntity::class,
        WeChatArticleEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(StringListConverter::class)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun feedLineDao(): FeedLineDao
    abstract fun instagramDao(): InstagramDao
    abstract fun hostProfileDao(): HostProfileDao
    abstract fun weChatMpDao(): WeChatMpDao
}

/**
 * Room 编译期代码生成构造器契约
 */
@Suppress("NO_ACTUAL_FOR_EXPECT", "KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}
