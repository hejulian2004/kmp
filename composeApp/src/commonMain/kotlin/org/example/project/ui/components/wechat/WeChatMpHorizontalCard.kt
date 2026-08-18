/**
 * @File: WeChatMpHorizontalCard.kt
 * @Package: org.example.project.ui.components.wechat
 * @Description: 微信公众号单列左右图文与通栏大图卡片组件（相对布局弹性适配）
 * @Author: 何聚敛
 * @Date: 2026-08-18
 */
package org.example.project.ui.components.wechat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import kotlinproject.composeapp.generated.resources.Res
import kotlinproject.composeapp.generated.resources.wechat_mp_followed_tag
import org.example.project.data.repository.wechat.createMockWaterfallArticles
import org.example.project.domain.model.wechat.WeChatArticle
import org.example.project.domain.model.wechat.WeChatCardType
import org.example.project.ui.theme.wechat.WeChatBackgroundGray
import org.example.project.ui.theme.wechat.WeChatCloseIconGray
import org.example.project.ui.theme.wechat.WeChatSurfaceWhite
import org.example.project.ui.theme.wechat.WeChatTagBlue
import org.example.project.ui.theme.wechat.WeChatTagBlueBg
import org.example.project.ui.theme.wechat.WeChatTextPrimary
import org.example.project.ui.theme.wechat.WeChatTextSecondary
import org.example.project.ui.theme.wechat.WeChatTheme
import org.jetbrains.compose.resources.stringResource

@Composable
fun WeChatMpHorizontalCard(
    article: WeChatArticle,
    modifier: Modifier = Modifier,
    onArticleClick: (WeChatArticle) -> Unit = {},
    onDislikeClick: (WeChatArticle) -> Unit = {}
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = WeChatSurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onArticleClick(article) }
    ) {
        if (article.cardType == WeChatCardType.BANNER_LARGE) {
            // 单列通栏大图模式 (如EMS录取通知书)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = article.title,
                    color = WeChatTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 22.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(article.coverAspectRatio.coerceAtLeast(1.6f))
                        .clip(RoundedCornerShape(6.dp))
                        .background(WeChatBackgroundGray)
                ) {
                    AsyncImage(
                        model = article.coverUrl,
                        contentDescription = article.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (article.isFollowedAccount) {
                            WeChatFollowedTag()
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            text = article.account.name,
                            color = WeChatTextSecondary,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(
                        onClick = { onDislikeClick(article) },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dislike",
                            tint = WeChatCloseIconGray,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        } else {
            // 单列左文右图模式 (如腾讯招聘、Grok4.7)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp)
                ) {
                    Text(
                        text = article.title,
                        color = WeChatTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 20.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            if (article.isFollowedAccount) {
                                WeChatFollowedTag()
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(
                                text = article.account.name,
                                color = WeChatTextSecondary,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        IconButton(
                            onClick = { onDislikeClick(article) },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dislike",
                                tint = WeChatCloseIconGray,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(WeChatBackgroundGray)
                ) {
                    AsyncImage(
                        model = article.coverUrl,
                        contentDescription = article.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )
                }
            }
        }
    }
}

@Composable
fun WeChatFollowedTag(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(3.dp))
            .background(WeChatTagBlueBg)
            .padding(horizontal = 4.dp, vertical = 1.dp)
    ) {
        Text(
            text = stringResource(Res.string.wechat_mp_followed_tag),
            color = WeChatTagBlue,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Preview(showBackground = true)
@Composable
fun WeChatMpHorizontalCardPreview() {
    WeChatTheme {
        WeChatMpHorizontalCard(article = createMockWaterfallArticles().first())
    }
}
