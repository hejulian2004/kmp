package org.example.project.presentation.state.instagram

import io.github.vinceglb.filekit.PlatformFile

enum class InstagramPostEditError {
    CONTENT_REQUIRED,
    PUBLISH_FAILED
}

typealias PostEditError = InstagramPostEditError

data class InstagramPostEditState(
    val images: List<PlatformFile> = emptyList(),
    val content: String = "",
    val location: String? = null,
    val isLoading: Boolean = false,
    val error: PostEditError? = null,
    val errorMessage: String? = null,
    val maxImageCount: Int = 8
) {
    val canPublish: Boolean
        get() = content.isNotBlank() && images.isNotEmpty()
}

typealias PostEditState = InstagramPostEditState
