/**
 * @File: AirbnbMainScreen.kt
 * @Package: org.example.project.ui.screens.airbnb
 * @Description: Airbnb业务模块主Screen容器（整合Room本地DB、HostProfile聚合页面、编辑页与设置页导航状态）
 * @Author: 何聚敛
 * @Date: 2026-08-11
 */
package org.example.project.ui.screens.airbnb

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import org.example.project.core.database.getRoomDatabase
import org.example.project.core.network.client.AppNetworkInitializer
import org.example.project.data.repository.airbnb.HostProfileRepositoryImpl
import org.example.project.presentation.intent.airbnb.ProfileEditIntent
import org.example.project.presentation.viewmodel.airbnb.HostProfileViewModel
import org.example.project.presentation.viewmodel.airbnb.ProfileEditViewModel
import org.example.project.presentation.viewmodel.airbnb.SettingsViewModel
import org.example.project.ui.theme.airbnb.AirbnbTheme

enum class AirbnbSubScreen {
    PROFILE,
    EDIT,
    SETTINGS
}

@Composable
fun AirbnbMainScreen(
    context: Any? = null,
) {
    val database = remember {
        getRoomDatabase(context)
    }

    val repository = remember {
        HostProfileRepositoryImpl(
            dao = database.hostProfileDao(),
            networkContainer = AppNetworkInitializer.container
        )
    }

    val profileViewModel = viewModel { HostProfileViewModel(repository) }
    val editViewModel = viewModel { ProfileEditViewModel() }
    val settingsViewModel = viewModel { SettingsViewModel() }

    val profileUiState by profileViewModel.uiState.collectAsState()
    val editUiState by editViewModel.uiState.collectAsState()
    val settingsUiState by settingsViewModel.uiState.collectAsState()

    var currentSubScreen by remember { mutableStateOf(AirbnbSubScreen.PROFILE) }

    AirbnbTheme(themeMode = settingsUiState.themeMode) {
        when (currentSubScreen) {
            AirbnbSubScreen.PROFILE -> {
                HostProfileScreen(
                    uiState = profileUiState,
                    onIntent = profileViewModel::handleIntent,
                    onEditHostClick = {
                        profileUiState.selectedHost?.let { host ->
                            editViewModel.handleIntent(ProfileEditIntent.InitFromHost(host))
                        }
                        currentSubScreen = AirbnbSubScreen.EDIT
                    },
                    onSettingsClick = {
                        currentSubScreen = AirbnbSubScreen.SETTINGS
                    }
                )
            }
            AirbnbSubScreen.EDIT -> {
                HostEditScreen(
                    state = editUiState,
                    onIntent = editViewModel::handleIntent,
                    onBack = {
                        currentSubScreen = AirbnbSubScreen.PROFILE
                    }
                )
            }
            AirbnbSubScreen.SETTINGS -> {
                SettingsScreen(
                    state = settingsUiState,
                    onIntent = settingsViewModel::handleIntent,
                    onBack = {
                        currentSubScreen = AirbnbSubScreen.PROFILE
                    }
                )
            }
        }
    }
}
