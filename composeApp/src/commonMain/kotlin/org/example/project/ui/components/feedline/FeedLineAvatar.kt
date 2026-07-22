/**
 * @File: FeedLineAvatar.kt
 * @Package: org.example.project.ui.components.feedline
 * @Description: 朋友圈头像组件
 * @Author: 何聚敛
 * @Date: 2026-07-20
 */
package org.example.project.ui.components.feedline

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

@Composable
fun Avatar(
    modifier: Modifier = Modifier,
    url: String,
    size: Dp,
    cornerRadius: Dp = 6.dp,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)

    Surface(
        modifier = modifier
            .size(size)
            .clip(shape),
        shape = shape,
        color = Color.Gray,
        onClick = {
            onClick()
        }
    ) {
        AsyncImage(
            model = url,
            contentDescription = "头像",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AvatarPreview() {
    Avatar(
        url = "https://i.pravatar.cc/300",
        size = 50.dp,
        onClick = {}
    )
}


