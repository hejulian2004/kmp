package org.example.project.ui.screens.profilescreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.outlined.GridOn
import androidx.compose.material.icons.outlined.PersonSearch
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.example.project.presentation.state.ProfileUiState
import org.example.project.presentation.state.UiState
import org.example.project.presentation.viewmodel.ProfileIntent
import org.example.project.presentation.viewmodel.ProfileSingleEvent
import org.example.project.presentation.viewmodel.ProfileViewModel
import org.example.project.ui.components.profilescreen.Content.GridContent
import org.example.project.ui.components.profilescreen.ProfileInfoSection
import org.example.project.ui.components.profilescreen.TabItem
import org.example.project.ui.components.profilescreen.TabSwitchLayout
import org.example.project.ui.components.profilescreen.TopBar
import org.example.project.ui.components.profilescreen.TopBarSpan
import org.example.project.ui.components.profilescreen.UserCard
import org.example.project.ui.theme.InstagramTheme
import org.example.project.ui.theme.size
import org.example.project.ui.theme.spacing

object ProfileTestTags {
    // TopBar
    const val TOPBAR_ROOT           = "topbar_root"
    const val TOPBAR_BTN_ADD        = "topbar_btn_add"
    const val TOPBAR_BTN_MENTION    = "topbar_btn_mention"
    const val TOPBAR_BTN_MORE       = "topbar_btn_more"

    // 操作按钮行
    const val BTN_EDIT_PROFILE      = "btn_edit_profile"
    const val BTN_SHARE_PROFILE     = "btn_share_profile"
    const val BTN_TOGGLE_DISCOVER   = "btn_toggle_discover"

    // 发现用户
    const val DISCOVER_HEADER       = "discover_header"
    const val DISCOVER_BTN_ALL      = "discover_btn_all"
    fun userCard(userId: String)        = "user_card_$userId"
    fun userCardDismiss(userId: String) = "user_card_dismiss_$userId"
    fun userCardFollow(userId: String)  = "user_card_follow_$userId"

    // Tab
    fun tab(tabId: String)          = "tab_$tabId"

    // Loading
    const val LOADING_FOOTER        = "loading_footer"
}

@Composable
fun ProfileScreen(
    viewModel          : ProfileViewModel = viewModel { ProfileViewModel() },
    onNavigateToPost   : (String) -> Unit = {},
    onShowEditProfile  : () -> Unit = {},
    onShowShareProfile : () -> Unit = {},
    onNavigateToUser   : (String) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.singleEvent.collect { event ->
            when (event) {
                is ProfileSingleEvent.NavigateToUser   -> onNavigateToUser(event.userId)
                is ProfileSingleEvent.NavigateToPost   -> onNavigateToPost(event.postId)
                is ProfileSingleEvent.ShowEditProfile  -> onShowEditProfile()
                is ProfileSingleEvent.ShowShareProfile -> onShowShareProfile()
            }
        }
    }

    ProfileContent(
        uiState  = uiState,
        onIntent = viewModel::onIntent,
    )
}

@Composable
fun ProfileContent(
    uiState  : ProfileUiState,
    onIntent : (ProfileIntent) -> Unit,
) {
    val listState = rememberLazyListState()

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemScrollOffset }
            .collect { offset -> onIntent(ProfileIntent.ScrollOffsetChanged(offset)) }
    }

    BoxWithConstraints(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
    ) {
        ProfileBody(
            uiState   = uiState,
            listState = listState,
            onIntent  = onIntent,
        )

        AnimatedVisibility(
            visible = uiState.isTopBarVisible,
            enter   = fadeIn() + slideInVertically(),
            exit    = fadeOut() + slideOutVertically(),
        ) {
            val username = (uiState.profileState as? UiState.Success)?.data?.username ?: ""
            TopBar(
                title      = username,
                leftSpan1  = TopBarSpan(
                    icon    = Icons.Default.Add,
                    onPress = { onIntent(ProfileIntent.AddClicked) },
                    testTag = ProfileTestTags.TOPBAR_BTN_ADD,
                ),
                rightSpan1 = TopBarSpan(
                    icon    = Icons.Default.AlternateEmail,
                    onPress = { onIntent(ProfileIntent.MentionClicked) },
                    testTag = ProfileTestTags.TOPBAR_BTN_MENTION,
                ),
                rightSpan2 = TopBarSpan(
                    icon    = Icons.Default.MoreHoriz,
                    onPress = { onIntent(ProfileIntent.MoreOptionsClicked) },
                    testTag = ProfileTestTags.TOPBAR_BTN_MORE,
                ),
                modifier = Modifier
                    .height(MaterialTheme.size.topBarHeight)
                    .testTag(ProfileTestTags.TOPBAR_ROOT),
            )
        }
    }
}

