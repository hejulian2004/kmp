package org.example.project.presentation.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    val showBottomBar = mutableStateOf(true)
    fun setBottomBarVisible(visible: Boolean) {
        showBottomBar.value = visible
    }
}