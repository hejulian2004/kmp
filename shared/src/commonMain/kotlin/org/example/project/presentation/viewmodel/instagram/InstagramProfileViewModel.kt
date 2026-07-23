package org.example.project.presentation.viewmodel.instagram

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.domain.model.instagram.ContentThumbnailData
import org.example.project.domain.model.instagram.DiscoverUser
import org.example.project.domain.model.instagram.PostType
import org.example.project.domain.model.instagram.ProfileUser
import org.example.project.presentation.state.UiState
import org.example.project.presentation.state.instagram.InstagramProfileUiState
import org.example.project.presentation.state.instagram.PostsSection

class InstagramProfileViewModel : ViewModel() {

    // StateFlow 持有当前页面状态
    private val _uiState = MutableStateFlow(InstagramProfileUiState())
    val uiState: StateFlow<InstagramProfileUiState> = _uiState.asStateFlow()

    // SingleEvent 用于一次性事件
    private val _singleEvent = MutableSharedFlow<InstagramProfileSingleEvent>()
    val singleEvent: SharedFlow<InstagramProfileSingleEvent> = _singleEvent.asSharedFlow()

    @OptIn(ExperimentalStdlibApi::class)
    fun onIntent(intent: InstagramProfileIntent) {
        when (intent) {
            is InstagramProfileIntent.AddClicked -> {
                viewModelScope.launch { _singleEvent.emit(InstagramProfileSingleEvent.ShowEditProfile) }
            }
            is InstagramProfileIntent.MentionClicked -> {
                // 处理消息入口
            }
            is InstagramProfileIntent.MoreOptionsClicked -> {
                // 弹出更多操作菜单
            }
            is InstagramProfileIntent.TitleToggled -> {
                _uiState.update { it.copy(isTitleExpanded = intent.isExpanded) }
            }

            is InstagramProfileIntent.AvatarClicked -> {
                // 触发头像查看/编辑
            }
            is InstagramProfileIntent.PostCountClicked -> {
                viewModelScope.launch { _singleEvent.emit(InstagramProfileSingleEvent.NavigateToPost("some_post_id")) }
            }
            is InstagramProfileIntent.FollowerClicked,
            is InstagramProfileIntent.FollowingClicked -> {
                // 可以触发导航
            }

            is InstagramProfileIntent.EditProfileClicked -> {
                viewModelScope.launch { _singleEvent.emit(InstagramProfileSingleEvent.ShowEditProfile) }
            }
            is InstagramProfileIntent.ShareProfileClicked -> {
                viewModelScope.launch { _singleEvent.emit(InstagramProfileSingleEvent.ShowShareProfile) }
            }
            is InstagramProfileIntent.ToggleDiscoverSection -> {
                _uiState.update { it.copy(isDiscoverVisible = !it.isDiscoverVisible) }
            }
            is InstagramProfileIntent.DiscoverAllClicked -> {
            }
            is InstagramProfileIntent.UserCardClicked -> {
                viewModelScope.launch { _singleEvent.emit(InstagramProfileSingleEvent.NavigateToUser(intent.userId)) }
            }
            is InstagramProfileIntent.UserCardDismissed -> {
                _uiState.update { state ->
                    val updated = (state.discoverState as? UiState.Success)?.data.orEmpty()
                        .filterNot { it.userId == intent.userId }
                    state.copy(discoverState = UiState.Success(updated))
                }
            }

            is InstagramProfileIntent.TabSelected -> {
                _uiState.update { it.copy(selectedTabId = intent.tabId) }
            }
            is InstagramProfileIntent.PostClicked -> {
                viewModelScope.launch { _singleEvent.emit(InstagramProfileSingleEvent.NavigateToPost(intent.id)) }
            }
            is InstagramProfileIntent.LoadMore -> {
                val currentState = _uiState.value
                val currentSection = when (currentState.selectedTabId) {
                    "posts"  -> currentState.postsSection
                    "reels"  -> currentState.reelsSection
                    "tagged" -> currentState.taggedSection
                    else     -> null
                }
                val sectionData = (currentSection as? UiState.Success)?.data ?: return
                if (sectionData.isLoadingMore || !sectionData.hasMore) return
                viewModelScope.launch {
                    updateSection(currentState.selectedTabId) { it.copy(isLoadingMore = true) }
                    delay(800)
                    val newPosts = sectionData.posts + generateMockPosts(12)
                    updateSection(currentState.selectedTabId) {
                        it.copy(
                            posts         = newPosts,
                            isLoadingMore = false,
                            hasMore       = newPosts.size < 36,
                        )
                    }
                }
            }

            is InstagramProfileIntent.ScrollOffsetChanged -> {
                val isTopBarVisible = intent.scrollOffset < 200
                _uiState.update { it.copy(isTopBarVisible = isTopBarVisible) }
            }

            is InstagramProfileIntent.CreatePostClicked -> {
                viewModelScope.launch { _singleEvent.emit(InstagramProfileSingleEvent.NavigateToPost("new")) }
            }
            is InstagramProfileIntent.CreateReelClicked -> {

            }
            else -> {}
        }
    }

