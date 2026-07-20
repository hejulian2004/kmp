package org.example.project.ui.components.feed

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

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
            text = "拍摄照片",
            onClick = onTakePhotoClick
        )
        HorizontalDivider(
            thickness = 0.5.dp,
            color = Color.LightGray
        )
        BottomSheetItem(
            text = "录制视频",
            onClick = onRecordVideoClick
        )
        HorizontalDivider(
            thickness = 0.5.dp,
            color = Color.LightGray
        )
        BottomSheetItem(
            text = "从手机相册选择",
            onClick = onChooseClick
        )
        HorizontalDivider(
            thickness = 0.5.dp,
            color = Color.LightGray
        )
        BottomSheetItem(
            text = "取消",
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
