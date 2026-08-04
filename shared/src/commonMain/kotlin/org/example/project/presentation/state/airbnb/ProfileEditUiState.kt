/**
 * @File: ProfileEditUiState.kt
 * @Package: org.example.project.presentation.state.airbnb
 * @Description: Airbnb 房东资料编辑 MVI 页面状态
 * @Author: 何聚敛
 * @Date: 2026-08-04
 */
package org.example.project.presentation.state.airbnb

data class ProfileEditState(
    val avatarUrl: String = "",
    val name: String = "",
    val languages: String = "",
    val occupation: String = "",
    val livesIn: String = "",
    val about: String = "",
    val hobbies: List<String> = emptyList(),
    val places: List<String> = emptyList(),
    val identityVerified: Boolean = false,
    val superHost: Boolean = false,
    val placesVisible: Boolean = false,
)
