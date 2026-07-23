package org.example.project.presentation.viewmodel.instagram

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.presentation.intent.instagram.InstagramPostEditIntent
import org.example.project.presentation.state.instagram.InstagramPostEditError
import org.example.project.presentation.state.instagram.InstagramPostEditState

class InstagramPostEditViewModel : ViewModel() {
    private val _state = MutableStateFlow(InstagramPostEditState())
    val state: StateFlow<InstagramPostEditState> = _state.asStateFlow()

    fun onIntent(intent: InstagramPostEditIntent) {
        when (intent) {
            is InstagramPostEditIntent.AddImages -> addImages(intent.newImages)
            is InstagramPostEditIntent.RemoveImage -> removeImage(intent.index)
            is InstagramPostEditIntent.UpdateTitle -> _state.update { it.copy(title = intent.title) }
            is InstagramPostEditIntent.UpdateBody -> _state.update { it.copy(body = intent.body) }
            is InstagramPostEditIntent.Publish -> publish()
            is InstagramPostEditIntent.ClearError -> _state.update { it.copy(error = null, errorMessage = null) }
        }
    }

    private fun addImages(newImages: PlatformFile) {
        _state.update { currentState ->
            val totalImages = (currentState.images + newImages).take(currentState.maxImageCount)
            currentState.copy(images = totalImages)
        }
    }

    private fun removeImage(index: Int) {
        _state.update { currentState ->
            val updatedImages = currentState.images.toMutableList().apply {
                if (index in indices) removeAt(index)
            }
            currentState.copy(images = updatedImages)
        }
    }

    private fun publish() {
        val currentState = _state.value
        if (!currentState.canPublish) {
            _state.update { it.copy(error = InstagramPostEditError.TITLE_BODY_REQUIRED) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                //todo:
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = InstagramPostEditError.PUBLISH_FAILED, errorMessage = e.message) }
            }
        }
    }
}

typealias PostEditViewModel = InstagramPostEditViewModel
