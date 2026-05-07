package org.example.project.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.GridOn
import androidx.compose.material.icons.outlined.PersonPinCircle
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import org.example.project.ui.components.profilescreen.Content
import org.example.project.ui.components.profilescreen.Content.ContentThumbnail
import org.example.project.ui.components.profilescreen.Content.ContentThumbnailData
import org.example.project.ui.components.profilescreen.Content.GridContent
import org.example.project.ui.components.profilescreen.Content.PostEmptyState
import org.example.project.ui.components.profilescreen.Content.PostType
import org.example.project.ui.components.profilescreen.Content.ReelsEmptyState
import org.example.project.ui.components.profilescreen.Content.TaggedEmptyState
import org.example.project.ui.components.profilescreen.ContentPreview
import org.example.project.ui.components.profilescreen.ContentTabBar
import org.example.project.ui.components.profilescreen.ProfileInfoSection
import org.example.project.ui.components.profilescreen.TabItem
import org.example.project.ui.components.profilescreen.TabSwitchLayout
import org.example.project.ui.components.profilescreen.TopBar
import org.example.project.ui.components.profilescreen.TopBarSpan
import org.example.project.ui.components.profilescreen.UserCard
import org.example.project.ui.theme.size

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

data class UserRecommendation(
    val username: String,
    val avatar: String,
    val bio: String
)

val mockRecommendedUsers = listOf(
    UserRecommendation("jane_doe", "https://picsum.photos/seed/1/200", "Traveler & Blogger"),
    UserRecommendation("alex_art", "https://picsum.photos/seed/2/200", "Digital Artist"),
    UserRecommendation("coffee_lover", "https://picsum.photos/seed/3/200", "Barista life"),
    UserRecommendation("dev_mike", "https://picsum.photos/seed/4/200", "Kotlin Enthusiast")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.statusBarsPadding()) {
                TopBar(
                    modifier = Modifier.height(MaterialTheme.size.topBarHeight),
                    title = "user_223",
                    leftSpan1 = TopBarSpan(icon = Icons.Default.Add, onPress = {}),
                    leftSpan2 = TopBarSpan(),
                    rightSpan1 = TopBarSpan(icon = Icons.Rounded.Search, onPress = {}),
                    rightSpan2 = TopBarSpan(icon = Icons.Default.Menu, onPress = {}),
                    onTitlePress = { }
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    ProfileInfoSection(
                        avatarUrl = "https://picsum.photos/200",
                        username = "user_223",
                        signature = "good day"
                    )
                }
                item {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
                    ) {
                        items(mockRecommendedUsers.size) { index ->
                            val user = mockRecommendedUsers[index]
                            UserCard(
                                avatarUrl = user.avatar,
                                username = user.username,
                                extraInfo = user.bio,
                                bottomAction = {
                                    Button(
                                        onClick = {},
                                        modifier = Modifier.fillMaxWidth().height(32.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                                    ) {
                                        Text("Follow", style = MaterialTheme.typography.labelMedium)
                                    }
                                }
                            )
                        }
                    }
                }
                item {
                    val tabs = listOf(
                        TabItem(id = "posts", icon = Icons.Outlined.GridOn),
                        TabItem(id = "reels", icon = Icons.Outlined.PlayCircle),
                        TabItem(id = "tagged", icon = Icons.Outlined.PersonPinCircle),
                    )

                    var selectedTab by remember { mutableStateOf(tabs.first().id) }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 500.dp, max = 2000.dp)
                    ) {
                        TabSwitchLayout(
                            tabs = tabs,
                            selectedTabId = selectedTab,
                            onTabSelected = { selectedTab = it },
                            modifier = Modifier.fillMaxSize()
                        ) { tabId ->
                            val gridModifier = Modifier.fillMaxSize()
                            when (tabId) {
                                "posts" -> GridContent(
                                    modifier = gridModifier,
                                    emptyContent = { PostEmptyState() },
                                    posts = generateMockPosts(20),
                                    isLoadingMore = false
                                )
                                "reels" -> GridContent(
                                    modifier = gridModifier,
                                    emptyContent = { ReelsEmptyState() },
                                    posts = generateMockPosts(10),
                                    isLoadingMore = false
                                )
                                "tagged" -> GridContent(
                                    modifier = gridModifier,
                                    emptyContent = { TaggedEmptyState() },
                                    posts = generateMockPosts(5),
                                    isLoadingMore = false
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
