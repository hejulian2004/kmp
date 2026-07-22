/**
 * @File: FeedLineConfirmDialog.kt
 * @Package: org.example.project.ui.components.feedline
 * @Description: 朋友圈确认二次弹窗通用组件（基于Compose Material3跨平台适配）
 * @Author: 何聚敛
 * @Date: 2026-07-22
 */
package org.example.project.ui.components.feedline

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun FeedLineConfirmDialog(
    title: String,
    text: String,
    confirmText: String = "删除",
    dismissText: String = "取消",
    onConfirmClick: () -> Unit,
    onDismissClick: () -> Unit,
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = {
            TextButton(onClick = onConfirmClick) {
                Text(confirmText, color = Color.Red)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissClick) {
                Text(dismissText, color = Color.Black)
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun FeedLineConfirmDialogPreview(){
    FeedLineConfirmDialog(
        title = "确认删除",
        text = "确定要删除这条动态吗？删除后不可恢复",
        confirmText = "删除",
        dismissText = "取消",
        onConfirmClick = {},
        onDismissClick = {},
        onDismissRequest = {}
    )
}
