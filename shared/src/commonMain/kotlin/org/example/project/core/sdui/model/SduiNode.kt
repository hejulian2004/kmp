/**
 * @File: SduiNode.kt
 * @Package: org.example.project.core.sdui.model
 * @Description: SDUI动态布局节点数据模型与样式描述定义
 * @Author: 何聚敛
 * @Date: 2026-08-05
 */
package org.example.project.core.sdui.model

import kotlinx.serialization.Serializable

/**
 * SDUI动态组件树节点
 *
 * @param componentType 关联的组件类型名称（如 "FeedLineTopBar", "Column", "Text"）
 * @param properties 传递给原生组件的属性键值对字典
 * @param style 节点排版与视效样式
 * @param dataBinding 数据绑定表达式或键名（如 "post"）
 * @param actions 组件绑定的事件动作映射表（如 "onLikeClick" -> SduiAction）
 * @param children 子节点列表
 */
@Serializable
data class SduiNode(
    val componentType: String,
    val properties: Map<String, String> = emptyMap(),
    val style: SduiStyle = SduiStyle(),
    val dataBinding: String? = null,
    val actions: Map<String, SduiAction> = emptyMap(),
    val children: List<SduiNode> = emptyList()
)

/**
 * SDUI节点通用样式定义
 *
 * @param paddingDp 内边距(dp)
 * @param backgroundColorHex 十六进制背景颜色（如 "#FFFFFF"）
 * @param cornerRadiusDp 圆角半径(dp)
 * @param fontSizeSp 字体大小(sp)
 * @param fontWeight 字体粗细（如 "Bold", "Normal"）
 * @param isVisible 是否显示
 */
@Serializable
data class SduiStyle(
    val paddingDp: Int = 0,
    val backgroundColorHex: String? = null,
    val cornerRadiusDp: Int = 0,
    val fontSizeSp: Int = 14,
    val fontWeight: String = "Normal",
    val isVisible: Boolean = true
)
