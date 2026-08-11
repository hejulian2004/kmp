/**
 * @File: SduiActionDispatcher.kt
 * @Package: org.example.project.ui.core.sdui
 * @Description: SDUI结构化Action事件透传分发器
 * @Author: 何聚敛
 * @Date: 2026-08-05
 */
package org.example.project.ui.core.sdui

import org.example.project.core.sdui.model.SduiAction

/**
 * SDUI Action事件处理分发中心
 */
object SduiActionDispatcher {
    /**
     * 将解构出的SduiAction转派给相应的回调与日志中心
     *
     * @param action接收到的交互动作描述
     * @param onActionHandled当前页面ViewModel接收动作的回调
     */
    fun dispatch(action: SduiAction, onActionHandled: (SduiAction) -> Unit) {
        onActionHandled(action)
    }
}
