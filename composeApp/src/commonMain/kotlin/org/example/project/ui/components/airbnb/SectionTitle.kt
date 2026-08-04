/**
 * @File: SectionTitle.kt
 * @Package: org.example.project.ui.components.airbnb
 * @Description: Airbnb 模块小标题 UI 组件
 * @Author: 何聚敛
 * @Date: 2026-08-04
 */
package org.example.project.ui.components.airbnb

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import org.example.project.ui.theme.airbnb.AirbnbTheme
import org.example.project.ui.theme.airbnb.TextPrimary

@Composable
fun SectionTitle(text: String) {
    Text(
        text = text,
        color = TextPrimary,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.SemiBold,
    )
}

@Preview(showBackground = true)
@Composable
fun SectionTitlePreview() {
    AirbnbTheme {
        SectionTitle("关于房东")
    }
}
