/**
 * @File: FeedLineTheme.kt
 * @Package: org.example.project.ui.theme.feedline
 * @Description: 朋友圈（FeedLine）模块专属主题配置
 * @Author: 何聚敛
 * @Date: 2026-07-23
 */
package org.example.project.ui.theme.feedline

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * 朋友圈专属主题
 */
@Composable
fun FeedLineTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = FeedLinePrimaryGreen,
            background = FeedLineBackgroundGray,
            surface = Color.White
        ),
        content = content
    )
}
