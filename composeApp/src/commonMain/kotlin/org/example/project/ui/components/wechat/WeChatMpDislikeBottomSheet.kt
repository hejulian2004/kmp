/**
 * @File: WeChatMpDislikeBottomSheet.kt
 * @Package: org.example.project.ui.components.wechat
 * @Description: 微信公众号不感兴趣与内容屏蔽操作弹窗
 * @Author: 何聚敛
 * @Date: 2026-08-18
 */
package org.example.project.ui.components.wechat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinproject.composeapp.generated.resources.Res
import kotlinproject.composeapp.generated.resources.wechat_mp_cancel
import kotlinproject.composeapp.generated.resources.wechat_mp_dislike_account
import kotlinproject.composeapp.generated.resources.wechat_mp_dislike_report
import kotlinproject.composeapp.generated.resources.wechat_mp_dislike_title
import kotlinproject.composeapp.generated.resources.wechat_mp_dislike_topic
import org.example.project.domain.model.wechat.WeChatArticle
import org.example.project.ui.theme.wechat.WeChatDividerGray
import org.example.project.ui.theme.wechat.WeChatSurfaceWhite
import org.example.project.ui.theme.wechat.WeChatTextMuted
import org.example.project.ui.theme.wechat.WeChatTextPrimary
import org.example.project.ui.theme.wechat.WeChatTextSecondary
import org.example.project.ui.theme.wechat.WeChatTheme
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeChatMpDislikeBottomSheet(
    targetArticle: WeChatArticle?,
    modifier: Modifier = Modifier,
    onSelectReason: (String) -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    if (targetArticle == null) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = WeChatSurfaceWhite,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 16.dp)
        ) {
            Text(
                text = stringResource(Res.string.wechat_mp_dislike_title),
                color = WeChatTextMuted,
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            )

            HorizontalDivider(color = WeChatDividerGray, thickness = 0.5.dp)

            DislikeActionRow(
                icon = Icons.Default.Block,
                title = stringResource(Res.string.wechat_mp_dislike_account),
                subtitle = targetArticle.account.name,
                onClick = { onSelectReason("不感兴趣此公众号: ${targetArticle.account.name}") }
            )

            DislikeActionRow(
                icon = Icons.Default.ThumbDown,
                title = stringResource(Res.string.wechat_mp_dislike_topic),
                subtitle = "减少类似话题推送",
                onClick = { onSelectReason("减少此类内容推荐") }
            )

            DislikeActionRow(
                icon = Icons.Default.Report,
                title = stringResource(Res.string.wechat_mp_dislike_report),
                subtitle = "内容违规或低质",
                onClick = { onSelectReason("投诉该文章") }
            )

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = WeChatDividerGray, thickness = 6.dp)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onDismiss)
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.wechat_mp_cancel),
                    color = WeChatTextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun DislikeActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = WeChatTextSecondary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                color = WeChatTextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal
            )
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    color = WeChatTextMuted,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WeChatMpDislikeBottomSheetPreview() {
    WeChatTheme {
        WeChatMpDislikeBottomSheet(
            targetArticle = org.example.project.data.repository.wechat.createMockFeaturedArticle()
        )
    }
}
