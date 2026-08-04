/**
 * @File: TopBar.kt
 * @Package: org.example.project.ui.components.airbnb
 * @Description: Airbnb 模块顶部导航栏组件
 * @Author: 何聚敛
 * @Date: 2026-08-04
 */
package org.example.project.ui.components.airbnb

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import org.example.project.ui.theme.airbnb.Accent
import org.example.project.ui.theme.airbnb.AirbnbTheme
import org.example.project.ui.theme.airbnb.TextPrimary

@Composable
fun TopBar(
    title: String,
    actionText: String,
    onActionClick: () -> Unit = {},
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            color = TextPrimary,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = actionText,
            color = Accent,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable { onActionClick() },
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TopBarPreview() {
    AirbnbTheme {
        TopBar(title = "个人资料", actionText = "编辑")
    }
}
