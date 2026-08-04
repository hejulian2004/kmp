/**
 * @File: SduiActionDispatcher.kt
 * @Package: org.example.project.ui.core.sdui
 * @Description: SDUI结构化Action事件透传分发器
 * @Author: 何聚敛
 * @Date: 2026-08-04
 */
package org.example.project.ui.core.sdui

import org.example.project.core.sdui.model.SduiAction

/**
 * SDUI Action 事件处理分发中心
 */
object SduiActionDispatcher {
    /**
     * 将解构出的 SduiAction 转派给相应的回调与日志中心
     *
     * @param action 接收到的交互动作描述
     * @param onActionHandled 当前页面 ViewModel 接收动作的回调
     */
    fun dispatch(action: SduiAction, onActionHandled: (SduiAction) -> Unit) {
        onActionHandled(action)
    }
}
