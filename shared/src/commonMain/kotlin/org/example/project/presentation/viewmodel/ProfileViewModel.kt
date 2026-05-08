package org.example.project.presentation.viewmodel

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
import org.example.project.domain.ContentThumbnailData
import org.example.project.domain.DiscoverUser
import org.example.project.domain.PostType
import org.example.project.domain.ProfileUser
import org.example.project.presentation.state.PostsSection
import org.example.project.presentation.state.ProfileUiState
import org.example.project.presentation.state.UiState

class ProfileViewModel : ViewModel() {

    // StateFlow 持有当前页面状态
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    // SingleEvent 用于一次性事件
    private val _singleEvent = MutableSharedFlow<ProfileSingleEvent>()
    val singleEvent: SharedFlow<ProfileSingleEvent> = _singleEvent.asSharedFlow()

    @OptIn(ExperimentalStdlibApi::class)
    fun onIntent(intent: ProfileIntent) {
        when (intent) {
            is ProfileIntent.AddClicked -> {
                viewModelScope.launch { _singleEvent.emit(ProfileSingleEvent.ShowEditProfile) }
            }
            is ProfileIntent.MentionClicked -> {
                // 处理消息入口
            }
            is ProfileIntent.MoreOptionsClicked -> {
                // 弹出更多操作菜单
            }
            is ProfileIntent.TitleToggled -> {
                _uiState.update { it.copy(isTitleExpanded = intent.isExpanded) }
            }

            is ProfileIntent.AvatarClicked -> {
                // 触发头像查看/编辑
            }
            is ProfileIntent.PostCountClicked -> {
                viewModelScope.launch { _singleEvent.emit(ProfileSingleEvent.NavigateToPost("some_post_id")) }
            }
            is ProfileIntent.FollowerClicked,
            is ProfileIntent.FollowingClicked -> {
                // 可以触发导航
            }

            is ProfileIntent.EditProfileClicked -> {
                viewModelScope.launch { _singleEvent.emit(ProfileSingleEvent.ShowEditProfile) }
            }
            is ProfileIntent.ShareProfileClicked -> {
                viewModelScope.launch { _singleEvent.emit(ProfileSingleEvent.ShowShareProfile) }
            }
            is ProfileIntent.ToggleDiscoverSection -> {
                _uiState.update { it.copy(isDiscoverVisible = !it.isDiscoverVisible) }
            }
            is ProfileIntent.DiscoverAllClicked -> {
            }
            is ProfileIntent.UserCardClicked -> {
                viewModelScope.launch { _singleEvent.emit(ProfileSingleEvent.NavigateToUser(intent.userId)) }
            }
            is ProfileIntent.UserCardDismissed -> {
                _uiState.update { state ->
                    val updated = (state.discoverState as? UiState.Success)?.data.orEmpty()
                        .filterNot { it.userId == intent.userId }
                    state.copy(discoverState = UiState.Success(updated))
                }
            }

            is ProfileIntent.TabSelected -> {
                _uiState.update { it.copy(selectedTabId = intent.tabId) }
            }
            is ProfileIntent.PostClicked -> {
                viewModelScope.launch { _singleEvent.emit(ProfileSingleEvent.NavigateToPost(intent.id)) }
            }
            is ProfileIntent.LoadMore -> {
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

            is ProfileIntent.ScrollOffsetChanged -> {
                val isTopBarVisible = intent.scrollOffset < 200
                _uiState.update { it.copy(isTopBarVisible = isTopBarVisible) }
            }

            is ProfileIntent.CreatePostClicked -> {
                viewModelScope.launch { _singleEvent.emit(ProfileSingleEvent.NavigateToPost("new")) }
            }
            is ProfileIntent.CreateReelClicked -> {

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

sealed interface ProfileIntent {

    // ==================================================
    // TopBar
    // ==================================================

    /** 左侧 "+" 按钮 */
    data object AddClicked : ProfileIntent

    /** 标题（userId）点击，驱动下拉展开/收起 */
    data class TitleToggled(val isExpanded: Boolean) : ProfileIntent

    /** 右侧 "@" 按钮（提及 / 消息入口） */
    data object MentionClicked : ProfileIntent

    /** 右侧 "—" 按钮（更多选项） */
    data object MoreOptionsClicked : ProfileIntent

    // ==================================================
    // ProfileInfoSection
    // ==================================================

    /** 头像点击（查看大图 / 修改头像） */
    data object AvatarClicked : ProfileIntent

    /** 帖子数点击，导航至帖子列表 */
    data object PostCountClicked : ProfileIntent

    /** 粉丝数点击，导航至粉丝列表 */
    data object FollowerClicked : ProfileIntent

    /** 关注数点击，导航至关注列表 */
    data object FollowingClicked : ProfileIntent

    /** 点击已有签名（进入编辑） */
    data object SignatureClicked : ProfileIntent

    /** 点击"添加兴趣" */
    data object AddInterestClicked : ProfileIntent

    // ==================================================
    // 操作按钮行（编辑主页 / 分享主页 / 隐藏推荐）
    // ==================================================

    /** 编辑主页按钮 */
    data object EditProfileClicked : ProfileIntent

    /** 分享主页按钮 */
    data object ShareProfileClicked : ProfileIntent

    /**
     * 切换推荐用户区域的显示状态。
     */
    data object ToggleDiscoverSection : ProfileIntent

    // ==================================================
    // 发现用户区域
    // ==================================================

    /** "全部" 入口，导航至完整推荐列表页 */
    data object DiscoverAllClicked : ProfileIntent

    /** 点击某张 UserCard，导航至该用户主页 */
    data class UserCardClicked(val userId: String) : ProfileIntent

    /** 关闭（X）某张 UserCard，将该用户从推荐列表移除 */
    data class UserCardDismissed(val userId: String) : ProfileIntent

    // ==================================================
    // Tab 切换
    // ==================================================

    /** 用户切换 Tab（帖子 / Reels / 标记） */
    data class TabSelected(val tabId: String) : ProfileIntent

    // ==================================================
    // GridContent — 内容列表
    // ==================================================

    /** 点击某个内容缩略图 */
    data class PostClicked(val id: String) : ProfileIntent

    /** 长按某个内容缩略图 */
    data class PostLongClicked(val id: String) : ProfileIntent

    /** 列表滚动到底部，触发分页加载 */
    data object LoadMore : ProfileIntent

    // ==================================================
    // Empty State CTA
    // ==================================================

    /** PostEmptyState — "创建" 按钮 */
    data object CreatePostClicked : ProfileIntent

    /** ReelsEmptyState — "创建首条 Reels" 按钮 */
    data object CreateReelClicked : ProfileIntent

    // ==================================================
    // 页面级滚动行为（LazyColumn 驱动 TopBar 显隐）
    // ==================================================

    /**
     * 滚动偏移变化时由 UI 上报，ViewModel 根据阈值决定是否更新
     */
    data class ScrollOffsetChanged(val scrollOffset: Int) : ProfileIntent
}

sealed class ProfileSingleEvent {
    data class NavigateToUser(val userId: String) : ProfileSingleEvent()
    data class NavigateToPost(val postId: String) : ProfileSingleEvent()
    object ShowEditProfile : ProfileSingleEvent()
    object ShowShareProfile : ProfileSingleEvent()
}

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

