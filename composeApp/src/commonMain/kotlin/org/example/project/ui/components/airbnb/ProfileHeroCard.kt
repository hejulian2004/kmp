/**
 * @File: ProfileHeroCard.kt
 * @Package: org.example.project.ui.components.airbnb
 * @Description: Airbnb 房东主卡片 UI 组件
 * @Author: 何聚敛
 * @Date: 2026-08-04
 */
package org.example.project.ui.components.airbnb

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.example.project.domain.model.airbnb.Host
import org.example.project.ui.theme.airbnb.AirbnbTheme
import org.example.project.ui.theme.airbnb.TextPrimary
import org.example.project.ui.theme.airbnb.TextSecondary
import org.example.project.ui.utils.airbnb.formatDecimal

@Composable
fun ProfileHeroCard(host: Host) {
    SectionCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NetworkImage(
                imageUrl = host.avatarUrl,
                contentDescription = host.name,
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape),
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = host.name,
                    color = TextPrimary,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (host.superHost) "超赞房东" else "房东",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "★ ${formatDecimal(host.rating)} · ${host.reviewCount}条评价 · 接待${host.yearsHosting}年",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileHeroCardPreview() {
    AirbnbTheme {
        ProfileHeroCard(
            host = Host(
                id = "1", name = "ArtRoomHK", reviewCount = 2066, rating = 4.85, yearsHosting = 7, totalListings = 11,
                languages = "中文和英语", identityVerified = true, superHost = true, about = "", hobbies = emptyList(), avatarUrl = ""
            )
        )
    }
}
