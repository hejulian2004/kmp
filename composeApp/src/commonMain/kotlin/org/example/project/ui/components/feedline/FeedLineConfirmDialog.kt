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

import org.example.project.ui.theme.feedline.FeedLineDangerRed
import org.example.project.ui.theme.feedline.FeedLineTextPrimary
import kotlinproject.composeapp.generated.resources.Res
import kotlinproject.composeapp.generated.resources.cancel
import kotlinproject.composeapp.generated.resources.feedline_confirm_delete
import kotlinproject.composeapp.generated.resources.feedline_delete
import kotlinproject.composeapp.generated.resources.feedline_delete_post_prompt
import org.jetbrains.compose.resources.stringResource

@Composable
fun FeedLineConfirmDialog(
    title: String,
    text: String,
    confirmText: String = stringResource(Res.string.feedline_delete),
    dismissText: String = stringResource(Res.string.cancel),
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
                Text(confirmText, color = FeedLineDangerRed)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissClick) {
                Text(dismissText, color = FeedLineTextPrimary)
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun FeedLineConfirmDialogPreview(){
    FeedLineConfirmDialog(
        title = stringResource(Res.string.feedline_confirm_delete),
        text = stringResource(Res.string.feedline_delete_post_prompt),
        confirmText = stringResource(Res.string.feedline_delete),
        dismissText = stringResource(Res.string.cancel),
        onConfirmClick = {},
        onDismissClick = {},
        onDismissRequest = {}
    )
}
