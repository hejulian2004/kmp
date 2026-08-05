/**
 * @File: ReviewCard.kt
 * @Package: org.example.project.ui.components.airbnb
 * @Description: Airbnb 房东评价卡片 UI 组件
 * @Author: 何聚敛
 * @Date: 2026-08-05
 */
package org.example.project.ui.components.airbnb

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.domain.model.airbnb.HostReview
import org.example.project.ui.theme.airbnb.AirbnbTheme
import org.example.project.ui.theme.airbnb.CardBg
import org.example.project.ui.theme.airbnb.TextPrimary
import org.example.project.ui.theme.airbnb.TextSecondary

@Composable
fun ReviewCard(review: HostReview) {
    Surface(
        modifier = Modifier
            .width(280.dp)
            .height(210.dp),
        shape = CardShape,
        color = CardBg,
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                NetworkImage(
                    imageUrl = review.reviewerAvatarUrl,
                    contentDescription = review.reviewerName,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        text = review.reviewerName,
                        color = TextPrimary,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = review.reviewerLocation,
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            Text(
                text = "${"★".repeat(review.stars.coerceIn(0, 5))} · ${review.dateText}",
                color = TextPrimary,
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = review.content,
                color = TextPrimary,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ReviewCardPreview() {
    AirbnbTheme {
        ReviewCard(
            review = HostReview(
                id = "r1", hostId = "1", reviewerName = "Yoshimi", reviewerLocation = "达拉斯", reviewerAvatarUrl = "", stars = 5, dateText = "2周前", content = "很喜欢这里，入住极其方便安全。"
            )
        )
    }
}
