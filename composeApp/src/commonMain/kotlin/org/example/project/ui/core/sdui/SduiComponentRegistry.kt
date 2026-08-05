/**
 * @File: SduiComponentRegistry.kt
 * @Package: org.example.project.ui.core.sdui
 * @Description: SDUI 动态组件控制反转注册表（隔绝渲染引擎与具体业务组件，支持注册组件全量自动导出 JSON）
 * @Author: 何聚敛
 * @Date: 2026-08-05
 */
package org.example.project.ui.core.sdui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.example.project.core.sdui.model.SduiAction
import org.example.project.core.sdui.model.SduiNode

/**
 * 业务组件渲染 Lambda 扩展签名
 *
 * @param node 动态节点数据
 * @param onAction 节点交互事件分发透传句柄
 */
typealias SduiComponentRenderer = @Composable (node: SduiNode, onAction: (SduiAction) -> Unit) -> Unit

/**
 * SDUI 组件全局依赖倒置注册表
 */
object SduiComponentRegistry {
    private val registry = mutableMapOf<String, SduiComponentRenderer>()

    /**
     * 注册一个新的 SDUI 节点渲染器
     *
     * @param type 关联节点名称（如 "FeedLineTopBar"）
     * @param renderer Compose 视图渲染回调
     */
    fun register(type: String, renderer: SduiComponentRenderer) {
        registry[type] = renderer
    }

    /**
     * 获取当前所有已注册的组件类型名称集合
     */
    fun getRegisteredTypes(): Set<String> = registry.keys.toSet()

    /**
     * 渲染指定 Node 节点
     */
    @Composable
    fun Render(node: SduiNode, onAction: (SduiAction) -> Unit = {}) {
        val renderer = registry[node.componentType]
        if (renderer != null) {
            renderer(node, onAction)
        } else {
            UnknownComponentFallback(node)
        }
    }

    /**
     * 零手动配置：根据当前注册表中所有已注册的组件，一键全自动生成完整的 SDUI 节点树及 JSON DSL 字符串
     *
     * @param containerType 根容器类型（默认 "LazyColumn"）
     * @return 自动拼装生成的 JSON 字符串
     */
    fun exportRegisteredLayoutJson(containerType: String = "LazyColumn"): String {
        val rootNode = SduiNode(
            componentType = containerType,
            children = registry.keys.map { compType ->
                SduiNode(
                    componentType = compType,
                    properties = mapOf("autoExported" to "true"),
                    actions = mapOf("onClick" to SduiAction(type = "ACTION_${compType.uppercase()}"))
                )
            }
        )
        return Json.encodeToString(SduiNode.serializer(), rootNode)
    }

    /**
     * 未识别节点的优雅降级提示
     */
    @Composable
    private fun UnknownComponentFallback(node: SduiNode) {
        Box(modifier = Modifier.padding(8.dp)) {
            Text(
                text = "未可识别动态组件: ${node.componentType}",
                color = Color.Red
            )
        }
    }
}
