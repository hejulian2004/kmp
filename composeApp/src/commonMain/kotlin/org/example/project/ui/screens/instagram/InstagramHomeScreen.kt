/**
 * @File: InstagramHomeScreen.kt
 * @Package: org.example.project.ui.screens.instagram
 * @Description: Instagram首页Screen视图主入口（架构与FeedLineScreen保持100%一致）
 * @Author: 何聚敛
 * @Date: 2026-07-28
 */
package org.example.project.ui.screens.instagram

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import org.example.project.domain.model.instagram.InstagramMedia
import org.example.project.domain.model.instagram.ProfileUser
import kotlinx.coroutines.launch
import org.example.project.data.repository.instagram.InstagramHomeRepositoryImpl
import org.example.project.data.repository.instagram.createFakeInstagramPosts
import org.example.project.data.repository.instagram.createFakeInstagramStories
import org.example.project.domain.model.instagram.InstagramComment
import org.example.project.domain.model.instagram.InstagramPost
import org.example.project.domain.repository.instagram.InstagramHomeRepository
import org.example.project.platform.currentTimeMillis
import org.example.project.presentation.effect.instagram.InstagramHomeEffect
import org.example.project.presentation.intent.instagram.InstagramHomeIntent
import org.example.project.presentation.state.RefreshState
import org.example.project.presentation.state.UiState
import org.example.project.presentation.viewmodel.instagram.InstagramHomeViewModel
import org.example.project.ui.components.instagram.home.InstagramCommentBar
import org.example.project.ui.components.instagram.home.InstagramConfirmDialog
import org.example.project.ui.components.instagram.home.InstagramHomeTopBar
import org.example.project.ui.components.instagram.home.InstagramPostItem
import org.example.project.ui.components.instagram.home.InstagramStoryTray
import org.example.project.ui.theme.instagram.InstagramTheme
import kotlinproject.composeapp.generated.resources.Res
import kotlinproject.composeapp.generated.resources.cancel
import kotlinproject.composeapp.generated.resources.ins_delete
import kotlinproject.composeapp.generated.resources.ins_delete_comment_msg
import kotlinproject.composeapp.generated.resources.ins_delete_comment_title
import kotlinproject.composeapp.generated.resources.ins_delete_post_msg
import kotlinproject.composeapp.generated.resources.ins_delete_post_title
import kotlinproject.composeapp.generated.resources.ins_no_posts
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.milliseconds

