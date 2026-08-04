/**
 * @File: HostEditScreen.kt
 * @Package: org.example.project.ui.screens.airbnb
 * @Description: Airbnb 房东资料编辑容器 Screen 组件（符合 MVI 架构）
 * @Author: 何聚敛
 * @Date: 2026-08-04
 */
package org.example.project.ui.screens.airbnb

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.example.project.presentation.intent.airbnb.ProfileEditIntent
import org.example.project.presentation.state.airbnb.ProfileEditState
import org.example.project.ui.components.airbnb.AboutMeSection
import org.example.project.ui.components.airbnb.AvatarSection
import org.example.project.ui.components.airbnb.EditFieldBottomSheet
import org.example.project.ui.components.airbnb.HobbiesSection
import org.example.project.ui.components.airbnb.PlacesSection
import org.example.project.ui.components.airbnb.ProfileField
import org.example.project.ui.components.airbnb.ProfileFieldItem
import org.example.project.ui.components.airbnb.ToggleItem
import org.example.project.ui.components.airbnb.TopBar
import org.example.project.ui.theme.airbnb.Accent
import org.example.project.ui.theme.airbnb.AirbnbTheme
import org.example.project.ui.theme.airbnb.DividerColor
import org.example.project.ui.theme.airbnb.PageBg

@Composable
fun HostEditScreen(
    state: ProfileEditState,
    onIntent: (ProfileEditIntent) -> Unit = {},
    onBack: () -> Unit = {},
) {
    var editingField by remember { mutableStateOf<ProfileField?>(null) }

    fun currentValueOf(field: ProfileField): String = when (field) {
        ProfileField.NAME -> state.name
        ProfileField.OCCUPATION -> state.occupation
        ProfileField.LIVES_IN -> state.livesIn
        ProfileField.LANGUAGES -> state.languages
        ProfileField.HOBBIES_ADD -> ""
    }

    fun saveField(field: ProfileField, value: String) {
        when (field) {
            ProfileField.NAME -> onIntent(ProfileEditIntent.UpdateName(value))
            ProfileField.OCCUPATION -> onIntent(ProfileEditIntent.UpdateOccupation(value))
            ProfileField.LIVES_IN -> onIntent(ProfileEditIntent.UpdateLivesIn(value))
            ProfileField.LANGUAGES -> onIntent(ProfileEditIntent.UpdateLanguages(value))
            ProfileField.HOBBIES_ADD -> onIntent(ProfileEditIntent.AddHobby(value))
        }
    }

    if (editingField != null) {
        val field = editingField!!
        EditFieldBottomSheet(
            field = field,
            currentValue = currentValueOf(field),
            onDismiss = { editingField = null },
            onSave = { value ->
                saveField(field, value)
                editingField = null
            },
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBg)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            TopBar(
                title = "编辑个人资料",
                actionText = "完成",
                onActionClick = onBack,
            )
        }

        item {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                AvatarSection(
                    avatarUrl = state.avatarUrl,
                    onAvatarClick = { },
                )
            }
        }

        item {
            org.example.project.ui.components.airbnb.SectionCard {
                ProfileFieldItem(
                    label = "房东姓名",
                    value = state.name,
                    onClick = { editingField = ProfileField.NAME },
                )

                ProfileFieldItem(
                    label = "职业",
                    value = state.occupation,
                    onClick = { editingField = ProfileField.OCCUPATION },
                )

                ProfileFieldItem(
                    label = "住在哪里",
                    value = state.livesIn,
                    onClick = { editingField = ProfileField.LIVES_IN },
                )

                ProfileFieldItem(
                    label = "语言",
                    value = state.languages,
                    onClick = { editingField = ProfileField.LANGUAGES },
                )

                ToggleItem(
                    title = "身份验证标识",
                    checked = state.identityVerified,
                    onCheckedChange = { onIntent(ProfileEditIntent.ToggleIdentityVerified(it)) },
                )

                ToggleItem(
                    title = "超赞房东认证",
                    checked = state.superHost,
                    onCheckedChange = { onIntent(ProfileEditIntent.ToggleSuperHost(it)) },
                )
            }
        }

        item {
            AboutMeSection(
                text = state.about,
                onTextChange = { onIntent(ProfileEditIntent.UpdateAbout(it)) },
            )
        }

        item {
            HorizontalDivider(color = DividerColor)
        }

        item {
            PlacesSection(
                places = state.places,
                isVisible = state.placesVisible,
                onToggle = { onIntent(ProfileEditIntent.TogglePlacesVisible(it)) },
            )
        }

        item {
            HorizontalDivider(color = DividerColor)
        }

        item {
            HobbiesSection(
                hobbies = state.hobbies,
                onAddHobby = { editingField = ProfileField.HOBBIES_ADD },
                onDeleteHobby = { hobby ->
                    onIntent(ProfileEditIntent.UpdateHobbies(state.hobbies - hobby))
                },
            )
        }

        item {
            Box(modifier = Modifier.height(80.dp))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .padding(16.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Button(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Accent),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text(
                "保存并返回",
                color = Color.White,
                style = MaterialTheme.typography.titleSmall,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HostEditScreenPreview() {
    AirbnbTheme {
        HostEditScreen(
            state = ProfileEditState(
                name = "ArtRoomHK",
                occupation = "艺术家",
                livesIn = "香港",
                languages = "中文和英语"
            )
        )
    }
}
