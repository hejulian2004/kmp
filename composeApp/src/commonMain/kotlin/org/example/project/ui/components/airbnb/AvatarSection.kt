/**
 * @File: AvatarSection.kt
 * @Package: org.example.project.ui.components.airbnb
 * @Description: Airbnb 房东头像展示与选择 UI 组件
 * @Author: 何聚敛
 * @Date: 2026-08-04
 */
package org.example.project.ui.components.airbnb

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import org.example.project.ui.theme.airbnb.AirbnbTheme

private val AvatarSize = 120.dp
private val CameraIconSize = 36.dp

@Composable
fun AvatarSection(
    avatarUrl: String,
    modifier: Modifier = Modifier,
    onAvatarClick: () -> Unit = {},
) {
    Box(
        modifier = modifier.size(AvatarSize),
        contentAlignment = Alignment.BottomEnd,
    ) {
        NetworkImage(
            imageUrl = avatarUrl,
            contentDescription = "头像",
            modifier = Modifier
                .size(AvatarSize)
                .clip(CircleShape)
                .clickable { onAvatarClick() },
            contentScale = ContentScale.Crop,
        )

        Box(
            modifier = Modifier
                .size(CameraIconSize)
                .clip(CircleShape)
                .background(Color.White)
                .clickable { onAvatarClick() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Default.CameraAlt,
                contentDescription = "更换头像",
                tint = Color.Black,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AvatarSectionPreview() {
    AirbnbTheme {
        AvatarSection(avatarUrl = "")
    }
}