    fun loadMockData() {
        val mockPosts = UiState.Success(
            PostsSection(
                posts = generateMockPosts(10),
                hasMore = true,
            )
        )
        _uiState.update { state ->
            state.copy(
                profileState = UiState.Success(
                    ProfileUser(
                        userId = "1",
                        username = "Alice",
                        avatarUrl = "https://picsum.photos/seed/300/300",
                        signature = "Hello world!",
                        postCount = "12",
                        followerCount = "345",
                        followingCount = "67"
                    )
                ),
                discoverState = UiState.Success(generateMockRecommendedUsers(10)),
                postsSection  = mockPosts,
                reelsSection  = mockPosts,
                taggedSection = mockPosts,
            )
        }
    }
    init {
        loadMockData()
    }
    private fun updateSection(tabId: String, transform: (PostsSection) -> PostsSection) {
        _uiState.update { state ->
            when (tabId) {
                "posts" -> {
                    val current = (state.postsSection as? UiState.Success)?.data ?: return@update state
                    state.copy(postsSection = UiState.Success(transform(current)))
                }
                "reels" -> {
                    val current = (state.reelsSection as? UiState.Success)?.data ?: return@update state
                    state.copy(reelsSection = UiState.Success(transform(current)))
                }
                "tagged" -> {
                    val current = (state.taggedSection as? UiState.Success)?.data ?: return@update state
                    state.copy(taggedSection = UiState.Success(transform(current)))
                }
                else -> state
            }
        }
    }
}

typealias ProfileViewModel = InstagramProfileViewModel

sealed interface InstagramProfileIntent {

    // ==================================================
    // TopBar
    // ==================================================

    /** 左侧 "+" 按钮 */
    data object AddClicked : InstagramProfileIntent

    /** 标题（userId）点击，驱动下拉展开/收起 */
    data class TitleToggled(val isExpanded: Boolean) : InstagramProfileIntent

    /** 右侧 "@" 按钮（提及 / 消息入口） */
    data object MentionClicked : InstagramProfileIntent

    /** 右侧 "—" 按钮（更多选项） */
    data object MoreOptionsClicked : InstagramProfileIntent

    // ==================================================
    // ProfileInfoSection
    // ==================================================

    /** 头像点击（查看大图 / 修改头像） */
    data object AvatarClicked : InstagramProfileIntent

    /** 帖子数点击，导航至帖子列表 */
    data object PostCountClicked : InstagramProfileIntent

    /** 粉丝数点击，导航至粉丝列表 */
    data object FollowerClicked : InstagramProfileIntent

    /** 关注数点击，导航至关注列表 */
    data object FollowingClicked : InstagramProfileIntent

    /** 点击已有签名（进入编辑） */
    data object SignatureClicked : InstagramProfileIntent

    /** 点击"添加兴趣" */
    data object AddInterestClicked : InstagramProfileIntent

    // ==================================================
    // 操作按钮行（编辑主页 / 分享主页 / 隐藏推荐）
    // ==================================================

    /** 编辑主页按钮 */
    data object EditProfileClicked : InstagramProfileIntent

    /** 分享主页按钮 */
    data object ShareProfileClicked : InstagramProfileIntent

    /**
     * 切换推荐用户区域的显示状态。
     */
    data object ToggleDiscoverSection : InstagramProfileIntent

    // ==================================================
    // 发现用户区域
    // ==================================================

    /** "全部" 入口，导航至完整推荐列表页 */
    data object DiscoverAllClicked : InstagramProfileIntent

    /** 点击某张 UserCard，导航至该用户主页 */
    data class UserCardClicked(val userId: String) : InstagramProfileIntent

    /** 关闭（X）某张 UserCard，将该用户从推荐列表移除 */
    data class UserCardDismissed(val userId: String) : InstagramProfileIntent

    // ==================================================
    // Tab 切换
    // ==================================================

    /** 用户切换 Tab（帖子 / Reels / 标记） */
    data class TabSelected(val tabId: String) : InstagramProfileIntent

    // ==================================================
    // GridContent — 内容列表
    // ==================================================

    /** 点击某个内容缩略图 */
    data class PostClicked(val id: String) : InstagramProfileIntent

    /** 长按某个内容缩略图 */
    data class PostLongClicked(val id: String) : InstagramProfileIntent

    /** 列表滚动到底部，触发分页加载 */
    data object LoadMore : InstagramProfileIntent

    // ==================================================
    // Empty State CTA
    // ==================================================

    /** PostEmptyState — "创建" 按钮 */
    data object CreatePostClicked : InstagramProfileIntent

    /** ReelsEmptyState — "创建首条 Reels" 按钮 */
    data object CreateReelClicked : InstagramProfileIntent

    // ==================================================
    // 页面级滚动行为（LazyColumn 驱动 TopBar 显隐）
    // ==================================================

    /**
     * 滚动偏移变化时由 UI 上报，ViewModel 根据阈值决定是否更新
     */
    data class ScrollOffsetChanged(val scrollOffset: Int) : InstagramProfileIntent
}

typealias ProfileIntent = InstagramProfileIntent

sealed class InstagramProfileSingleEvent {
    data class NavigateToUser(val userId: String) : InstagramProfileSingleEvent()
    data class NavigateToPost(val postId: String) : InstagramProfileSingleEvent()
    data object ShowEditProfile : InstagramProfileSingleEvent()
    data object ShowShareProfile : InstagramProfileSingleEvent()
}

typealias ProfileSingleEvent = InstagramProfileSingleEvent

fun generateMockPosts(count: Int = 12): List<ContentThumbnailData> {
    val types = PostType.entries
    val durations = listOf("0:30", "1:23", "2:05", "0:45", null, null, null)
    return List(count) { index ->
        val type = types.random()
        ContentThumbnailData(
            id = "mock_${index}_${(0..9999).random()}",
            imageUrl = "https://picsum.photos/seed/${(0..1000).random()}/300/300",
            type = type,
            duration = if (type == PostType.VIDEO || type == PostType.REEL) durations.random() else null,
        )
    }
}

fun generateMockRecommendedUsers(count: Int = 12): List<DiscoverUser> {
    return List(count) { index ->
        DiscoverUser(
            userId = "user_${(0..9999).random()}",
            username = "user_${(0..9999).random()}",
            avatarUrl = "https://picsum.photos/seed/${(0..1000).random()}/300/300",
            extraInfo = "Kotlin Enthusiast",
        )
    }
}
