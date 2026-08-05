/**
 * @File: InstagramDao.kt
 * @Package: org.example.project.data.database.dao.instagram
 * @Description: Instagram 动态与 Story 快拍 Room DAO 契约接口
 * @Author: 何聚敛
 * @Date: 2026-08-05
 */
package org.example.project.data.database.dao.instagram

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.example.project.data.database.entity.instagram.InstagramPostEntity

@Dao
interface InstagramDao {
    @Query("SELECT * FROM instagram_posts WHERE isStory = 0")
    fun observePosts(): Flow<List<InstagramPostEntity>>

    @Query("SELECT * FROM instagram_posts WHERE isStory = 1")
    fun observeStories(): Flow<List<InstagramPostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<InstagramPostEntity>)

    @Query("DELETE FROM instagram_posts WHERE id = :postId")
    suspend fun deletePost(postId: String)

    @Query("DELETE FROM instagram_posts")
    suspend fun clearAll()
}
