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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.GridOn
import androidx.compose.material.icons.outlined.PersonPinCircle
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import org.example.project.ui.components.profilescreen.ContentPreview
import org.example.project.ui.components.profilescreen.ContentTabBar
import org.example.project.ui.components.profilescreen.ProfileInfoSection
import org.example.project.ui.components.profilescreen.TabItem
import org.example.project.ui.components.profilescreen.TabSwitchLayout
import org.example.project.ui.components.profilescreen.TopBar
import org.example.project.ui.components.profilescreen.TopBarSpan
import org.example.project.ui.theme.size

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
) {
    Surface(
        modifier = Modifier
            .fillMaxSize(),
        color = MaterialTheme.colorScheme.background
        ) {
        Column {
            Row(
                modifier = Modifier
                    .statusBarsPadding()
            ) {
                TopBar(
                    modifier = Modifier
                        .height(MaterialTheme.size.topBarHeight),
                    title = "user_223",
                    leftSpan1 = TopBarSpan(
                        icon = Icons.Default.Add,
                        alignment = Alignment.CenterStart,
                        onPress = { }
                    ),
                    leftSpan2 = TopBarSpan(),
                    rightSpan1 = TopBarSpan(
                        icon = Icons.Rounded.Search,
                        alignment = Alignment.Center,
                        onPress = { }
                    ),
                    rightSpan2 = TopBarSpan(
                        icon = Icons.Default.Menu,
                        alignment = Alignment.CenterEnd,
                        onPress = { }
                    ),
                    onTitlePress = { isExpanded -> }
                )
            }
            ProfileInfoSection(
                avatarUrl = "https://picsum.photos/200",
                username = "user_223",
                signature = "good day"
            )
            val tabs = listOf(
                TabItem(
                    id = "posts",
                    icon = Icons.Outlined.GridOn,
                ),
                TabItem(
                    id = "reels",
                    icon = Icons.Outlined.PlayCircle,
                ),
                TabItem(
                    id = "tagged",
                    icon = Icons.Outlined.PersonPinCircle,
                ),
            )

            var selectedTab by remember { mutableStateOf(tabs.first().id) }

            TabSwitchLayout(
                tabs = tabs,
                selectedTabId = selectedTab,
                onTabSelected = { selectedTab = it },
            ) { tabId ->
                when (tabId) {
                    "posts" -> Content("post", Modifier.background(Color.Red))
                    "reels" -> Content("reels", Modifier.background(Color.Blue))
                    "tagged" -> Content("tagged", Modifier.background(Color.Yellow))
                }
            }
        }
    }
}
