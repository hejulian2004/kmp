package org.example.project.presentation.feed

sealed interface FeedEffect{
    data class ShowMessage(
        val message: String
    ): FeedEffect

    data class ScrollToIndex(
        val index: Int
    ): FeedEffect
}
