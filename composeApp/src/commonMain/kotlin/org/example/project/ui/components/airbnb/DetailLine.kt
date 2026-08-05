/**
 * @File: DetailLine.kt
 * @Package: org.example.project.ui.components.airbnb
 * @Description: Airbnb 详情行单项 UI 组件
 * @Author: 何聚敛
 * @Date: 2026-08-05
 */
package org.example.project.ui.components.airbnb

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.ui.theme.airbnb.AirbnbTheme
import org.example.project.ui.theme.airbnb.TextPrimary

@Composable
fun DetailLine(icon: String, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(icon, fontSize = 18.sp)
        Spacer(Modifier.width(8.dp))
        Text(
            text = text,
            color = TextPrimary,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DetailLinePreview() {
    AirbnbTheme {
        DetailLine(icon = "🌐", text = "语言：中文、英语")
    }
}
