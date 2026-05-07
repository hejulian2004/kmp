package org.example.project.ui.components.profilescreen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GridOn
import androidx.compose.material.icons.outlined.PersonPinCircle
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Preview
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.VerticalAlignmentLine
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinproject.composeapp.generated.resources.Res
import kotlinproject.composeapp.generated.resources.nav_post
import org.jetbrains.compose.resources.stringResource


data class TabItem(
    val id: String,
    val icon: ImageVector,
    val contentDescription: String? = null,
)

@Composable
fun TabSwitchLayout(
    tabs: List<TabItem>,
    selectedTabId: String = "default",
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (tabId: String) -> Unit,
) {
    val selectedIndex = tabs.indexOfFirst { it.id == selectedTabId }
    val pagerState = rememberPagerState(
        initialPage = selectedIndex.coerceAtLeast(0),
        pageCount = { tabs.size }
    )

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .collect { page ->
                val newId = tabs.getOrNull(page)?.id
                if (newId != null && newId != selectedTabId) {
                    onTabSelected(newId)
                }
            }
    }

    LaunchedEffect(selectedTabId) {
        val index = tabs.indexOfFirst { it.id == selectedTabId }
        if (index >= 0 && index != pagerState.currentPage) {
            pagerState.animateScrollToPage(index)
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        ContentTabBar(
            tabs = tabs,
            selectedTabId = selectedTabId,
            onTabSelected = onTabSelected,
        )
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            content(tabs[page].id)
        }
    }
}
@Composable
fun ContentTabBar(
    tabs: List<TabItem> = listOf(
        TabItem(
            id = "posts",
            icon = Icons.Outlined.GridOn,
            contentDescription = "帖子"
        )
    ),
    selectedTabId: String = stringResource(Res.string.nav_post),
    onTabSelected: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val selectedIndex = remember(selectedTabId, tabs) {
        tabs.indexOfFirst { it.id == selectedTabId }.coerceAtLeast(0)
    }
    Column(modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            tabs.forEachIndexed { index, tab ->
                val isSelected = index == selectedIndex
                val iconColor by animateColorAsState(
                    targetValue = if (isSelected)
                        MaterialTheme.colorScheme.onSurface
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    animationSpec = tween(200, easing = LinearEasing),
                )
                IconButton(
                    onClick = { onTabSelected(tab.id) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.contentDescription,
                        tint = iconColor
                    )
                }
            }
        }
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
        ) {
            val tabWidth = maxWidth / tabs.size
            val indicatorOffset by animateDpAsState(
                targetValue = tabWidth * selectedIndex,
                animationSpec = tween(250, easing = FastOutSlowInEasing),
            )
            Box(
                modifier = Modifier
                    .offset(x = indicatorOffset + tabWidth/3)
                    .width(tabWidth/3)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.onSurface)
            )
        }
    }
}

@Composable
fun Content(
    text: String,
    modifier: Modifier
){
    Text(text,modifier)
}

@Preview(showBackground = true)
@Composable
fun ContentPreview(){
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
            "posts"  -> Content("post", Modifier.background(Color.Red))
            "reels"  -> Content("reels", Modifier.background(Color.Blue))
            "tagged" -> Content("tagged", Modifier.background(Color.Yellow))
        }
    }
}