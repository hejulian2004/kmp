/**
 * @File: FeedLineScreen.kt
 * @Package: org.example.project.ui.screens.feedline
 * @Description: 朋友圈动态主界面的Compose视图入口（基于FileKit跨平台适配）
 * @Author: 何聚敛
 * @Date: 2026-07-22
 */
package org.example.project.ui.screens.feedline

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import org.example.project.ui.utils.BackHandler
import org.example.project.ui.utils.CameraMediaType
import org.example.project.ui.utils.rememberCameraPickerLauncher
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.path
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import org.example.project.data.repository.feedline.FeedRepositoryImpl
import org.example.project.core.network.config.createFakeData
import org.example.project.data.repository.feedline.generateUUID
import org.example.project.domain.model.feedline.FeedLineComment
import org.example.project.domain.model.feedline.FeedLineMedia
import org.example.project.domain.model.feedline.FeedLineUser
import org.example.project.domain.repository.feedline.FeedLineRepository
import org.example.project.platform.currentTimeMillis
import org.example.project.presentation.effect.feedline.FeedLineEffect
import org.example.project.presentation.intent.feedline.FeedIntent
import org.example.project.presentation.state.RefreshState
import org.example.project.presentation.state.UiState
import org.example.project.presentation.state.feedline.Screen
import org.example.project.presentation.viewmodel.feedline.FeedLineViewModel
import org.example.project.ui.components.feedline.BottomSheet
import org.example.project.ui.components.feedline.FeedLineConfirmDialog
import org.example.project.ui.components.feedline.FeedCommentBar
import org.example.project.ui.components.feedline.FeedNotificationBar
import org.example.project.ui.components.feedline.FeedPostItem
import org.example.project.ui.components.feedline.FeedTopBar
import org.example.project.ui.components.feedline.PublishScreen
import kotlin.time.Duration.Companion.milliseconds

