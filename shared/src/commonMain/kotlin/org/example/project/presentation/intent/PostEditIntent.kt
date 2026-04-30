package org.example.project.presentation.intent

import io.github.vinceglb.filekit.PlatformFile
sealed class PostEditIntent {
    data class AddImages(val newImages: PlatformFile) : PostEditIntent()
    data class RemoveImage(val index: Int) : PostEditIntent()
    data class UpdateTitle(val title: String) : PostEditIntent()
    data class UpdateBody(val body: String) : PostEditIntent()
    object Publish : PostEditIntent()
    object ClearError : PostEditIntent()
}
