package org.example.project.presentation.intent.instagram

sealed class InstagramImagePickerIntent {
    data object PickImage : InstagramImagePickerIntent()
    data object ClearImage : InstagramImagePickerIntent()
}

typealias ImagePickerScreenIntent = InstagramImagePickerIntent
