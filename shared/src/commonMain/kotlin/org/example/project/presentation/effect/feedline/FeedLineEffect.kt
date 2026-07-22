/**
 * @File: FeedLineEffect.kt
 * @Package: org.example.project.presentation.effect.feedline
 * @Description: 朋友圈MVI架构中的单次副作用事件定义
 * @Author: 何聚敛
 * @Date: 2026-07-20
 */
package org.example.project.presentation.effect.feedline
sealed interface FeedLineEffect{
    data class ShowMessage(
        val message: String
    ): FeedLineEffect

    data class ScrollToIndex(
        val index: Int
    ): FeedLineEffect
}