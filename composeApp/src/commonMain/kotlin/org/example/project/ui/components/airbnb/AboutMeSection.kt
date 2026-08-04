/**
 * @File: AboutMeSection.kt
 * @Package: org.example.project.ui.components.airbnb
 * @Description: Airbnb 个人简介编辑 UI 区域
 * @Author: 何聚敛
 * @Date: 2026-08-04
 */
package org.example.project.ui.components.airbnb

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.example.project.ui.theme.airbnb.Accent
import org.example.project.ui.theme.airbnb.AirbnbTheme
import org.example.project.ui.theme.airbnb.CardBg
import org.example.project.ui.theme.airbnb.DividerColor
import org.example.project.ui.theme.airbnb.TextPrimary
import org.example.project.ui.theme.airbnb.TextSecondary
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun AboutMeSection(
    text: String,
    onTextChange: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    maxLength: Int = 500,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "关于我",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = text,
            onValueChange = { if (it.length <= maxLength) onTextChange(it) },
            placeholder = {
                Text(
                    "向房客介绍你自己、你的兴趣和租房理念...",
                    color = TextSecondary,
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Accent,
                unfocusedBorderColor = DividerColor,
                cursorColor = Accent,
                focusedContainerColor = CardBg,
                unfocusedContainerColor = CardBg,
            ),
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = "${text.length}/$maxLength",
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AboutMeSectionPreview() {
    AirbnbTheme {
        AboutMeSection(text = "Hello")
    }
}
