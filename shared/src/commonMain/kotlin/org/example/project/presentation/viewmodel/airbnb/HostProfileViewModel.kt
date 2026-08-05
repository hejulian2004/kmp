/**
 * @File: HostProfileViewModel.kt
 * @Package: org.example.project.presentation.viewmodel.airbnb
 * @Description: Airbnb 房东主页 MVI 架构 ViewModel（融合 Room + SWR 数据流与单向 Intent 统一分发）
 * @Author: 何聚敛
 * @Date: 2026-08-05
 */
package org.example.project.presentation.viewmodel.airbnb

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.core.data.ResourceState
import org.example.project.domain.model.airbnb.Host
import org.example.project.domain.repository.airbnb.HostProfileRepository
import org.example.project.presentation.effect.airbnb.HostProfileEffect
import org.example.project.presentation.intent.airbnb.HostProfileIntent
import org.example.project.presentation.state.airbnb.HostProfileUiState

class HostProfileViewModel(
    private val repository: HostProfileRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HostProfileUiState())
    val uiState: StateFlow<HostProfileUiState> = _uiState.asStateFlow()

    private val _effect = Channel<HostProfileEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        handleIntent(HostProfileIntent.LoadData)
    }

    fun handleIntent(intent: HostProfileIntent) {
        when (intent) {
            is HostProfileIntent.LoadData -> loadData()
            is HostProfileIntent.SelectHost -> selectHost(intent.hostId)
            is HostProfileIntent.UpdateHostName -> updateSelectedHostName(intent.name)
            is HostProfileIntent.UpdateHostLanguages -> updateSelectedHostLanguages(intent.languages)
            is HostProfileIntent.UpdateHostOccupation -> updateSelectedHostOccupation(intent.occupation)
            is HostProfileIntent.UpdateHostLivesIn -> updateSelectedHostLivesIn(intent.livesIn)
            is HostProfileIntent.UpdateHostAbout -> updateSelectedHostAbout(intent.about)
            is HostProfileIntent.UpdateHostHobbies -> updateSelectedHostHobbies(intent.hobbies)
            is HostProfileIntent.UpdateHostPlaces -> updateSelectedHostPlaces(intent.places)
            is HostProfileIntent.UpdateHostPlacesVisible -> updateSelectedHostPlacesVisible(intent.visible)
            is HostProfileIntent.ToggleHostIdentityVerified -> toggleSelectedHostIdentityVerified(intent.checked)
            is HostProfileIntent.ToggleHostSuperHost -> toggleSelectedHostSuperHost(intent.checked)
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                // 结合 SWR 响应式状态流
                repository.getHostsResource().collect { resource ->
                    when (resource) {
                        is ResourceState.Loading -> {
                            _uiState.update { state ->
                                state.copy(
                                    isLoading = true,
                                    hosts = resource.cachedData ?: state.hosts
                                )
                            }
                        }
                        is ResourceState.Success -> {
                            val hosts = resource.data
                            val properties = repository.getProperties()
                            val reviews = repository.getReviews()
                            val guides = repository.getGuides()

                            if (hosts.isEmpty()) {
                                _uiState.update { state ->
                                    state.copy(
                                        isLoading = false,
                                        hosts = hosts,
                                        properties = properties,
                                        reviews = reviews,
                                        guides = guides,
                                        errorMessage = "暂无房东数据"
                                    )
                                }
                            } else {
                                _uiState.update { state ->
                                    state.copy(
                                        isLoading = false,
                                        hosts = hosts,
                                        selectedHostId = if (state.selectedHostId.isEmpty()) hosts.first().id else state.selectedHostId,
                                        properties = properties,
                                        reviews = reviews,
                                        guides = guides,
                                        errorMessage = null
                                    )
                                }
                            }
                        }
                        is ResourceState.Error -> {
                            _uiState.update { state ->
                                state.copy(
                                    isLoading = false,
                                    hosts = resource.cachedData ?: state.hosts,
                                    errorMessage = "数据同步出现异常"
                                )
                            }
                            _effect.send(HostProfileEffect.ShowToast("本地数据已加载，网络同步提示失败"))
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "加载数据失败：${e.message}"
                    )
                }
            }
        }
    }

    private fun selectHost(hostId: String) {
        if (_uiState.value.hosts.any { it.id == hostId }) {
            _uiState.update { it.copy(selectedHostId = hostId) }
        }
    }

    private fun updateSelectedHostName(name: String) {
        updateSelectedHost { it.copy(name = name) }
    }

    private fun updateSelectedHostLanguages(languages: String) {
        updateSelectedHost { it.copy(languages = languages) }
    }

    private fun updateSelectedHostOccupation(occupation: String) {
        updateSelectedHost { it.copy(occupation = occupation) }
    }

    private fun updateSelectedHostLivesIn(livesIn: String) {
        updateSelectedHost { it.copy(livesIn = livesIn) }
    }

    private fun updateSelectedHostAbout(about: String) {
        updateSelectedHost { it.copy(about = about) }
    }

    private fun updateSelectedHostHobbies(hobbies: List<String>) {
        updateSelectedHost { it.copy(hobbies = hobbies) }
    }

    private fun updateSelectedHostPlaces(places: List<String>) {
        updateSelectedHost { it.copy(places = places) }
    }

    private fun updateSelectedHostPlacesVisible(visible: Boolean) {
        updateSelectedHost { it.copy(placesVisible = visible) }
    }

    private fun toggleSelectedHostIdentityVerified(checked: Boolean) {
        updateSelectedHost { it.copy(identityVerified = checked) }
    }

    private fun toggleSelectedHostSuperHost(checked: Boolean) {
        updateSelectedHost { it.copy(superHost = checked) }
    }

    private fun updateSelectedHost(transform: (Host) -> Host) {
        val selectedId = _uiState.value.selectedHostId
        _uiState.update { state ->
            state.copy(
                hosts = state.hosts.map { host ->
                    if (host.id == selectedId) transform(host) else host
                }
            )
        }
    }
}
