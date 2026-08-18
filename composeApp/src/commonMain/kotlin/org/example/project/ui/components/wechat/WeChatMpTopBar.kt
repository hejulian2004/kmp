/**
 * @File: WeChatMpTopBar.kt
 * @Package: org.example.project.ui.components.wechat
 * @Description: 微信公众号顶部导航栏组件（包含系统状态栏避让与交互Lambda导出）
 * @Author: 何聚敛
 * @Date: 2026-08-18
 */
package org.example.project.ui.components.wechat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinproject.composeapp.generated.resources.Res
import kotlinproject.composeapp.generated.resources.wechat_mp_title
import org.example.project.ui.theme.wechat.WeChatBackgroundGray
import org.example.project.ui.theme.wechat.WeChatTextPrimary
import org.example.project.ui.theme.wechat.WeChatTheme
import org.jetbrains.compose.resources.stringResource

@Composable
fun WeChatMpTopBar(
    modifier: Modifier = Modifier,
    title: String = stringResource(Res.string.wechat_mp_title),
    onBackClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(WeChatBackgroundGray)
            .statusBarsPadding()
            .height(52.dp)
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                    contentDescription = "Back",
                    tint = WeChatTextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onSearchClick,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = WeChatTextPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                IconButton(
                    onClick = onProfileClick,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonOutline,
                        contentDescription = "Profile",
                        tint = WeChatTextPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        Text(
            text = title,
            color = WeChatTextPrimary,
            fontSize = 17.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Preview(showBackground = true)
@Composable
fun WeChatMpTopBarPreview() {
    WeChatTheme {
        WeChatMpTopBar()
    }
}
