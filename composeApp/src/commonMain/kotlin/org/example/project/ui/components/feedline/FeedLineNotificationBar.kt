/**
 * @File: FeedLineNotificationBar.kt
 * @Package: org.example.project.ui.components.feedline
 * @Description: 朋友圈顶部未读消息通知提示栏组件
 * @Author: 何聚敛
 * @Date: 2026-07-20
 */
package org.example.project.ui.components.feedline

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.ui.theme.feedline.FeedLineNotificationBarDarkGray
import kotlinproject.composeapp.generated.resources.Res
import kotlinproject.composeapp.generated.resources.feedline_new_messages_suffix
import kotlinproject.composeapp.generated.resources.feedline_notification_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun FeedNotificationBar(
    unreadCount: Int,
    latestNotificationUserAvatar: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (unreadCount <= 0) return

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(FeedLineNotificationBarDarkGray)
                .clickable { onClick() }
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (latestNotificationUserAvatar != null) {
                Avatar(
                    url = latestNotificationUserAvatar,
                    size = 28.dp,
                    onClick = {}
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            
            Text(
                text = "$unreadCount ${stringResource(Res.string.feedline_new_messages_suffix)}",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.width(4.dp))
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = stringResource(Res.string.feedline_notification_title),
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FeedNotificationBarPreview() {
    FeedNotificationBar(
        unreadCount = 3,
        latestNotificationUserAvatar = "",
        onClick = {}
    )
}


