/**
 * @File: SduiAction.kt
 * @Package: org.example.project.core.sdui.model
 * @Description: SDUI动态组件交互动作数据模型
 * @Author: 何聚敛
 * @Date: 2026-08-05
 */
package org.example.project.core.sdui.model

import kotlinx.serialization.Serializable

/**
 * SDUI结构化交互动作描述
 *
 * 用于将动态组件的交互事件（如点击点赞、打开评论、路由跳转）投递给 MVI 架构中的 UiIntent 处理中心。
 *
 * @param type 动作类型枚举值或字符串标识（如 "NAVIGATE", "TOGGLE_LIKE", "OPEN_SHEET"）
 * @param params 交互携带的扩展参数字典（如 "postId" -> "1001", "url" -> "https://..."）
 */
@Serializable
data class SduiAction(
    val type: String,
    val params: Map<String, String> = emptyMap()
)
