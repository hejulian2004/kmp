package org.example.project.presentation.intent.instagram

import io.github.vinceglb.filekit.PlatformFile

sealed class InstagramPostEditIntent {
    data class AddImages(val newImages: PlatformFile) : InstagramPostEditIntent()
    data class RemoveImage(val index: Int) : InstagramPostEditIntent()
    data class UpdateTitle(val title: String) : InstagramPostEditIntent()
    data class UpdateBody(val body: String) : InstagramPostEditIntent()
    data object Publish : InstagramPostEditIntent()
    data object ClearError : InstagramPostEditIntent()
}

typealias PostEditIntent = InstagramPostEditIntent
