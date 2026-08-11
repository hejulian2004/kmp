package org.example.project.presentation.intent.instagram

import io.github.vinceglb.filekit.PlatformFile

sealed class InstagramPostEditIntent {
    data class AddImages(val newImages: PlatformFile) : InstagramPostEditIntent()
    data class RemoveImage(val index: Int) : InstagramPostEditIntent()
    data class UpdateContent(val content: String) : InstagramPostEditIntent()
    data class UpdateLocation(val location: String?) : InstagramPostEditIntent()
    data object Publish : InstagramPostEditIntent()
    data object ClearError : InstagramPostEditIntent()
}

typealias PostEditIntent = InstagramPostEditIntent