/**
 * 朋友圈主页面组件
 * 
 * 包含：
 * - 顶部导航栏 (FeedTopBar)
 * - 下拉刷新列表 (PullToRefreshBox + LazyColumn)
 * - 底部弹出层 (BottomSheet用于发布/评论)
 * 
 * @param viewModel朋友圈状态管理器
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    viewModel: FeedLineViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        org.example.project.ui.core.sdui.registry.registerFeedLineSduiComponents()
    }

    val snackbarHostState = remember {
        SnackbarHostState()
    }

    var snackbarJob by remember {
        mutableStateOf<Job?>(null)
    }

    var showBottomSheet by remember {
        mutableStateOf(false)
    }

    val bottomSheetState = rememberModalBottomSheetState()

    var commentPostId by remember {
        mutableStateOf<String?>(null)
    }

    var commentContent by remember {
        mutableStateOf("")
    }

    var publishMediaList by remember { mutableStateOf<List<FeedLineMedia>>(emptyList()) }
    var isPublishTextOnly by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberFilePickerLauncher(
        type = FileKitType.ImageAndVideo,
        mode = FileKitMode.Multiple(maxItems = 9),
        onResult = { files ->
            files?.let { list ->
                val media = list.map { file ->
                    val filePath = file.path
                    val fileName = file.name
                    val isVideo = fileName.endsWith(".mp4", ignoreCase = true) ||
                            fileName.endsWith(".mov", ignoreCase = true) ||
                            fileName.endsWith(".mkv", ignoreCase = true)
                    if (isVideo) {
                        FeedLineMedia.Video(coverUrl = filePath, videoUrl = filePath)
                    } else {
                        FeedLineMedia.Image(url = filePath)
                    }
                }
                publishMediaList = media
                isPublishTextOnly = false
                viewModel.handleIntent(FeedIntent.NavigateTo(Screen.Publish))
            }
        }
    )

    val takePictureLauncher = rememberCameraPickerLauncher(
        type = CameraMediaType.Photo,
        onResult = { file ->
            file?.let {
                val filePath = it.path
                publishMediaList = listOf(FeedLineMedia.Image(url = filePath))
                isPublishTextOnly = false
                viewModel.handleIntent(FeedIntent.NavigateTo(Screen.Publish))
            }
        }
    )

    val takeVideoLauncher = rememberCameraPickerLauncher(
        type = CameraMediaType.Video,
        onResult = { file ->
            file?.let {
                val filePath = it.path
                publishMediaList = listOf(FeedLineMedia.Video(coverUrl = filePath, videoUrl = filePath))
                isPublishTextOnly = false
                viewModel.handleIntent(FeedIntent.NavigateTo(Screen.Publish))
            }
        }
    )

    var currentTime by remember {
        mutableLongStateOf(currentTimeMillis())
    }

    var pendingDeletePostId by remember {
        mutableStateOf<String?>(null)
    }

    var pendingDeleteComment by remember {
        mutableStateOf<FeedLineComment?>(null)
    }

    val lazyListState = rememberLazyListState()

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = currentTimeMillis()
            delay((60 * 1000L).milliseconds)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is FeedLineEffect.ShowMessage -> {
                    snackbarJob?.cancel()
                    snackbarHostState.currentSnackbarData?.dismiss()

                    snackbarJob = launch {
                        snackbarHostState.showSnackbar(
                            message = effect.message,
                            duration = SnackbarDuration.Short
                        )
                    }
                }

                is FeedLineEffect.ScrollToIndex -> { //发帖后回到顶部,默认回到index=0
                    lazyListState.animateScrollToItem(effect.index)
                }
            }
        }
    }

    BackHandler(enabled = uiState.currentScreen != Screen.Feed) {
        viewModel.handleIntent(FeedIntent.NavigateTo(Screen.Feed))
    }

    if (uiState.currentScreen == Screen.Notification) {
        NotificationScreen(viewModel = viewModel)
    } else if (uiState.currentScreen == Screen.Publish) {
        PublishScreen(
            initialMediaList = publishMediaList,
            isTextOnly = isPublishTextOnly,
            onCancelClick = {
                viewModel.handleIntent(FeedIntent.NavigateTo(Screen.Feed))
            },
            onPostClick = { textContent, mediaList ->
                viewModel.handleIntent(
                    FeedIntent.CreatePost(
                        user = uiState.currentUser,
                        content = textContent,
                        mediaList = mediaList
                    )
                )
                viewModel.handleIntent(FeedIntent.NavigateTo(Screen.Feed))
            }
        )
    } else {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                FeedTopBar(
                    onShortClickCreatePost = {
                        showBottomSheet = true
                    },
                    onLongClickCreatePost = {
                        publishMediaList = emptyList()
                        isPublishTextOnly = true
                        viewModel.handleIntent(FeedIntent.NavigateTo(Screen.Publish))
                    }
                )
            },
            modifier = Modifier.background(Color.White)
        ) { innerPadding ->
            val feedState = uiState.feedState
            val isRefreshing = (feedState as? UiState.Success)?.refreshState == RefreshState.Refreshing

            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = {
                    viewModel.handleIntent(FeedIntent.Refresh)
                },
                modifier = Modifier
                    .background(Color.White)
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                when (feedState) {
                    is UiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(40.dp))
                        }
                    }
                    is UiState.Error -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = feedState.message, color = Color.Red)
                        }
                    }
                    is UiState.Success, is UiState.Idle -> {
                        val posts = (feedState as? UiState.Success)?.data ?: emptyList()
                        
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            state = lazyListState
                        ) {
                            if (uiState.unreadNotificationCount > 0) {
                                item {
                                    val notifications = (uiState.notificationsState as? UiState.Success)?.data ?: emptyList()
                                    val latestNotification = notifications.firstOrNull { !it.isRead && !it.isDelete }
                                    FeedNotificationBar(
                                        unreadCount = uiState.unreadNotificationCount,
                                        latestNotificationUserAvatar = latestNotification?.user?.avatarUrl,
                                        onClick = {
                                            viewModel.handleIntent(FeedIntent.NavigateTo(Screen.Notification))
                                        }
                                    )
                                }
                            }

                            if (posts.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier.fillParentMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("暂无动态")
                                    }
                                }
                            }

                            items(
                                items = posts,
                                key = { post -> post.id },
                            ) { post ->
                                FeedPostItem(
                                    post = post,
                                    currentUser = uiState.currentUser,
                                    onClick = { post ->
                                        viewModel.handleIntent(FeedIntent.ShowMessage("postId:${post.id}"))
                                    },
                                    onNameClick = { viewModel.handleIntent(FeedIntent.ShowMessage(post.postUser.name)) },
                                    onLikeClick = {
                                        if (!post.isLiked) {
                                            viewModel.handleIntent(
                                                FeedIntent.LikePost(
                                                    post.id,
                                                    uiState.currentUser
                                                )
                                            )
                                        } else {
                                            viewModel.handleIntent(
                                                FeedIntent.UnlikePost(
                                                    post.id,
                                                    uiState.currentUser
                                                )
                                            )
                                        }
                                    },
                                    onAddCommentClick = {
                                        commentPostId = post.id
                                        commentContent = ""
                                    },
                                    onDeleteCommentClick = { comment ->
                                        pendingDeleteComment = if (comment.commentUser.id == uiState.currentUser.id) {
                                            comment
                                        } else {
                                            null
                                        }
                                    },
                                    onDeletePostClick = { post ->
                                        pendingDeletePostId = post.id
                                    },
                                    onPostAvatarClick = {
                                        viewModel.handleIntent(FeedIntent.ShowMessage(post.postUser.toString()))
                                    },
                                    onLikedAvatarClick = { user ->
                                        viewModel.handleIntent(FeedIntent.ShowMessage(user.toString()))
                                    },
                                    currentTime = currentTime,
                                    onCommentClick = { comment ->
                                        viewModel.handleIntent(FeedIntent.ShowMessage(comment.toString()))
                                    },
                                    onCommentUserClick = { user ->
                                        viewModel.handleIntent(FeedIntent.ShowMessage(user.toString()))
                                    },
                                )
                                HorizontalDivider(
                                    thickness = 0.5.dp,
                                    color = Color.LightGray
                                )
                            }
                        }
                    }
                }
            }
        }

        // 展开发布bottom sheet栏
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = bottomSheetState,
                dragHandle = {
                    BottomSheetDefaults.DragHandle()
                }
            ) {
                BottomSheet(
                    onTakePhotoClick = {
                        showBottomSheet = false
                        takePictureLauncher.launch()
                    },
                    onRecordVideoClick = {
                        showBottomSheet = false
                        takeVideoLauncher.launch()
                    },
                    onChooseClick = {
                        showBottomSheet = false
                        photoPickerLauncher.launch()
                    },
                    onCancelClick = {
                        showBottomSheet = false
                    }
                )
            }
        }

        // 删除自己的帖子
        if (pendingDeletePostId != null) {
            FeedLineConfirmDialog(
                onDismissRequest = {
                    pendingDeletePostId = null
                },
                title = "确认删除",
                text = "确定要删除这条动态吗？删除后不可恢复",
                onConfirmClick = {
                    val postId = pendingDeletePostId
                    if (postId != null) {
                        viewModel.handleIntent(
                            FeedIntent.DeletePost(
                                postId = postId
                            )
                        )
                    }
                    pendingDeletePostId = null
                },
                onDismissClick = {
                    pendingDeletePostId = null
                }
            )
        }

        // 删除自己发的评论
        if (pendingDeleteComment != null) {
            FeedLineConfirmDialog(
                onDismissRequest = {
                    pendingDeleteComment = null
                },
                title = "确认删除",
                text = "确定要删除这条评论吗？删除后不可恢复",
                onConfirmClick = {
                    val comment = pendingDeleteComment
                    if (comment != null) {
                        viewModel.handleIntent(
                            FeedIntent.DeleteComment(comment = comment)
                        )
                    }
                    pendingDeleteComment = null
                },
                onDismissClick = {
                    pendingDeleteComment = null
                }
            )
        }

        // 发布评论
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
                        // 拦截输入框区域点击，避免点输入框也关闭
                    }
                ) {
                    FeedCommentBar(
                        value = commentContent,
                        onValueChange = {
                            commentContent = it
                        },
                        onSendClick = {
                            val postId = commentPostId
                            val content = commentContent.trim()

                            if (postId != null && content.isNotEmpty()) {
                                viewModel.handleIntent(
                                    FeedIntent.AddComment(
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
}

@Preview
@Composable
fun FeedScreenPreview() {
    val uuid = generateUUID()
    val user = FeedLineUser(
        id = uuid,
        name = "何聚敛",
        avatarUrl = "https://i.pravatar.cc/300"
    )

    val repository = object : FeedLineRepository by FeedRepositoryImpl() {
        override fun getFeedPosts() = flowOf(
            createFakeData()
        )
    }

    FeedScreen(
        viewModel = FeedLineViewModel(
            feedRepository = repository,
            currentUser = user
        )
    )
}
