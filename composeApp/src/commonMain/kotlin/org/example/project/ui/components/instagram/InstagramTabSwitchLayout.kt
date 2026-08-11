package org.example.project.ui.components.instagram

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

data class InstagramTabItem(
    val id: String,
    val icon: ImageVector,
    val contentDescription: String? = null,
)

typealias TabItem = InstagramTabItem

@Composable
fun InstagramTabSwitchLayout(
    tabs: List<InstagramTabItem>,
    selectedTabId: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (tabId: String) -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            val selectedIndex = tabs.indexOfFirst { it.id == selectedTabId }.coerceAtLeast(0)
            tabs.forEachIndexed { index, tab ->
                val isSelected = index == selectedIndex
                val iconColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    animationSpec = tween(200)
                )
                IconButton(
                    onClick = { onTabSelected(tab.id) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = tab.icon, contentDescription = tab.contentDescription, tint = iconColor)
                }
            }
        }
        BoxWithConstraints(modifier = Modifier.fillMaxWidth().height(2.dp)) {
            val tabWidth = maxWidth / tabs.size
            val selectedIndex = tabs.indexOfFirst { it.id == selectedTabId }.coerceAtLeast(0)
            Box(
                modifier = Modifier
                    .offset(x = tabWidth * selectedIndex + tabWidth / 3)
                    .width(tabWidth / 3)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.onSurface)
            )
        }
        content(selectedTabId)
    }
}

@Composable
fun TabSwitchLayout(
    tabs: List<InstagramTabItem>,
    selectedTabId: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (tabId: String) -> Unit
) = InstagramTabSwitchLayout(tabs, selectedTabId, onTabSelected, modifier, content)

@Composable
fun InstagramContentTabBar(
    tabs: List<InstagramTabItem>,
    selectedTabId: String,
    pagerState: androidx.compose.foundation.pager.PagerState,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            val selectedIndex = tabs.indexOfFirst { it.id == selectedTabId }.coerceAtLeast(0)
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
            val indicatorOffset by remember {
                derivedStateOf {
                    tabWidth * (pagerState.currentPage + pagerState.currentPageOffsetFraction) + tabWidth / 3
                }
            }
            Box(
                modifier = Modifier
                    .offset(x = indicatorOffset)
                    .width(tabWidth / 3)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.onSurface)
            )
        }
    }
}

@Composable
fun ContentTabBar(
    tabs: List<InstagramTabItem>,
    selectedTabId: String,
    pagerState: androidx.compose.foundation.pager.PagerState,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) = InstagramContentTabBar(tabs, selectedTabId, pagerState, onTabSelected, modifier)
