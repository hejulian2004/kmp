/**
 * @File: HostSelector.kt
 * @Package: org.example.project.ui.components.airbnb
 * @Description: Airbnb房东选择标签栏组件
 * @Author: 何聚敛
 * @Date: 2026-08-05
 */
package org.example.project.ui.components.airbnb

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.example.project.domain.model.airbnb.Host
import org.example.project.ui.theme.airbnb.Accent
import org.example.project.ui.theme.airbnb.AirbnbTheme
import org.example.project.ui.theme.airbnb.CardBg
import org.example.project.ui.theme.airbnb.DividerColor
import org.example.project.ui.theme.airbnb.TextPrimary

@Composable
fun HostSelector(
    hosts: List<Host>,
    selectedHostId: String,
    onHostSelected: (String) -> Unit = {},
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(hosts, key = { it.id }) { host ->
            val selected = host.id == selectedHostId
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = if (selected) Accent else CardBg,
                border = BorderStroke(1.dp, if (selected) Accent else DividerColor),
                modifier = Modifier.clickable { onHostSelected(host.id) },
            ) {
                Text(
                    text = host.name,
                    color = if (selected) Color.White else TextPrimary,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HostSelectorPreview() {
    AirbnbTheme {
        HostSelector(
            hosts = listOf(
                Host(
                    id = "1", name = "ArtRoomHK", reviewCount = 10, rating = 4.8, yearsHosting = 2, totalListings = 3,
                    languages = "中文", identityVerified = true, superHost = true, about = "", hobbies = emptyList(), avatarUrl = ""
                )
            ),
            selectedHostId = "1"
        )
    }
}
