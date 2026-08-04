/**
 * @File: FeedLineDao.kt
 * @Package: org.example.project.data.database.dao.feedline
 * @Description: 朋友圈动态与通知消息 Room DAO 契约接口
 * @Author: 何聚敛
 * @Date: 2026-08-04
 */
package org.example.project.data.database.dao.feedline

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.example.project.data.database.entity.feedline.FeedLineNotificationEntity
import org.example.project.data.database.entity.feedline.FeedLinePostEntity

@Dao
interface FeedLineDao {
    @Query("SELECT * FROM feedline_posts")
    fun observePosts(): Flow<List<FeedLinePostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<FeedLinePostEntity>)

    @Query("DELETE FROM feedline_posts WHERE id = :postId")
    suspend fun deletePost(postId: String)

    @Query("SELECT * FROM feedline_notifications")
    fun observeNotifications(): Flow<List<FeedLineNotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<FeedLineNotificationEntity>)

    @Query("DELETE FROM feedline_posts")
    suspend fun clearPosts()

    @Query("DELETE FROM feedline_notifications")
    suspend fun clearNotifications()
}
