package org.example.project.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import org.example.project.navigation.Screen.Detail.action
import org.example.project.ui.components.PostPickerContent
import org.example.project.ui.theme.NavAnim
import org.example.project.ui.theme.NavColors
import org.example.project.ui.theme.NavSize
import org.example.project.ui.theme.navColors


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomBar(
    navController: NavHostController,
    screens: List<Screen>
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    var currentPopup by remember { mutableStateOf<NavAction.Popup?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    when (val popup = currentPopup) {
        is NavAction.Popup.BottomSheet -> {
            ModalBottomSheet(
                onDismissRequest = { currentPopup = null },
                sheetState = sheetState
            ) {
                popup.content(
                    { currentPopup = null },
                    navController
                )
            }
        }
        is NavAction.Popup.Dialog -> {
            //todo:
        }
        is NavAction.Popup.TopSheet -> {
            // todo:
        }
        null -> {
        }
    }

    val colors = navColors()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.background)
            .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
    ) {
        HorizontalDivider(
            thickness = NavSize.DividerThickness,
            color = colors.divider
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = NavSize.BarPaddingHorizontal,
                    vertical = NavSize.BarPaddingVertical
                ),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ){
            screens.forEach { screen ->
                val isSelected = currentDestination?.route == screen.route
                NavItem(
                    screen,
                    isSelected,
                    colors = colors,
                    onClick = {
                        when (val action = screen.action) {
                            is NavAction.Navigate -> {
                                if (!isSelected) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                            is NavAction.Popup -> {
                                currentPopup = action
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun NavItem(
    screen: Screen,
    isSelected: Boolean,
    colors : NavColors,
    onClick: () -> Unit
){
    val iconScale by animateFloatAsState(
        targetValue =  if(isSelected) NavSize.IconScaleSelected else NavSize.IconScaleUnselected,
        animationSpec = tween(NavAnim.DurationMs),
        label         = "iconScale"
    )
    val iconColor by animateColorAsState(
        targetValue   = if (isSelected) colors.selectedIcon else colors.unselectedIcon,
        animationSpec = tween(NavAnim.DurationMs),
        label         = "iconColor"
    )

    Box(
        modifier = Modifier
            .size(NavSize.IconSize*2)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ){
        Icon(
            imageVector = screen.icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier
                .size(NavSize.IconSize)
                .scale(iconScale)
        )
    }
}

@Composable
@Preview
fun BottomNavigationPreview(){
    val screens = bottomNavScreens
    BottomBar(rememberNavController(),screens)
}