@Composable
private fun ProfileBody(
    uiState   : ProfileUiState,
    listState : LazyListState,
    onIntent  : (ProfileIntent) -> Unit,
) {
    val spacing = MaterialTheme.spacing
    val size    = MaterialTheme.size

    val tabs = remember {
        listOf(
            TabItem("posts",  Icons.Outlined.GridOn,             "帖子"),
            TabItem("reels",  Icons.Outlined.VideoLibrary,       "Reels"),
            TabItem("tagged", Icons.AutoMirrored.Outlined.Label, "标记"),
        )
    }

    val currentSection = when (uiState.selectedTabId) {
        "posts"  -> uiState.postsSection
        "reels"  -> uiState.reelsSection
        "tagged" -> uiState.taggedSection
        else     -> null
    }
    val currentData   = (currentSection as? UiState.Success)?.data
    val isLoadingMore = currentData?.isLoadingMore ?: false
    val hasMore       = currentData?.hasMore ?: false

    val shouldLoadMore by remember(hasMore, isLoadingMore) {
        derivedStateOf {
            if (!hasMore || isLoadingMore) return@derivedStateOf false
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            val total       = listState.layoutInfo.totalItemsCount
            total > 0 && lastVisible >= total - 2
        }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) onIntent(ProfileIntent.LoadMore)
    }

    LazyColumn(
        state    = listState,
        modifier = Modifier.fillMaxWidth(),
    ) {

        item(key = "topbar_spacer") {
            Spacer(modifier = Modifier.height(size.topBarHeight))
        }

        item(key = "profile_info") {
            val state = uiState.profileState
            if (state is UiState.Success) {
                ProfileInfoSection(
                    avatarUrl        = state.data.avatarUrl,
                    username         = state.data.username,
                    postCount        = state.data.postCount,
                    followerCount    = state.data.followerCount,
                    followingCount   = state.data.followingCount,
                    signature        = state.data.signature,
                    onAvatarClick    = { onIntent(ProfileIntent.AvatarClicked) },
                    onPostClick      = { onIntent(ProfileIntent.PostCountClicked) },
                    onFollowerClick  = { onIntent(ProfileIntent.FollowerClicked) },
                    onFollowingClick = { onIntent(ProfileIntent.FollowingClicked) },
                    onSignatureClick = { onIntent(ProfileIntent.SignatureClicked) },
                )
            } else {
                ProfileInfoSection()
            }
        }

        item(key = "action_buttons") {
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.md, vertical = spacing.sm),
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                OutlinedButton(
                    onClick  = { onIntent(ProfileIntent.EditProfileClicked) },
                    shape    = RoundedCornerShape(size.buttonRadius),
                    modifier = Modifier
                        .weight(1f)
                        .testTag(ProfileTestTags.BTN_EDIT_PROFILE),
                ) {
                    Text(
                        text  = "编辑主页",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                OutlinedButton(
                    onClick  = { onIntent(ProfileIntent.ShareProfileClicked) },
                    shape    = RoundedCornerShape(size.buttonRadius),
                    modifier = Modifier
                        .weight(1f)
                        .testTag(ProfileTestTags.BTN_SHARE_PROFILE),
                ) {
                    Text(
                        text  = "分享主页",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                OutlinedButton(
                    onClick        = { onIntent(ProfileIntent.ToggleDiscoverSection) },
                    shape          = RoundedCornerShape(size.buttonRadius),
                    contentPadding = PaddingValues(spacing.xs),
                    modifier       = Modifier
                        .height(size.iconLg + spacing.md)
                        .testTag(ProfileTestTags.BTN_TOGGLE_DISCOVER),
                ) {
                    androidx.compose.material3.Icon(
                        imageVector        = Icons.Outlined.PersonSearch,
                        contentDescription = if (uiState.isDiscoverVisible) "隐藏推荐" else "显示推荐",
                        modifier           = Modifier.height(size.iconMd),
                        tint               = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }

        if (uiState.isDiscoverVisible) {
            item(key = "discover_header") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing.md, vertical = spacing.xs)
                        .testTag(ProfileTestTags.DISCOVER_HEADER),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically,
                ) {
                    Text(
                        text  = "发现用户",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text     = "全部",
                        style    = MaterialTheme.typography.labelMedium,
                        color    = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .testTag(ProfileTestTags.DISCOVER_BTN_ALL)
                            .clickable { onIntent(ProfileIntent.DiscoverAllClicked) },
                    )
                }
            }

            item(key = "discover_list") {
                val state = uiState.discoverState
                if (state is UiState.Success) {
                    LazyRow(
                        contentPadding        = PaddingValues(horizontal = spacing.md),
                        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                    ) {
                        items(items = state.data, key = { it.userId }) { user ->
                            UserCard(
                                avatarUrl    = user.avatarUrl,
                                username     = user.username,
                                extraInfo    = user.extraInfo,
                                modifier     = Modifier.testTag(
                                    ProfileTestTags.userCard(user.userId)
                                ),
                                onClick      = { onIntent(ProfileIntent.UserCardClicked(user.userId)) },
                                onDismiss    = { onIntent(ProfileIntent.UserCardDismissed(user.userId)) },
                                dismissTestTag = ProfileTestTags.userCardDismiss(user.userId),
                                bottomAction = {
                                    Button(
                                        onClick  = { onIntent(ProfileIntent.UserCardClicked(user.userId)) },
                                        shape    = RoundedCornerShape(size.buttonRadius),
                                        colors   = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor   = MaterialTheme.colorScheme.onPrimary,
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag(ProfileTestTags.userCardFollow(user.userId)), // ✅
                                    ) {
                                        Text(
                                            text  = "关注",
                                            style = MaterialTheme.typography.labelMedium,
                                        )
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }

        item(key = "tab_content") {
            TabSwitchLayout(
                tabs          = tabs,
                selectedTabId = uiState.selectedTabId,
                onTabSelected = { id -> onIntent(ProfileIntent.TabSelected(id)) },
            ) { tabId ->
                val section = when (tabId) {
                    "posts"  -> uiState.postsSection
                    "reels"  -> uiState.reelsSection
                    "tagged" -> uiState.taggedSection
                    else     -> null
                }
                val data = (section as? UiState.Success)?.data
                GridContent(
                    posts           = data?.posts ?: emptyList(),
                    isLoadingMore   = isLoadingMore,
                    hasMore         = hasMore,
                    onLoadMore      = { onIntent(ProfileIntent.LoadMore) },
                    onItemClick     = { id -> onIntent(ProfileIntent.PostClicked(id)) },
                    onItemLongClick = { id -> onIntent(ProfileIntent.PostLongClicked(id)) },
                    modifier        = Modifier
                        .padding(spacing.xs)
                        .height(520.dp),
                )
            }
        }

        if (isLoadingMore) {
            item(key = "loading_footer") {
                Box(
                    modifier         = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag(ProfileTestTags.LOADING_FOOTER),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color       = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    )
                }
            }
        }

        item(key = "extra_content") {
            Box(modifier = Modifier.fillMaxWidth().height(size.navigationBarHeight + spacing.xl))
        }
    }
}

@Preview
@Composable
fun ProfileContentPreview() {
    InstagramTheme {
        ProfileContent(
            uiState  = ProfileUiState(),
            onIntent = {},
        )
    }
}
