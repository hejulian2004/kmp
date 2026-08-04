/**
 * @File: SduiLayoutBuilder.kt
 * @Package: org.example.project.core.sdui.builder
 * @Description: 提供强类型的 Kotlin DSL Builder 声明语法并转换输出 JSON 字符串
 * @Author: 何聚敛
 * @Date: 2026-08-04
 */
package org.example.project.core.sdui.builder

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.example.project.core.sdui.model.SduiAction
import org.example.project.core.sdui.model.SduiNode
import org.example.project.core.sdui.model.SduiStyle

/**
 * SDUI DSL 构建节点类
 */
class SduiNodeBuilder(private val componentType: String) {
    var properties: MutableMap<String, String> = mutableMapOf()
    var style: SduiStyle = SduiStyle()
    var dataBinding: String? = null
    var actions: MutableMap<String, SduiAction> = mutableMapOf()
    private val children: MutableList<SduiNode> = mutableListOf()

    fun prop(key: String, value: String) {
        properties[key] = value
    }

    fun action(eventName: String, actionType: String, params: Map<String, String> = emptyMap()) {
        actions[eventName] = SduiAction(type = actionType, params = params)
    }

    fun node(type: String, block: SduiNodeBuilder.() -> Unit = {}) {
        val childBuilder = SduiNodeBuilder(type)
        childBuilder.block()
        children.add(childBuilder.build())
    }

    fun build(): SduiNode {
        return SduiNode(
            componentType = componentType,
            properties = properties,
            style = style,
            dataBinding = dataBinding,
            actions = actions,
            children = children
        )
    }
}

/**
 * 根 DSL 构建器函数
 *
 * @param rootType 根节点类型名（如 "LazyColumn"）
 * @param block 节点构建作用域
 * @return 构造完成的 SduiNode
 */
fun sduiLayout(rootType: String, block: SduiNodeBuilder.() -> Unit): SduiNode {
    val builder = SduiNodeBuilder(rootType)
    builder.block()
    return builder.build()
}

/**
 * 将 SduiNode 序列化为格式化的 JSON 字符串
 */
fun SduiNode.toJson(prettyPrint: Boolean = true): String {
    val json = Json { this.prettyPrint = prettyPrint }
    return json.encodeToString(this)
}