/**
 * Instagram首页主界面组件
 *
 * 包含：
 * - 顶部可折叠/渐隐导航栏(InstagramHomeTopBar)
 * - 下拉刷新列表(PullToRefreshBox + LazyColumn)
 * - 顶部Stories行(InstagramStoryTray)
 * - Feed动态帖子列表(InstagramPostItem)
 * - 底部浮动快速评论输入栏(InstagramCommentBar)
 * - 删除二次确认对话框(InstagramConfirmDialog)
 *
 * @param viewModel首页MVI状态管理器
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstagramHomeScreen(
    viewModel: InstagramHomeViewModel = remember { InstagramHomeViewModel() }
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        org.example.project.ui.core.sdui.registry.registerInstagramSduiComponents()
    }

    val snackbarHostState = remember { SnackbarHostState() }
    var snackbarJob by remember { mutableStateOf<Job?>(null) }

    var commentPostId by remember { mutableStateOf<String?>(null) }
    var commentContent by remember { mutableStateOf("") }

    var pendingDeletePostId by remember { mutableStateOf<String?>(null) }
    var pendingDeleteCommentPair by remember { mutableStateOf<Pair<String, InstagramComment>?>(null) }

    val lazyListState = rememberLazyListState()

    var currentTime by remember { mutableLongStateOf(currentTimeMillis()) }

    // 滚动驱动TopBar上滑平移与渐隐透明度计算
    val density = LocalDensity.current
    val maxTopBarOffsetPx = with(density) { 54.dp.toPx() }
    var topBarOffsetPx by remember { mutableFloatStateOf(0f) }

    val nestedScrollConnection = remember(maxTopBarOffsetPx) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                val newOffset = topBarOffsetPx + delta
                topBarOffsetPx = newOffset.coerceIn(-maxTopBarOffsetPx, 0f)
                return Offset.Zero
            }
        }
    }

    LaunchedEffect(lazyListState.firstVisibleItemIndex, lazyListState.firstVisibleItemScrollOffset) {
        if (lazyListState.firstVisibleItemIndex == 0 && lazyListState.firstVisibleItemScrollOffset == 0) {
            topBarOffsetPx = 0f
        }
    }

    val topBarAlpha = (1f + topBarOffsetPx / maxTopBarOffsetPx).coerceIn(0f, 1f)
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = currentTimeMillis()
            delay((60 * 1000L).milliseconds)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is InstagramHomeEffect.ShowMessage -> {
                    snackbarJob?.cancel()
                    snackbarHostState.currentSnackbarData?.dismiss()

                    snackbarJob = launch {
                        snackbarHostState.showSnackbar(
                            message = effect.message,
                            duration = SnackbarDuration.Short
                        )
                    }
                }

                is InstagramHomeEffect.ScrollToIndex -> {
                    lazyListState.animateScrollToItem(effect.index)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.background(MaterialTheme.colorScheme.background)
    ) { innerPadding ->
        val postsState = uiState.postsState
        val storiesState = uiState.storiesState

        val isRefreshing = (postsState as? UiState.Success)?.refreshState == RefreshState.Refreshing

        Box(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(nestedScrollConnection)
        ) {
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = {
                    viewModel.handleIntent(InstagramHomeIntent.Refresh)
                },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .fillMaxSize()
            ) {
                when (postsState) {
                    is UiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(40.dp))
                        }
                    }

                    is UiState.Error -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = postsState.message, color = Color.Red)
                        }
                    }

                    is UiState.Success, is UiState.Idle -> {
                        val posts = (postsState as? UiState.Success)?.data ?: emptyList()
                        val stories = (storiesState as? UiState.Success)?.data ?: emptyList()

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            state = lazyListState,
                            contentPadding = PaddingValues(
                                top = statusBarHeight + 54.dp,
                                bottom = innerPadding.calculateBottomPadding()
                            )
                        ) {
                        // 顶部Story快拍栏
//                        if (stories.isNotEmpty()) {
//                            item {
//                                InstagramStoryTray(
//                                    stories = stories,
//                                    onStoryClick = { story ->
//                                        viewModel.handleIntent(
//                                            InstagramHomeIntent.ShowMessage("Viewing story of @${story.postUser.username}")
//                                        )
//                                    },
//                                    onAddStoryClick = {
//                                        viewModel.handleIntent(
//                                            InstagramHomeIntent.ShowMessage("Add to Your Story")
//                                        )
//                                    }
//                                )
//                                HorizontalDivider(
//                                    thickness = 0.5.dp,
//                                    color = Color.LightGray.copy(alpha = 0.4f)
//                                )
//                            }
//                        }

                        // 空Feed动态帖子占位视图
                        if (posts.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier.fillParentMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(stringResource(Res.string.ins_no_posts), color = Color.Gray)
                                }
                            }
                        }

                        // Feed动态帖子列表
                        items(
                            items = posts,
                            key = { post -> post.id }
                        ) { post ->
                            InstagramPostItem(
                                post = post,
                                currentUser = uiState.currentUser,
                                onClick = { targetPost ->
                                    viewModel.handleIntent(InstagramHomeIntent.ShowMessage("Post ID: ${targetPost.id}"))
                                },
                                onPostAvatarClick = { user ->
                                    viewModel.handleIntent(InstagramHomeIntent.ShowMessage("User avatar: @${user.username}"))
                                },
                                onNameClick = { user ->
                                    viewModel.handleIntent(InstagramHomeIntent.ShowMessage("User name: @${user.username}"))
                                },
                                onLocationClick = { loc ->
                                    viewModel.handleIntent(InstagramHomeIntent.ShowMessage("Location: $loc"))
                                },
                                onAudioClick = { audio ->
                                    viewModel.handleIntent(InstagramHomeIntent.ShowMessage("Audio: $audio"))
                                },
                                onMediaClick = { index ->
                                    viewModel.handleIntent(InstagramHomeIntent.ShowMessage("Media page ${index + 1} clicked"))
                                },
                                onLikeClick = {
                                    if (post.isLiked) {
                                        viewModel.handleIntent(
                                            InstagramHomeIntent.UnlikePost(post.id, uiState.currentUser)
                                        )
                                    } else {
                                        viewModel.handleIntent(
                                            InstagramHomeIntent.LikePost(post.id, uiState.currentUser)
                                        )
                                    }
                                },
                                onLikedUserClick = { user ->
                                    viewModel.handleIntent(InstagramHomeIntent.ShowMessage("Liked by: @${user.username}"))
                                },
                                onSaveClick = {
                                    if (post.isSaved) {
                                        viewModel.handleIntent(InstagramHomeIntent.UnsavePost(post.id))
                                    } else {
                                        viewModel.handleIntent(InstagramHomeIntent.SavePost(post.id))
                                    }
                                },
                                onAddCommentClick = {
                                    commentPostId = post.id
                                    commentContent = ""
                                },
                                onCommentClick = { comment ->
                                    viewModel.handleIntent(InstagramHomeIntent.ShowMessage("Comment: ${comment.content}"))
                                },
                                onCommentUserClick = { user ->
                                    viewModel.handleIntent(InstagramHomeIntent.ShowMessage("Commenter: @${user.username}"))
                                },
                                onDeletePostClick = { targetPost ->
                                    pendingDeletePostId = targetPost.id
                                },
                                onDeleteCommentClick = { comment ->
                                    pendingDeleteCommentPair = Pair(post.id, comment)
                                },
                                onShareClick = {
                                    viewModel.handleIntent(InstagramHomeIntent.ShowMessage("Share post: ${post.id}"))
                                },
                                onRepostClick = {
                                    viewModel.handleIntent(InstagramHomeIntent.ShowMessage("Repost: @${post.postUser.username}'s post"))
                                },
                                currentTime = currentTime
                            )
                            HorizontalDivider(
                                thickness = 0.5.dp,
                                color = Color.LightGray.copy(alpha = 0.3f)
                            )
                        }
                    }
                }
            }
        }

            // 可折叠TopBar组件，随列表上滑逐渐平移移出与渐隐
            InstagramHomeTopBar(
                unreadNotificationCount = uiState.unreadNotificationCount,
                onCreatePostClick = {
                    viewModel.handleIntent(InstagramHomeIntent.ShowMessage("Create Post clicked"))
                },
                onNotificationClick = {
                    viewModel.handleIntent(InstagramHomeIntent.ShowMessage("Notifications clicked"))
                },
                onLogoClick = {
                    viewModel.handleIntent(InstagramHomeIntent.ShowMessage("Instagram Home Feed"))
                },
                modifier = Modifier
                    .graphicsLayer {
                        translationY = topBarOffsetPx
                        alpha = topBarAlpha
                    }
            )

            // 固定系统状态栏遮罩容器（置于最顶层，完全挡住上滑移入状态栏区域的TopBar内容）
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsTopHeight(WindowInsets.statusBars)
                    .background(MaterialTheme.colorScheme.background)
            )
        }
    }

    // 删除Post动态二次确认对话框
    if (pendingDeletePostId != null) {
        InstagramConfirmDialog(
            onDismissRequest = { pendingDeletePostId = null },
            title = stringResource(Res.string.ins_delete_post_title),
            text = stringResource(Res.string.ins_delete_post_msg),
            confirmText = stringResource(Res.string.ins_delete),
            dismissText = stringResource(Res.string.cancel),
            onConfirmClick = {
                val postId = pendingDeletePostId
                if (postId != null) {
                    viewModel.handleIntent(InstagramHomeIntent.DeletePost(postId))
                }
                pendingDeletePostId = null
            },
            onDismissClick = { pendingDeletePostId = null }
        )
    }

    // 删除Comment评论二次确认对话框
    if (pendingDeleteCommentPair != null) {
        InstagramConfirmDialog(
            onDismissRequest = { pendingDeleteCommentPair = null },
            title = stringResource(Res.string.ins_delete_comment_title),
            text = stringResource(Res.string.ins_delete_comment_msg),
            confirmText = stringResource(Res.string.ins_delete),
            dismissText = stringResource(Res.string.cancel),
            onConfirmClick = {
                val pair = pendingDeleteCommentPair
                if (pair != null) {
                    viewModel.handleIntent(
                        InstagramHomeIntent.DeleteComment(postId = pair.first, commentId = pair.second.id)
                    )
                }
                pendingDeleteCommentPair = null
            },
            onDismissClick = { pendingDeleteCommentPair = null }
        )
    }

    // 底部浮动Comment快速评论输入栏
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    if (commentPostId != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    focusManager.clearFocus(force = true)
                    keyboardController?.hide()
                    commentPostId = null
                    commentContent = ""
                },
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    // 拦截评论输入栏内部点击事件
                }
            ) {
                InstagramCommentBar(
                    avatarUrl = uiState.currentUser.avatarUrl,
                    value = commentContent,
                    onValueChange = { commentContent = it },
                    onSendClick = {
                        val postId = commentPostId
                        val content = commentContent.trim()
                        if (postId != null && content.isNotEmpty()) {
                            viewModel.handleIntent(
                                InstagramHomeIntent.AddComment(
                                    postId = postId,
                                    user = uiState.currentUser,
                                    content = content
                                )
                            )
                            focusManager.clearFocus(force = true)
                            keyboardController?.hide()
                            commentPostId = null
                            commentContent = ""
                        }
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InstagramHomeScreenPreview() {
    InstagramTheme {
        val mockRepo = object : InstagramHomeRepository {
            override fun getHomePosts() = flowOf(createFakeInstagramPosts())
            override fun getStories() = flowOf(createFakeInstagramStories())
            override suspend fun refreshHome() {}
            override suspend fun likePost(postId: String, currentUser: ProfileUser) {}
            override suspend fun unlikePost(postId: String, currentUser: ProfileUser) {}
            override suspend fun savePost(postId: String) {}
            override suspend fun unsavePost(postId: String) {}
            override suspend fun addComment(postId: String, currentUser: ProfileUser, content: String) {}
            override suspend fun deleteComment(postId: String, commentId: String) {}
            override suspend fun deletePost(postId: String) {}
            override suspend fun createPost(user: ProfileUser, content: String, mediaList: List<InstagramMedia>, location: String?) {}
        }

        InstagramHomeScreen(
            viewModel = InstagramHomeViewModel(repository = mockRepo)
        )
    }
}
