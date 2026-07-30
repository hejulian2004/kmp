/**
 * @File: InstagramPostItem.kt
 * @Package: org.example.project.ui.components.instagram.home
 * @Description: Instagram Feed动态帖子卡片容器组件（组合Header、MediaCarousel、ActionBar、ContentPanel四大模块组件）
 * @Author: 何聚敛
 * @Date: 2026-07-29
 */
package org.example.project.ui.components.instagram.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.example.project.data.repository.instagram.createFakeInstagramPosts
import org.example.project.domain.model.instagram.InstagramComment
import org.example.project.domain.model.instagram.InstagramMedia
import org.example.project.domain.model.instagram.InstagramPost
import org.example.project.domain.model.instagram.ProfileUser
import org.example.project.ui.theme.instagram.InstagramTheme

/**
 * Instagram动态Feed帖子卡片组件
 *
 * 继承并模仿 [FeedPostItem] 的组件回调设计风格，将所有原子级的交互行为通过Lambda () -> Unit导出，允许上层自由响应与自定义扩展。
 *
 * @param post 帖子聚合实体数据
 * @param currentUser 当前登录用户
 * @param onClick 卡片整体被点击回调
 * @param onPostAvatarClick 帖子作者头像被点击回调
 * @param onNameClick 帖子作者用户名被点击回调
 * @param onLocationClick 地理位置被点击回调
 * @param onAudioClick 背景音乐音轨被点击回调
 * @param onMediaClick 媒体轮播图片被点击回调(传入当前媒体index)
 * @param onLikeClick 点赞/双击点赞按钮回调
 * @param onLikedUserClick 点赞列表中的用户名被点击回调
 * @param onAddCommentClick 点击评论图标/查看全部评论回调
 * @param onCommentClick 评论列表中的单条评论被点击回调
 * @param onCommentUserClick 评论发布者用户名被点击回调
 * @param onDeleteCommentClick 点击删除评论按钮回调
 * @param onDeletePostClick 点击删除帖子按钮回调
 * @param onSaveClick 点击收藏/取消收藏按钮回调
 * @param onRepostClick 点击转发按钮回调
 * @param onShareClick 点击分享按钮回调
 * @param currentTime 当前系统时间戳(用于计算相对发布时间)
 * @param modifier 外部修饰符
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InstagramPostItem(
    post: InstagramPost,
    currentUser: ProfileUser,
    onClick: (InstagramPost) -> Unit = {},
    onPostAvatarClick: (ProfileUser) -> Unit = {},
    onNameClick: (ProfileUser) -> Unit = {},
    onLocationClick: (String) -> Unit = {},
    onAudioClick: (String) -> Unit = {},
    onMediaClick: (Int) -> Unit = {},
    onLikeClick: () -> Unit = {},
    onLikedUserClick: (ProfileUser) -> Unit = {},
    onAddCommentClick: () -> Unit = {},
    onCommentClick: (InstagramComment) -> Unit = {},
    onCommentUserClick: (ProfileUser) -> Unit = {},
    onDeleteCommentClick: (InstagramComment) -> Unit = {},
    onDeletePostClick: (InstagramPost) -> Unit = {},
    onSaveClick: () -> Unit = {},
    onRepostClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    onFollowClick: (ProfileUser) -> Unit = {},
    currentTime: Long = 0L,
    modifier: Modifier = Modifier
) {
    val mediaList = post.mediaList.ifEmpty {
        listOf(InstagramMedia.Image("https://picsum.photos/seed/${post.id}/1080/1080"))
    }
    val pagerState = rememberPagerState(pageCount = { mediaList.size })

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .clickable { onClick(post) }
            .padding(bottom = 12.dp)
    ) {
        // 1. 帖子头部作者信息栏模块组件（包含关注按钮及更多操作下拉菜单）
        InstagramPostHeader(
            post = post,
            currentUser = currentUser,
            onAvatarClick = { onPostAvatarClick(post.postUser) },
            onNameClick = { onNameClick(post.postUser) },
            onLocationClick = { post.location?.let(onLocationClick) },
            onAudioClick = { post.audioTitle?.let(onAudioClick) },
            onFollowClick = { onFollowClick(post.postUser) },
            onDeletePostClick = { onDeletePostClick(post) }
        )

        // 2. 帖子媒体多图轮播模块组件（含多图圆点指示器居中在正下方）
        InstagramPostMediaCarousel(
            post = post,
            pagerState = pagerState,
            onMediaClick = onMediaClick,
            onDoubleTapLike = {
                if (!post.isLiked) {
                    onLikeClick()
                }
            }
        )

        // 3. 帖子快捷交互操作按钮工具栏与数据展示模块组件（图标右侧紧跟数字：点赞数、评论数、转发数、分享数）
        InstagramPostItemActionBar(
            isLiked = post.isLiked,
            isSaved = post.isSaved,
            likesCount = post.likesCount.toLong(),
            commentsCount = post.commentsCount.toLong(),
            repostCount = post.repostCount ?: 0L,
            shareCount = post.shareCount ?: 0L,
            isLikeCountHidden = post.isLikeCountHidden,
            onLikeClick = onLikeClick,
            onAddCommentClick = onAddCommentClick,
            onRepostClick = onRepostClick,
            onShareClick = onShareClick,
            onSaveClick = onSaveClick
        )

        // 4. 帖子信息内容面板模块组件（正文文案、评论及相对时间）
        InstagramPostItemContentPanel(
            post = post,
            currentUser = currentUser,
            onAddCommentClick = onAddCommentClick,
            onCommentClick = onCommentClick,
            onCommentUserClick = onCommentUserClick,
            onDeleteCommentClick = onDeleteCommentClick
        )
    }
}

@Preview(showBackground = true)
@Composable
fun InstagramPostItemPreview() {
    InstagramTheme {
        val fakePosts = createFakeInstagramPosts()
        if (fakePosts.isNotEmpty()) {
            InstagramPostItem(
                post = fakePosts.first(),
                currentUser = ProfileUser(
                    userId = "u_me",
                    username = "hejulian",
                    avatarUrl = "https://picsum.photos/seed/me/200/200",
                    signature = "Kotlin KMP Developer",
                    postCount = "18",
                    followerCount = "450",
                    followingCount = "320"
                )
            )
        }
    }
}
