/**
 * @File: HostProfileIntent.kt
 * @Package: org.example.project.presentation.intent.airbnb
 * @Description: Airbnb 房东主页 MVI 意图集合
 * @Author: 何聚敛
 * @Date: 2026-08-04
 */
package org.example.project.presentation.intent.airbnb

import org.example.project.domain.model.airbnb.Host

sealed interface HostProfileIntent {
    data object LoadData : HostProfileIntent
    data class SelectHost(val hostId: String) : HostProfileIntent
    data class UpdateHostName(val name: String) : HostProfileIntent
    data class UpdateHostLanguages(val languages: String) : HostProfileIntent
    data class UpdateHostOccupation(val occupation: String) : HostProfileIntent
    data class UpdateHostLivesIn(val livesIn: String) : HostProfileIntent
    data class UpdateHostAbout(val about: String) : HostProfileIntent
    data class UpdateHostHobbies(val hobbies: List<String>) : HostProfileIntent
    data class UpdateHostPlaces(val places: List<String>) : HostProfileIntent
    data class UpdateHostPlacesVisible(val visible: Boolean) : HostProfileIntent
    data class ToggleHostIdentityVerified(val checked: Boolean) : HostProfileIntent
    data class ToggleHostSuperHost(val checked: Boolean) : HostProfileIntent
}
