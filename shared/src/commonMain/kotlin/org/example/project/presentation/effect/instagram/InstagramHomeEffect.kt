/**
 * @File: InstagramHomeEffect.kt
 * @Package: org.example.project.presentation.effect.instagram
 * @Description: Instagram首页MVI一次性UI事件管道（对标FeedLineEffect）
 * @Author: 何聚敛
 * @Date: 2026-07-28
 */
package org.example.project.presentation.effect.instagram

/**
 * Instagram首页MVI模式的一次性UI侧效应管道
 */
sealed interface InstagramHomeEffect {
    /** 弹出Toast/Snackbar消息提示 */
    data class ShowMessage(val message: String) : InstagramHomeEffect

    /** 平滑滚动列表至指定Item位置 */
    data class ScrollToIndex(val index: Int = 0) : InstagramHomeEffect
}
