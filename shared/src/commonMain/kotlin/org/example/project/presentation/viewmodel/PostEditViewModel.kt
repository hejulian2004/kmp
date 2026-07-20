package org.example.project.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.presentation.intent.PostEditIntent
import org.example.project.presentation.state.PostEditError
import org.example.project.presentation.state.PostEditState

class PostEditViewModel : ViewModel() {
    private val _state = MutableStateFlow(PostEditState())
    val state: StateFlow<PostEditState> = _state.asStateFlow()

    fun onIntent(intent: PostEditIntent) {
        when (intent) {
            is PostEditIntent.AddImages -> addImages(intent.newImages)
            is PostEditIntent.RemoveImage -> removeImage(intent.index)
            is PostEditIntent.UpdateTitle -> _state.update { it.copy(title = intent.title) }
            is PostEditIntent.UpdateBody -> _state.update { it.copy(body = intent.body) }
            is PostEditIntent.Publish -> publish()
            is PostEditIntent.ClearError -> _state.update { it.copy(error = null, errorMessage = null) }
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
            _state.update { it.copy(error = PostEditError.TITLE_BODY_REQUIRED) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                //todo:
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = PostEditError.PUBLISH_FAILED, errorMessage = e.message) }
            }
        }
    }
}
