/**
 * @File: ListingCard.kt
 * @Package: org.example.project.ui.components.airbnb
 * @Description: Airbnb 房源名片 UI 组件
 * @Author: 何聚敛
 * @Date: 2026-08-04
 */
package org.example.project.ui.components.airbnb

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.example.project.domain.model.airbnb.PropertyListing
import org.example.project.ui.theme.airbnb.AirbnbTheme
import org.example.project.ui.theme.airbnb.CardBg
import org.example.project.ui.theme.airbnb.TextPrimary
import org.example.project.ui.theme.airbnb.TextSecondary
import org.example.project.ui.utils.airbnb.formatDecimal

@Composable
fun ListingCard(listing: PropertyListing) {
    Surface(
        modifier = Modifier.width(280.dp),
        shape = CardShape,
        color = CardBg,
    ) {
        Column {
            NetworkImage(
                imageUrl = listing.imageUrl,
                contentDescription = listing.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
            )
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = listing.title,
                    color = TextPrimary,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = listing.subtitle,
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "★ ${formatDecimal(listing.rating)} (${listing.reviewCount})",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ListingCardPreview() {
    AirbnbTheme {
        ListingCard(
            listing = PropertyListing(
                id = "p1", hostId = "1", title = "酒店式公寓", subtitle = "ArtRoom 6 - 睡眠舱女生共享空间", rating = 4.84, reviewCount = 88, imageUrl = ""
            )
        )
    }
}
