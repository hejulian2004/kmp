/**
 * @File: HostProfileUiState.kt
 * @Package: org.example.project.presentation.state.airbnb
 * @Description: Airbnb 房东主页 MVI 页面全局状态
 * @Author: 何聚敛
 * @Date: 2026-08-04
 */
package org.example.project.presentation.state.airbnb

import org.example.project.domain.model.airbnb.Host
import org.example.project.domain.model.airbnb.HostReview
import org.example.project.domain.model.airbnb.PropertyListing
import org.example.project.domain.model.airbnb.TravelGuide

data class HostProfileUiState(
    val hosts: List<Host> = emptyList(),
    val selectedHostId: String = "",
    val properties: List<PropertyListing> = emptyList(),
    val reviews: List<HostReview> = emptyList(),
    val guides: List<TravelGuide> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
) {
    val selectedHost: Host?
        get() = hosts.firstOrNull { it.id == selectedHostId }

    val selectedHostProperties: List<PropertyListing>
        get() = properties.filter { it.hostId == selectedHostId }

    val selectedHostReviews: List<HostReview>
        get() = reviews.filter { it.hostId == selectedHostId }

    val selectedHostGuides: List<TravelGuide>
        get() = guides.filter { it.hostId == selectedHostId }
}
