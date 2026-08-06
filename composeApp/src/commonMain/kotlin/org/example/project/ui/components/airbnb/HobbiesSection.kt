/**
 * @File: HobbiesSection.kt
 * @Package: org.example.project.ui.components.airbnb
 * @Description: Airbnb兴趣爱好列表管理与标签展示UI组件
 * @Author: 何聚敛
 * @Date: 2026-08-05
 */
package org.example.project.ui.components.airbnb

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.example.project.ui.theme.airbnb.Accent
import org.example.project.ui.theme.airbnb.AirbnbTheme
import org.example.project.ui.theme.airbnb.CardBg
import org.example.project.ui.theme.airbnb.DividerColor
import org.example.project.ui.theme.airbnb.TextPrimary
import org.example.project.ui.theme.airbnb.TextSecondary

private const val MAX_HOBBIES = 3

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HobbiesSection(
    hobbies: List<String>,
    onAddHobby: () -> Unit = {},
    onDeleteHobby: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "兴趣爱好",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = "选择你的兴趣，让房客快速了解你的生活方式。",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
        )

        Spacer(Modifier.height(12.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            hobbies.forEach { hobby ->
                HobbyChip(
                    label = hobby,
                    onDelete = onDeleteHobby?.let { { it(hobby) } },
                )
            }

            val emptySlots = MAX_HOBBIES - hobbies.size
            repeat(emptySlots.coerceAtLeast(0)) {
                HobbyEmptySlot()
            }
        }

        Spacer(Modifier.height(12.dp))

        FilledTonalButton(
            onClick = onAddHobby,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = CardBg,
                contentColor = Accent,
            ),
            border = BorderStroke(1.dp, DividerColor),
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text("添加兴趣爱好")
        }
    }
}

@Composable
private fun HobbyChip(
    label: String,
    onDelete: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = Accent.copy(alpha = 0.1f),
    ) {
        Text(
            text = label,
            color = Accent,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .clickable(enabled = onDelete != null) { onDelete?.invoke() }
                .padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }
}

@Composable
private fun HobbyEmptySlot(
    modifier: Modifier = Modifier,
) {
    val dashPathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 4f), 0f)

    Box(
        modifier = modifier
            .size(width = 80.dp, height = 38.dp)
            .drawBehind {
                drawRoundRect(
                    color = DividerColor,
                    style = Stroke(width = 1.5f, pathEffect = dashPathEffect),
                    cornerRadius = CornerRadius(20f, 20f),
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            Icons.Default.Add,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HobbiesSectionPreview() {
    AirbnbTheme {
        HobbiesSection(hobbies = listOf("摄影", "艺术展览"))
    }
}
