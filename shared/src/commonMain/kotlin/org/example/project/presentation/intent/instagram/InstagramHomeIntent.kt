/**
 * @File: InstagramHomeIntent.kt
 * @Package: org.example.project.presentation.intent.instagram
 * @Description: Instagram首页MVI模式的用户意图事件集合（对标FeedIntent）
 * @Author: 何聚敛
 * @Date: 2026-07-28
 */
package org.example.project.presentation.intent.instagram

import org.example.project.domain.model.instagram.InstagramMedia
import org.example.project.domain.model.instagram.ProfileUser
import org.example.project.presentation.state.instagram.InstagramHomeScreenType

/**
 * Instagram首页MVI模式的用户意图事件集合
 */
sealed interface InstagramHomeIntent {
    /** 触发刷新首页动态 */
    data object Refresh : InstagramHomeIntent

    /** 点赞帖子 */
    data class LikePost(val postId: String, val user: ProfileUser) : InstagramHomeIntent

    /** 取消点赞帖子 */
    data class UnlikePost(val postId: String, val user: ProfileUser) : InstagramHomeIntent

    /** 收藏帖子 */
    data class SavePost(val postId: String) : InstagramHomeIntent

    /** 取消收藏帖子 */
    data class UnsavePost(val postId: String) : InstagramHomeIntent

    /** 添加评论 */
    data class AddComment(val postId: String, val user: ProfileUser, val content: String) : InstagramHomeIntent

    /** 删除评论 */
    data class DeleteComment(val postId: String, val commentId: String) : InstagramHomeIntent

    /** 删除帖子 */
    data class DeletePost(val postId: String) : InstagramHomeIntent

    /** 发布新帖子 */
    data class CreatePost(
        val user: ProfileUser,
        val content: String,
        val mediaList: List<InstagramMedia>,
        val location: String? = null
    ) : InstagramHomeIntent

    /** 弹出提示消息 */
    data class ShowMessage(val message: String) : InstagramHomeIntent

    /** 切换页面导航 */
    data class NavigateTo(val screen: InstagramHomeScreenType) : InstagramHomeIntent
}
