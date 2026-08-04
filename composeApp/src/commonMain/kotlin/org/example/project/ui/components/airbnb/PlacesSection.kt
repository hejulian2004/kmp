/**
 * @File: PlacesSection.kt
 * @Package: org.example.project.ui.components.airbnb
 * @Description: Airbnb 去过的地点与纪念邮戳展示 UI 组件
 * @Author: 何聚敛
 * @Date: 2026-08-04
 */
package org.example.project.ui.components.airbnb

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.ui.theme.airbnb.Accent
import org.example.project.ui.theme.airbnb.AirbnbTheme
import org.example.project.ui.theme.airbnb.CardBg
import org.example.project.ui.theme.airbnb.DividerColor
import org.example.project.ui.theme.airbnb.TextPrimary
import org.example.project.ui.theme.airbnb.TextSecondary

val destinationEmojis = mapOf(
    "东京" to "🗼", "巴黎" to "🗼", "纽约" to "🗽", "伦敦" to "🎡",
    "悉尼" to "🏄", "冰岛" to "🌋", "巴厘岛" to "🌴", "罗马" to "🏛",
    "香港" to "🌃", "上海" to "🏙", "首尔" to "🇰🇷", "曼谷" to "🙏",
    "新加坡" to "🦁", "洛杉矶" to "🎬", "旧金山" to "🌉", "大阪" to "🏯",
)

@Composable
fun PlacesSection(
    places: List<String>,
    isVisible: Boolean,
    onToggle: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "去过的地点",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
            )
            Switch(
                checked = isVisible,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(checkedTrackColor = Accent),
            )
        }

        Spacer(Modifier.height(4.dp))

        Text(
            text = "展示你曾经游览过的城市与纪念图章。",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
        )

        Spacer(Modifier.height(12.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(places, key = { it }) { place ->
                DestinationStamp(
                    name = place,
                    emoji = destinationEmojis[place] ?: "📍",
                )
            }
        }
    }
}

@Composable
fun DestinationStamp(
    name: String,
    emoji: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.size(width = 100.dp, height = 120.dp),
        shape = RoundedCornerShape(12.dp),
        color = CardBg,
        border = BorderStroke(1.dp, DividerColor),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(text = emoji, fontSize = 32.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.bodySmall,
                color = TextPrimary,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PlacesSectionPreview() {
    AirbnbTheme {
        PlacesSection(places = listOf("东京", "巴黎", "纽约"), isVisible = true)
    }
}
