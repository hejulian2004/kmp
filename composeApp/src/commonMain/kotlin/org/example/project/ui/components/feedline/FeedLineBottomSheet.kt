/**
 * @File: FeedLineBottomSheet.kt
 * @Package: org.example.project.ui.components.feedline
 * @Description: 朋友圈底部弹出选择菜单组件
 * @Author: 何聚敛
 * @Date: 2026-07-20
 */
package org.example.project.ui.components.feedline

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

import kotlinproject.composeapp.generated.resources.Res
import kotlinproject.composeapp.generated.resources.cancel
import kotlinproject.composeapp.generated.resources.feedline_select_from_album
import kotlinproject.composeapp.generated.resources.feedline_take_photo
import kotlinproject.composeapp.generated.resources.feedline_take_video
import org.jetbrains.compose.resources.stringResource

@Composable
fun BottomSheet(
    modifier: Modifier = Modifier,
    onTakePhotoClick: () -> Unit,
    onRecordVideoClick: () -> Unit,
    onChooseClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp)
    ) {
        BottomSheetItem(
            text = stringResource(Res.string.feedline_take_photo),
            onClick = onTakePhotoClick
        )
        HorizontalDivider(
            thickness = 0.5.dp,
            color = Color.LightGray
        )
        BottomSheetItem(
            text = stringResource(Res.string.feedline_take_video),
            onClick = onRecordVideoClick
        )
        HorizontalDivider(
            thickness = 0.5.dp,
            color = Color.LightGray
        )
        BottomSheetItem(
            text = stringResource(Res.string.feedline_select_from_album),
            onClick = onChooseClick
        )
        HorizontalDivider(
            thickness = 0.5.dp,
            color = Color.LightGray
        )
        BottomSheetItem(
            text = stringResource(Res.string.cancel),
            onClick = onCancelClick
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BottomSheetPreview() {
    BottomSheet(
        onTakePhotoClick = {},
        onRecordVideoClick = {},
        onChooseClick = {},
        onCancelClick = {}
    )
}


