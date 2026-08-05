/**
 * @File: NetworkImage.kt
 * @Package: org.example.project.ui.components.airbnb
 * @Description: Airbnb 模块网络图片加载封装组件
 * @Author: 何聚敛
 * @Date: 2026-08-05
 */
package org.example.project.ui.components.airbnb

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import org.example.project.ui.theme.airbnb.ImagePlaceholder
import org.example.project.ui.theme.airbnb.TextSecondary

@Composable
fun NetworkImage(
    imageUrl: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    if (imageUrl.isBlank()) {
        Box(
            modifier = modifier.background(ImagePlaceholder),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "图片加载失败",
                color = TextSecondary,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    } else {
        AsyncImage(
            model = imageUrl,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    }
}
