/**
 * @File: WeChatMpFrequentlyReadBar.kt
 * @Package: org.example.project.ui.components.wechat
 * @Description: 微信公众号常读号横向滚动列表条组件（包含未读绿点与点击交互）
 * @Author: 何聚敛
 * @Date: 2026-08-18
 */
package org.example.project.ui.components.wechat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import kotlinproject.composeapp.generated.resources.Res
import kotlinproject.composeapp.generated.resources.wechat_mp_frequently_read
import org.example.project.data.repository.wechat.createMockWeChatAccounts
import org.example.project.domain.model.wechat.WeChatAccount
import org.example.project.ui.theme.wechat.WeChatBackgroundGray
import org.example.project.ui.theme.wechat.WeChatSurfaceWhite
import org.example.project.ui.theme.wechat.WeChatTextMuted
import org.example.project.ui.theme.wechat.WeChatTextSecondary
import org.example.project.ui.theme.wechat.WeChatTheme
import org.example.project.ui.theme.wechat.WeChatUnreadGreenDot
import org.jetbrains.compose.resources.stringResource

@Composable
fun WeChatMpFrequentlyReadBar(
    accounts: List<WeChatAccount>,
    modifier: Modifier = Modifier,
    onAccountClick: (WeChatAccount) -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(WeChatBackgroundGray)
            .padding(top = 4.dp, bottom = 12.dp)
    ) {
        Text(
            text = stringResource(Res.string.wechat_mp_frequently_read),
            color = WeChatTextMuted,
            fontSize = 12.sp,
            modifier = Modifier.padding(start = 16.dp, bottom = 10.dp)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(accounts, key = { it.id }) { account ->
                FrequentlyReadAccountItem(
                    account = account,
                    onClick = { onAccountClick(account) }
                )
            }
        }
    }
}

@Composable
private fun FrequentlyReadAccountItem(
    account: WeChatAccount,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .width(58.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(48.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            AsyncImage(
                model = account.avatarUrl,
                contentDescription = account.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(WeChatSurfaceWhite)
            )

            if (account.hasUnread) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(WeChatUnreadGreenDot)
                        .border(1.5.dp, WeChatBackgroundGray, CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = account.name,
            color = WeChatTextSecondary,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun WeChatMpFrequentlyReadBarPreview() {
    WeChatTheme {
        WeChatMpFrequentlyReadBar(accounts = createMockWeChatAccounts())
    }
}
