package org.example.project.presentation.state

import io.github.vinceglb.filekit.PlatformFile

enum class PostEditError {
    TITLE_BODY_REQUIRED,
    PUBLISH_FAILED
}

data class PostEditState(
    val images: List<PlatformFile> = emptyList(),
    val title: String = "",
    val body: String = "",
    val isLoading: Boolean = false,
    val error: PostEditError? = null,
    val errorMessage: String? = null,
    val maxImageCount: Int = 8
) {
    val canPublish: Boolean
        get() = title.isNotBlank() && body.isNotBlank() && images.isNotEmpty()
}
