/**
 * @File: InstagramHomeRepository.kt
 * @Package: org.example.project.domain.repository.instagram
 * @Description: Instagram 首页数据仓库接口（全量使用 InstagramPost 实体）
 * @Author: 何聚敛
 * @Date: 2026-07-28
 */
package org.example.project.domain.repository.instagram

import kotlinx.coroutines.flow.Flow
import org.example.project.domain.model.instagram.InstagramMedia
import org.example.project.domain.model.instagram.InstagramPost
import org.example.project.domain.model.instagram.ProfileUser

/**
 * Instagram 首页数据仓库接口
 */
interface InstagramHomeRepository {
    /** 获取 Feed 动态列表 */
    fun getHomePosts(): Flow<List<InstagramPost>>

    /** 获取 Story 快拍列表 */
    fun getStories(): Flow<List<InstagramPost>>

    /** 刷新首页 */
    suspend fun refreshHome()

    /** 点赞帖子 */
    suspend fun likePost(postId: String, currentUser: ProfileUser)

    /** 取消点赞帖子 */
    suspend fun unlikePost(postId: String, currentUser: ProfileUser)

    /** 收藏帖子 */
    suspend fun savePost(postId: String)

    /** 取消收藏帖子 */
    suspend fun unsavePost(postId: String)

    /** 添加评论 */
    suspend fun addComment(postId: String, currentUser: ProfileUser, content: String)

    /** 删除评论 */
    suspend fun deleteComment(postId: String, commentId: String)

    /** 删除帖子 */
    suspend fun deletePost(postId: String)

    /** 创建新帖子 */
    suspend fun createPost(user: ProfileUser, content: String, mediaList: List<InstagramMedia>, location: String? = null)
}
