/**
 * @File: SduiRenderer.kt
 * @Package: org.example.project.ui.core.sdui
 * @Description: SDUI 动态 Compose 递归渲染引擎入口
 * @Author: 何聚敛
 * @Date: 2026-08-04
 */
package org.example.project.ui.core.sdui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.core.sdui.model.SduiAction
import org.example.project.core.sdui.model.SduiNode
import org.example.project.core.sdui.model.SduiStyle

/**
 * SDUI 核心递归渲染器
 *
 * @param node 要渲染的 SDUI 节点树
 * @param onAction 组件触发 Action 时的透传句柄
 */
@Composable
fun SduiRenderer(
    node: SduiNode,
    onAction: (SduiAction) -> Unit = {}
) {
    if (!node.style.isVisible) return

    val modifier = Modifier.applyStyle(node.style)

    when (node.componentType) {
        "Column" -> {
            Column(modifier = modifier) {
                node.children.forEach { child ->
                    SduiRenderer(node = child, onAction = onAction)
                }
            }
        }
        "Row" -> {
            Row(modifier = modifier) {
                node.children.forEach { child ->
                    SduiRenderer(node = child, onAction = onAction)
                }
            }
        }
        "Card" -> {
            Card(modifier = modifier) {
                Column(modifier = Modifier.padding(12.dp)) {
                    node.children.forEach { child ->
                        SduiRenderer(node = child, onAction = onAction)
                    }
                }
            }
        }
        "LazyColumn" -> {
            LazyColumn(modifier = modifier) {
                items(node.children) { child ->
                    SduiRenderer(node = child, onAction = onAction)
                }
            }
        }
        "Text" -> {
            val textContent = node.properties["text"] ?: ""
            Text(
                text = textContent,
                fontSize = node.style.fontSizeSp.sp,
                fontWeight = if (node.style.fontWeight == "Bold") FontWeight.Bold else FontWeight.Normal,
                modifier = modifier
            )
        }
        "Button" -> {
            val label = node.properties["text"] ?: "按钮"
            Button(
                onClick = {
                    node.actions["onClick"]?.let { action ->
                        onAction(action)
                    }
                },
                modifier = modifier
            ) {
                Text(text = label)
            }
        }
        "Spacer" -> {
            val heightDp = node.properties["height"]?.toIntOrNull() ?: 8
            Spacer(modifier = Modifier.height(heightDp.dp))
        }
        else -> {
            // 未在内置基本容器中的节点，委派给注册表 SduiComponentRegistry 处理
            SduiComponentRegistry.Render(node = node, onAction = onAction)
        }
    }
}

/**
 * 将 SduiStyle 转换为 Compose Modifier
 */
private fun Modifier.applyStyle(style: SduiStyle): Modifier {
    var modifier = this
    if (style.paddingDp > 0) {
        modifier = modifier.padding(style.paddingDp.dp)
    }
    if (style.cornerRadiusDp > 0) {
        modifier = modifier.clip(RoundedCornerShape(style.cornerRadiusDp.dp))
    }
    val bgHex = style.backgroundColorHex
    if (!bgHex.isNullOrBlank()) {
        try {
            val hexClean = bgHex.removePrefix("#")
            val colorInt = hexClean.toLong(16)
            val color = if (hexClean.length == 6) Color(0xFF000000 or colorInt) else Color(colorInt)
            modifier = modifier.background(color)
        } catch (_: Exception) { }
    }
    return modifier
}

@Preview(showBackground = true)
@Composable
fun SduiRendererPreview() {
    val sampleNode = SduiNode(
        componentType = "Card",
        children = listOf(
            SduiNode(
                componentType = "Text",
                properties = mapOf("text" to "动态 SDUI 卡片预览"),
                style = SduiStyle(fontWeight = "Bold", fontSizeSp = 16)
            ),
            SduiNode(
                componentType = "Button",
                properties = mapOf("text" to "点击交互")
            )
        )
    )
    SduiRenderer(node = sampleNode)
}
