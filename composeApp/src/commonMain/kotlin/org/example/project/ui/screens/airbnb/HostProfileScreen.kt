/**
 * @File: HostProfileScreen.kt
 * @Package: org.example.project.ui.screens.airbnb
 * @Description: Airbnb 房东主页 Screen 容器视图组件（符合 MVI 架构，全量保留原 UI 样式与注释）
 * @Author: 何聚敛
 * @Date: 2026-08-05
 */
package org.example.project.ui.screens.airbnb

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.domain.model.airbnb.Host
import org.example.project.presentation.intent.airbnb.HostProfileIntent
import org.example.project.presentation.state.airbnb.HostProfileUiState
import org.example.project.ui.components.airbnb.ActionItem
import org.example.project.ui.components.airbnb.CardShape
import org.example.project.ui.components.airbnb.DestinationStamp
import org.example.project.ui.components.airbnb.DetailLine
import org.example.project.ui.components.airbnb.HostSelector
import org.example.project.ui.components.airbnb.ListingCard
import org.example.project.ui.components.airbnb.ProfileHeroCard
import org.example.project.ui.components.airbnb.ReviewCard
import org.example.project.ui.components.airbnb.SectionCard
import org.example.project.ui.components.airbnb.SectionTitle
import org.example.project.ui.components.airbnb.TopBar
import org.example.project.ui.components.airbnb.destinationEmojis
import org.example.project.ui.theme.airbnb.Accent
import org.example.project.ui.theme.airbnb.AirbnbTheme
import org.example.project.ui.theme.airbnb.DividerColor
import org.example.project.ui.theme.airbnb.GuideBg
import org.example.project.ui.theme.airbnb.PageBg
import org.example.project.ui.theme.airbnb.TextPrimary
import org.example.project.ui.theme.airbnb.TextSecondary

private const val ProfileSectionGap = 16

@Composable
fun HostProfileScreen(
    uiState: HostProfileUiState,
    onIntent: (HostProfileIntent) -> Unit = {},
    onEditHostClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
) {
    if (uiState.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PageBg)
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = Accent)
                Spacer(Modifier.height(12.dp))
                Text("加载中…", color = TextSecondary)
            }
        }
        return
    }

    uiState.errorMessage?.let { error ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PageBg)
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = error,
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { onIntent(HostProfileIntent.LoadData) },
                    colors = ButtonDefaults.buttonColors(containerColor = Accent),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text("重试", color = Color.White)
                }
            }
        }
        return
    }

    val host = uiState.selectedHost ?: return
    val reviews = uiState.selectedHostReviews
    val listings = uiState.selectedHostProperties
    val guide = uiState.selectedHostGuides.firstOrNull()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBg)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.TopCenter,
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 840.dp)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(ProfileSectionGap.dp),
        ) {
            item {
                TopBar(
                    title = "个人资料",
                    actionText = "编辑",
                    onActionClick = onEditHostClick,
                )
            }

            item {
                HostSelector(
                    hosts = uiState.hosts,
                    selectedHostId = host.id,
                    onHostSelected = { onIntent(HostProfileIntent.SelectHost(it)) },
                )
            }

            item { ProfileHeroCard(host) }

            item {
                SectionCard {
                    DetailLine(icon = "🌐", text = "语言：${host.languages}")
                    DetailLine(icon = "🛡", text = if (host.identityVerified) "身份已验证" else "身份未验证")
                    if (host.occupation.isNotEmpty()) {
                        DetailLine(icon = "💼", text = host.occupation)
                    }
                    if (host.livesIn.isNotEmpty()) {
                        DetailLine(icon = "📍", text = host.livesIn)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = host.about,
                        color = TextPrimary,
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 24.sp,
                    )
                }
            }

            item {
                SectionTitle("${host.name}的兴趣爱好")
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    host.hobbies.forEach { hobby ->
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, DividerColor),
                        ) {
                            Text(
                                text = hobby,
                                color = TextPrimary,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
            }

            if (host.placesVisible && host.places.isNotEmpty()) {
                item {
                    SectionTitle("${host.name}去过的地点")
                }

                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(host.places, key = { it }) { place ->
                            DestinationStamp(
                                name = place,
                                emoji = destinationEmojis[place] ?: "📍",
                            )
                        }
                    }
                }
            }

            item {
                SectionTitle("评价（${reviews.size}条）")
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(reviews, key = { it.id }) { review ->
                        ReviewCard(review)
                    }
                }
            }

            item {
                SectionTitle("${host.name}的房源")
            }

            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(listings, key = { it.id }) { listing ->
                        ListingCard(listing)
                    }
                }
            }

            item {
                Text(
                    text = "查看全部 ${host.totalListings} 套房源 ›",
                    color = TextPrimary,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            item {
                SectionTitle("${host.name}的旅行指南")
            }

            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(148.dp),
                    shape = CardShape,
                    color = GuideBg,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.BottomStart,
                    ) {
                        Text(
                            text = guide?.title ?: "${host.name}的旅行指南",
                            color = TextPrimary,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            item {
                HorizontalDivider(color = DividerColor)
            }

            item {
                ActionItem(icon = "⚑", label = "举报${host.name}")
                ActionItem(icon = "✍", label = "给${host.name}写评价")
                ActionItem(icon = "⊘", label = "屏蔽${host.name}")
            }

            item {
                HorizontalDivider(color = DividerColor)
            }

            item {
                ActionItem(
                    icon = "⚙",
                    label = "系统设置",
                    onClick = onSettingsClick,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HostProfileScreenPreview() {
    AirbnbTheme {
        HostProfileScreen(
            uiState = HostProfileUiState(
                hosts = listOf(
                    Host(
                        id = "art-room-hk",
                        name = "ArtRoomHK",
                        reviewCount = 2066,
                        rating = 4.85,
                        yearsHosting = 7,
                        totalListings = 11,
                        languages = "中文和英语",
                        identityVerified = true,
                        superHost = true,
                        about = "ArtRoom 是一个极具艺术气息的空间。",
                        occupation = "艺术家 / 策展人",
                        livesIn = "香港",
                        hobbies = listOf("艺术展览", "城市散步"),
                        places = listOf("东京", "巴黎"),
                        avatarUrl = ""
                    )
                ),
                selectedHostId = "art-room-hk"
            )
        )
    }
}
