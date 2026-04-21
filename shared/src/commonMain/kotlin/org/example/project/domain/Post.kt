package org.example.project.domain


sealed class PostContent {
    data class TextPost(val body: String) : PostContent()
//    todo: 未来实现
//    data class ImagePost(val body: String, val images: List<MediaFile>) : PostContent()
//    data class VideoPost(val video: MediaFile, val thumbnail: MediaFile, val caption: String) : PostContent()
}

data class Post(
    val id: String,
    val authorId: String,
    val createdAt: Long,
    val content: PostContent
) {
}

sealed class PublishState {
    object Idle : PublishState()
//    data class Editing(val type: PostType) : PublishState()
    object Publishing : PublishState()
    data class Done(val post: Post) : PublishState()
}