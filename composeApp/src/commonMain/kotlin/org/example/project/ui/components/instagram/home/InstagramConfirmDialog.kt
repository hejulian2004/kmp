/**
 * @File: InstagramConfirmDialog.kt
 * @Package: org.example.project.ui.components.instagram.home
 * @Description: Instagram风格删除/操作二次确认对话框
 * @Author: 何聚敛
 * @Date: 2026-07-29
 */
package org.example.project.ui.components.instagram.home

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import org.example.project.ui.theme.instagram.InstagramRed
import org.example.project.ui.theme.instagram.InstagramTheme

/**
 * Instagram风格二次确认操作对话框
 *
 * @param onDismissRequest 关闭对话框请求回调
 * @param title 对话框标题
 * @param text 对话框详细提示文案
 * @param confirmText 确认按钮文本
 * @param dismissText 取消按钮文本
 * @param onConfirmClick 点击确认按钮回调
 * @param onDismissClick 点击取消按钮回调
 */
@Composable
fun InstagramConfirmDialog(
    onDismissRequest: () -> Unit,
    title: String = "Confirm Action",
    text: String = "Are you sure you want to proceed?",
    confirmText: String = "Delete",
    dismissText: String = "Cancel",
    onConfirmClick: () -> Unit,
    onDismissClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Text(
                text = text,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirmClick) {
                Text(
                    text = confirmText,
                    color = InstagramRed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissClick) {
                Text(
                    text = dismissText,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp
                )
            }
        }
    )
}

/**
 * Instagram风格二次确认操作对话框Composable预览函数
 */
@Preview
@Composable
fun InstagramConfirmDialogPreview() {
    InstagramTheme {
        InstagramConfirmDialog(
            onDismissRequest = {},
            title = "Delete Post?",
            text = "Are you sure you want to delete this post? This action cannot be undone.",
            confirmText = "Delete",
            dismissText = "Cancel",
            onConfirmClick = {},
            onDismissClick = {}
        )
    }
}
