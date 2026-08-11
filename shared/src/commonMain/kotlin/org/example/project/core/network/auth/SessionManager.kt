/**
 * @File: SessionManager.kt
 * @Package: org.example.project.core.network.auth
 * @Description: KMP全局用户Session会话与登出事件总线
 * @Author: 何聚敛
 * @Date: 2026-07-30
 */
package org.example.project.core.network.auth

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * [SessionManager]
 * 全局Session会话管理器。
 * 当401凭据彻底失效或用户主动登出时，通过[sessionExpiredEvents]广播通知UI层重定向至登录页。
 */
object SessionManager {
    private val _sessionExpiredEvents = MutableSharedFlow<Unit>(
        replay = 1,
        extraBufferCapacity = 64
    )

    /**
     * 会话失效广播热流。
     * UI层/ViewModel订阅此Flow监听登出事件。
     */
    val sessionExpiredEvents: SharedFlow<Unit> = _sessionExpiredEvents.asSharedFlow()

    /**
     * 触发Session会话失效事件(广播通知全应用登出)。
     */
    suspend fun notifySessionExpired() {
        _sessionExpiredEvents.emit(Unit)
    }

    /**
     * 非挂起方式触发Session会话失效事件。
     */
    fun tryNotifySessionExpired() {
        _sessionExpiredEvents.tryEmit(Unit)
    }
}
