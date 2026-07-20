/**
 * @File: FeedEffect.kt
 * @Package: org.example.project.presentation.feed
 * @Description: 朋友圈MVI架构中的单次副作用事件定义
 * @Date: 2026-07-20
 */
package org.example.project.presentation.feed

sealed interface FeedEffect{
    data class ShowMessage(
        val message: String
    ): FeedEffect

    data class ScrollToIndex(
        val index: Int
    ): FeedEffect
}


