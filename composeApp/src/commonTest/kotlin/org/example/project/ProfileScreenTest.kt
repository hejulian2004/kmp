package org.example.project

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runComposeUiTest
import org.example.project.model.DiscoverUser
import org.example.project.model.ProfileUser
import org.example.project.presentation.state.PostsSection
import org.example.project.presentation.state.ProfileUiState
import org.example.project.presentation.state.UiState
import org.example.project.presentation.viewmodel.ProfileIntent
import org.example.project.presentation.viewmodel.generateMockPosts
import org.example.project.ui.screens.profilescreen.ProfileContent
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class ProfileScreenTest {

    // 辅助函数
    private fun baseState(
        isDiscoverVisible : Boolean = false,
        selectedTabId     : String  = "posts",
        isTopBarVisible   : Boolean = false,
    ) = ProfileUiState(
        profileState      = UiState.Success(
            ProfileUser(
                username = "test_user",
                postCount = "42",
                followerCount = "1000",
                followingCount = "200",
                avatarUrl = "",
                signature = "这是一段签名",
                userId = "1",
            )
        ),
        isDiscoverVisible = isDiscoverVisible,
        selectedTabId     = selectedTabId,
        isTopBarVisible   = isTopBarVisible,
    )

    private fun collectIntents(
        state   : ProfileUiState = baseState(),
        block   : ProfileScreenTest.(intents: MutableList<ProfileIntent>) -> Unit,
    ) {
        val intents = mutableListOf<ProfileIntent>()
        block(intents)
    }

    // TopBar

    @Test
    fun TopBar_isTopBarVisible为true_显示TopBar() = runComposeUiTest {
        setContent {
            ProfileContent(uiState = baseState(isTopBarVisible = true), onIntent = {})
        }
        onNodeWithTag("topbar_root").assertIsDisplayed()
    }

    @Test
    fun TopBar_isTopBarVisible为false_不显示TopBar() = runComposeUiTest {
        setContent {
            ProfileContent(uiState = baseState(isTopBarVisible = false), onIntent = {})
        }
        onNodeWithTag("topbar_root").assertDoesNotExist()
    }

    @Test
    fun TopBar_点击加号_触发AddClicked() = runComposeUiTest {
        val intents = mutableListOf<ProfileIntent>()
        setContent {
            ProfileContent(
                uiState  = baseState(isTopBarVisible = true),
                onIntent = { intents.add(it) },
            )
        }
        onNodeWithTag("topbar_btn_add").performClick()
        assertTrue(intents.contains(ProfileIntent.AddClicked))
    }

    @Test
    fun TopBar_点击At图标_触发MentionClicked() = runComposeUiTest {
        val intents = mutableListOf<ProfileIntent>()
        setContent {
            ProfileContent(
                uiState  = baseState(isTopBarVisible = true),
                onIntent = { intents.add(it) },
            )
        }
        onNodeWithTag("topbar_btn_mention").performClick()
        assertTrue(intents.contains(ProfileIntent.MentionClicked))
    }

    @Test
    fun TopBar_点击更多_触发MoreOptionsClicked() = runComposeUiTest {
        val intents = mutableListOf<ProfileIntent>()
        setContent {
            ProfileContent(
                uiState  = baseState(isTopBarVisible = true),
                onIntent = { intents.add(it) },
            )
        }
        onNodeWithTag("topbar_btn_more").performClick()
        assertTrue(intents.contains(ProfileIntent.MoreOptionsClicked))
    }

    // ProfileInfoSection

    @Test
    fun ProfileInfo_点击头像_触发AvatarClicked() = runComposeUiTest {
        val intents = mutableListOf<ProfileIntent>()
        setContent {
            ProfileContent(uiState = baseState(), onIntent = { intents.add(it) })
        }
        onNodeWithTag("profile_avatar").performClick()
        assertTrue(intents.contains(ProfileIntent.AvatarClicked))
    }

    @Test
    fun ProfileInfo_点击帖子数_触发PostCountClicked() = runComposeUiTest {
        val intents = mutableListOf<ProfileIntent>()
        setContent {
            ProfileContent(uiState = baseState(), onIntent = { intents.add(it) })
        }
        onNodeWithTag("profile_post_count").performClick()
        assertTrue(intents.contains(ProfileIntent.PostCountClicked))
    }

    @Test
    fun ProfileInfo_点击粉丝数_触发FollowerClicked() = runComposeUiTest {
        val intents = mutableListOf<ProfileIntent>()
        setContent {
            ProfileContent(uiState = baseState(), onIntent = { intents.add(it) })
        }
        onNodeWithTag("profile_follower_count").performClick()
        assertTrue(intents.contains(ProfileIntent.FollowerClicked))
    }

    @Test
    fun ProfileInfo_点击关注数_触发FollowingClicked() = runComposeUiTest {
        val intents = mutableListOf<ProfileIntent>()
        setContent {
            ProfileContent(uiState = baseState(), onIntent = { intents.add(it) })
        }
        onNodeWithTag("profile_following_count").performClick()
        assertTrue(intents.contains(ProfileIntent.FollowingClicked))
    }

    @Test
    fun ProfileInfo_点击签名_触发SignatureClicked() = runComposeUiTest {
        val intents = mutableListOf<ProfileIntent>()
        setContent {
            ProfileContent(uiState = baseState(), onIntent = { intents.add(it) })
        }
        onNodeWithTag("profile_signature").performClick()
        assertTrue(intents.contains(ProfileIntent.SignatureClicked))
    }

    // 操作按钮行

    @Test
    fun ActionRow_三个按钮默认可见() = runComposeUiTest {
        setContent {
            ProfileContent(uiState = baseState(), onIntent = {})
        }
        onNodeWithTag("btn_edit_profile").assertIsDisplayed()
        onNodeWithTag("btn_share_profile").assertIsDisplayed()
        onNodeWithTag("btn_toggle_discover").assertIsDisplayed()
    }

    @Test
    fun ActionRow_点击编辑主页_触发EditProfileClicked() = runComposeUiTest {
        val intents = mutableListOf<ProfileIntent>()
        setContent {
            ProfileContent(uiState = baseState(), onIntent = { intents.add(it) })
        }
        onNodeWithTag("btn_edit_profile").performClick()
        assertTrue(intents.contains(ProfileIntent.EditProfileClicked))
    }

    @Test
    fun ActionRow_点击分享主页_触发ShareProfileClicked() = runComposeUiTest {
        val intents = mutableListOf<ProfileIntent>()
        setContent {
            ProfileContent(uiState = baseState(), onIntent = { intents.add(it) })
        }
        onNodeWithTag("btn_share_profile").performClick()
        assertTrue(intents.contains(ProfileIntent.ShareProfileClicked))
    }

    @Test
    fun ActionRow_点击发现图标_触发ToggleDiscoverSection() = runComposeUiTest {
        val intents = mutableListOf<ProfileIntent>()
        setContent {
            ProfileContent(uiState = baseState(), onIntent = { intents.add(it) })
        }
        onNodeWithTag("btn_toggle_discover").performClick()
        assertTrue(intents.contains(ProfileIntent.ToggleDiscoverSection))
    }

    // 发现用户区域

    @Test
    fun Discover_isDiscoverVisible为false_区域不存在() = runComposeUiTest {
        setContent {
            ProfileContent(uiState = baseState(isDiscoverVisible = false), onIntent = {})
        }
        onNodeWithTag("discover_header").assertDoesNotExist()
    }

    @Test
    fun Discover_isDiscoverVisible为true_区域可见() = runComposeUiTest {
        setContent {
            ProfileContent(uiState = baseState(isDiscoverVisible = true), onIntent = {})
        }
        onNodeWithTag("discover_header").assertIsDisplayed()
        onNodeWithTag("discover_btn_all").assertIsDisplayed()
    }

    @Test
    fun Discover_点击全部_触发DiscoverAllClicked() = runComposeUiTest {
        val intents = mutableListOf<ProfileIntent>()
        setContent {
            ProfileContent(
                uiState  = baseState(isDiscoverVisible = true),
                onIntent = { intents.add(it) },
            )
        }
        onNodeWithTag("discover_btn_all").performClick()
        assertTrue(intents.contains(ProfileIntent.DiscoverAllClicked))
    }

    @Test
    fun Discover_点击UserCard_触发UserCardClicked() = runComposeUiTest {
        val intents  = mutableListOf<ProfileIntent>()
        val fakeUser = DiscoverUser(
            userId = "user_001", username = "alice",
            avatarUrl = "",
            extraInfo = "check"
        )
        setContent {
            ProfileContent(
                uiState  = baseState(isDiscoverVisible = true).copy(
                    discoverState = UiState.Success(listOf(fakeUser))
                ),
                onIntent = { intents.add(it) },
            )
        }
        onNodeWithTag("user_card_user_001").performClick()
        assertTrue(intents.any { it is ProfileIntent.UserCardClicked && it.userId == "user_001" })
    }

    @Test
    fun Discover_关闭UserCard_触发UserCardDismissed() = runComposeUiTest {
        val intents  = mutableListOf<ProfileIntent>()
        val fakeUser = DiscoverUser(
            userId = "user_001", username = "alice",
            avatarUrl = "",
            extraInfo = "check"
        )
        setContent {
            ProfileContent(
                uiState  = baseState(isDiscoverVisible = true).copy(
                    discoverState = UiState.Success(listOf(fakeUser))
                ),
                onIntent = { intents.add(it) },
            )
        }
        onNodeWithTag("user_card_dismiss_user_001").performClick()
        assertTrue(intents.any { it is ProfileIntent.UserCardDismissed && it.userId == "user_001" })
    }

    // Tab 切换

    @Test
    fun Tab_默认选中posts() = runComposeUiTest {
        setContent {
            ProfileContent(uiState = baseState(selectedTabId = "posts"), onIntent = {})
        }
        onNodeWithTag("tab_posts_selected").assertIsDisplayed()
    }

    @Test
    fun Tab_点击Reels_触发TabSelected_reels() = runComposeUiTest {
        val intents = mutableListOf<ProfileIntent>()
        setContent {
            ProfileContent(uiState = baseState(), onIntent = { intents.add(it) })
        }
        onNodeWithTag("tab_reels").performClick()
        assertTrue(intents.any { it is ProfileIntent.TabSelected && it.tabId == "reels" })
    }

    @Test
    fun Tab_点击标记_触发TabSelected_tagged() = runComposeUiTest {
        val intents = mutableListOf<ProfileIntent>()
        setContent {
            ProfileContent(uiState = baseState(), onIntent = { intents.add(it) })
        }
        onNodeWithTag("tab_tagged").performClick()
        assertTrue(intents.any { it is ProfileIntent.TabSelected && it.tabId == "tagged" })
    }

    // GridContent — 内容列表

    @Test
    fun Grid_点击帖子缩略图_触发PostClicked() = runComposeUiTest {
        val intents   = mutableListOf<ProfileIntent>()
        setContent {
            ProfileContent(
                uiState  = baseState().copy(
                    postsSection =UiState.Success(PostsSection(
                        posts = generateMockPosts(10)
                    ))
                ),
                onIntent = { intents.add(it) },
            )
        }
        onNodeWithTag("grid_item_post_001").performClick()
        assertTrue(intents.any { it is ProfileIntent.PostClicked && it.id == "post_001" })
    }

    @Test
    fun Grid_长按帖子缩略图_触发PostLongClicked() = runComposeUiTest {
        val intents  = mutableListOf<ProfileIntent>()
        val fakePost = generateMockPosts(1)
        setContent {
            ProfileContent(
                uiState  = baseState().copy(
                    postsSection = UiState.Success(PostsSection(posts = listOf()))
                ),
                onIntent = { intents.add(it) },
            )
        }
        onNodeWithTag("grid_item_post_001").performTouchInput { longClick() }
        assertTrue(intents.any { it is ProfileIntent.PostLongClicked && it.id == "post_001" })
    }

    // Empty State CTA

    @Test
    fun EmptyState_posts_点击创建_触发CreatePostClicked() = runComposeUiTest {
        val intents = mutableListOf<ProfileIntent>()
        setContent {
            ProfileContent(
                uiState  = baseState().copy(
                    postsSection = UiState.Success(PostsSection(posts = emptyList()))
                ),
                onIntent = { intents.add(it) },
            )
        }
        onNodeWithTag("empty_btn_create_post").performClick()
        assertTrue(intents.contains(ProfileIntent.CreatePostClicked))
    }

    @Test
    fun EmptyState_reels_点击创建首条Reels_触发CreateReelClicked() = runComposeUiTest {
        val intents = mutableListOf<ProfileIntent>()
        setContent {
            ProfileContent(
                uiState  = baseState(selectedTabId = "reels").copy(
                    reelsSection = UiState.Success(PostsSection(posts = emptyList()))
                ),
                onIntent = { intents.add(it) },
            )
        }
        onNodeWithTag("empty_btn_create_reel").performClick()
        assertTrue(intents.contains(ProfileIntent.CreateReelClicked))
    }

    // Loading 状态

    @Test
    fun Loading_profileState为Loading_页面不崩溃且按钮行可见() = runComposeUiTest {
        setContent {
            ProfileContent(
                uiState  = ProfileUiState(profileState = UiState.Loading),
                onIntent = {},
            )
        }
        onNodeWithTag("btn_edit_profile").assertIsDisplayed()
    }
}