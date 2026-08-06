/**
 * @File: ProfileFieldItem.kt
 * @Package: org.example.project.ui.components.airbnb
 * @Description: Airbnb个人资料单项信息展示与点击编辑栏
 * @Author: 何聚敛
 * @Date: 2026-08-05
 */
package org.example.project.ui.components.airbnb

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.example.project.ui.theme.airbnb.AirbnbTheme
import org.example.project.ui.theme.airbnb.DividerColor
import org.example.project.ui.theme.airbnb.TextPrimary
import org.example.project.ui.theme.airbnb.TextSecondary

@Composable
fun ProfileFieldItem(
    label: String,
    value: String,
    onClick: () -> Unit = {},
    showDivider: Boolean = true,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
    ) {
        Text(
            text = label,
            color = TextSecondary,
            style = MaterialTheme.typography.labelMedium,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = value.ifEmpty { "未填写" },
            color = if (value.isEmpty()) TextSecondary else TextPrimary,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Normal,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
    if (showDivider) {
        HorizontalDivider(color = DividerColor)
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileFieldItemPreview() {
    AirbnbTheme {
        ProfileFieldItem(label = "职业", value = "艺术家 / 策展人")
    }
}
