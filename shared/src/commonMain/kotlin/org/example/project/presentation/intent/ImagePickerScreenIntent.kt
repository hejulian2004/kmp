package org.example.project.presentation.intent


sealed class ImagePickerScreenIntent {
    data object PickImage : ImagePickerScreenIntent()
    data object ClearImage : ImagePickerScreenIntent()
}