/**
 * @File: ProfileEditViewModel.kt
 * @Package: org.example.project.presentation.viewmodel.airbnb
 * @Description: Airbnb 个人资料编辑 MVI ViewModel（保留原逻辑与单向 Intent 触发）
 * @Author: 何聚敛
 * @Date: 2026-08-05
 */
package org.example.project.presentation.viewmodel.airbnb

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.example.project.domain.model.airbnb.Host
import org.example.project.presentation.intent.airbnb.ProfileEditIntent
import org.example.project.presentation.state.airbnb.ProfileEditState

class ProfileEditViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileEditState())
    val uiState: StateFlow<ProfileEditState> = _uiState.asStateFlow()

    fun handleIntent(intent: ProfileEditIntent) {
        when (intent) {
            is ProfileEditIntent.InitFromHost -> initFromHost(intent.host)
            is ProfileEditIntent.UpdateName -> updateName(intent.name)
            is ProfileEditIntent.UpdateLanguages -> updateLanguages(intent.languages)
            is ProfileEditIntent.UpdateOccupation -> updateOccupation(intent.occupation)
            is ProfileEditIntent.UpdateLivesIn -> updateLivesIn(intent.livesIn)
            is ProfileEditIntent.UpdateAbout -> updateAbout(intent.about)
            is ProfileEditIntent.UpdateHobbies -> updateHobbies(intent.hobbies)
            is ProfileEditIntent.AddHobby -> addHobby(intent.hobby)
            is ProfileEditIntent.UpdatePlaces -> updatePlaces(intent.places)
            is ProfileEditIntent.ToggleIdentityVerified -> toggleIdentityVerified(intent.checked)
            is ProfileEditIntent.ToggleSuperHost -> toggleSuperHost(intent.checked)
            is ProfileEditIntent.TogglePlacesVisible -> togglePlacesVisible(intent.visible)
            is ProfileEditIntent.UpdateAvatarUrl -> updateAvatarUrl(intent.url)
        }
    }

    private fun initFromHost(host: Host) {
        _uiState.value = ProfileEditState(
            avatarUrl = host.avatarUrl,
            name = host.name,
            languages = host.languages,
            occupation = host.occupation,
            livesIn = host.livesIn,
            about = host.about,
            hobbies = host.hobbies,
            places = host.places,
            placesVisible = host.placesVisible,
            identityVerified = host.identityVerified,
            superHost = host.superHost,
        )
    }

    private fun updateName(value: String) { _uiState.update { it.copy(name = value) } }
    private fun updateLanguages(value: String) { _uiState.update { it.copy(languages = value) } }
    private fun updateOccupation(value: String) { _uiState.update { it.copy(occupation = value) } }
    private fun updateLivesIn(value: String) { _uiState.update { it.copy(livesIn = value) } }
    private fun updateAbout(value: String) { _uiState.update { it.copy(about = value) } }
    private fun updateHobbies(value: List<String>) { _uiState.update { it.copy(hobbies = value) } }
    private fun addHobby(hobby: String) {
        _uiState.update { it.copy(hobbies = it.hobbies + hobby.trim()) }
    }
    private fun updatePlaces(value: List<String>) { _uiState.update { it.copy(places = value) } }
    private fun toggleIdentityVerified(checked: Boolean) { _uiState.update { it.copy(identityVerified = checked) } }
    private fun toggleSuperHost(checked: Boolean) { _uiState.update { it.copy(superHost = checked) } }
    private fun togglePlacesVisible(visible: Boolean) { _uiState.update { it.copy(placesVisible = visible) } }
    private fun updateAvatarUrl(url: String) { _uiState.update { it.copy(avatarUrl = url) } }
}
