package org.example.project.presentation.viewmodel.instagram

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class InstagramMainViewModel : ViewModel() {
    val showBottomBar = mutableStateOf(true)
    fun setBottomBarVisible(visible: Boolean) {
        showBottomBar.value = visible
    }
}

typealias MainViewModel = InstagramMainViewModel
