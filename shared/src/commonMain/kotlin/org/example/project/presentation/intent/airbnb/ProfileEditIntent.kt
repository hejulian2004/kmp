/**
 * @File: ProfileEditIntent.kt
 * @Package: org.example.project.presentation.intent.airbnb
 * @Description: Airbnb 个人资料编辑 MVI 意图集合
 * @Author: 何聚敛
 * @Date: 2026-08-04
 */
package org.example.project.presentation.intent.airbnb

import org.example.project.domain.model.airbnb.Host

sealed interface ProfileEditIntent {
    data class InitFromHost(val host: Host) : ProfileEditIntent
    data class UpdateName(val name: String) : ProfileEditIntent
    data class UpdateLanguages(val languages: String) : ProfileEditIntent
    data class UpdateOccupation(val occupation: String) : ProfileEditIntent
    data class UpdateLivesIn(val livesIn: String) : ProfileEditIntent
    data class UpdateAbout(val about: String) : ProfileEditIntent
    data class UpdateHobbies(val hobbies: List<String>) : ProfileEditIntent
    data class AddHobby(val hobby: String) : ProfileEditIntent
    data class UpdatePlaces(val places: List<String>) : ProfileEditIntent
    data class ToggleIdentityVerified(val checked: Boolean) : ProfileEditIntent
    data class ToggleSuperHost(val checked: Boolean) : ProfileEditIntent
    data class TogglePlacesVisible(val visible: Boolean) : ProfileEditIntent
    data class UpdateAvatarUrl(val url: String) : ProfileEditIntent
}
