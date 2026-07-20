package org.example.project.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.project.domain.model.PostModel
import org.example.project.presentation.intent.ImagePickerScreenIntent
import org.example.project.presentation.state.UiState

class ImagePickerScreenViewModel: ViewModel() {
    private val _state = MutableStateFlow<UiState<PostModel>>(UiState.Idle)
    val state: StateFlow<UiState<PostModel>> = _state.asStateFlow()

    fun onIntent(intent: ImagePickerScreenIntent) {
        when (intent) {
            is ImagePickerScreenIntent.PickImage -> pickImage()
            is ImagePickerScreenIntent.ClearImage -> _state.value = UiState.Idle
        }
    }

    private fun pickImage() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val file = FileKit.openFilePicker(type = FileKitType.Image)
                if (file != null) {
                } else {
                    _state.value = UiState.Idle
                }
            } catch (e: Exception) {
                _state.value = UiState.Error(message = e.message ?: "未知错误")
            }
        }
    }

}
