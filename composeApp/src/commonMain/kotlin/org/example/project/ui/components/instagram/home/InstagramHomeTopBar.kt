/**
 * @File: InstagramHomeTopBar.kt
 * @Package: org.example.project.ui.components.instagram.home
 * @Description: Instagram首页顶部App导航栏组件（依据最新UI：左侧"+"加号，中间居中"Instagram"Logo，右侧爱心通知图标）
 * @Author: 何聚敛
 * @Date: 2026-07-28
 */
package org.example.project.ui.components.instagram.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.ui.theme.instagram.InstagramRed
import org.example.project.ui.theme.instagram.InstagramTheme

/**
 * Instagram首页顶部导航栏组件（对齐最新版本UI设计）
 *
 * 布局包含：
 * - 左侧：发布"+"加号快捷图标
 * - 中间：水平居中的"Instagram"艺术字体Logo
 * - 右侧：爱心通知图标（含未读数Badge红点提示）
 *
 * @param unreadNotificationCount 未读通知数量，>0时显示红点Badge
 * @param onCreatePostClick 点击左侧发帖"+"图标回调
 * @param onNotificationClick 点击右侧爱心通知图标回调
 * @param onLogoClick 点击中间Logo标题回调
 * @param modifier 外部修饰符
 */
@Composable
fun InstagramHomeTopBar(
    unreadNotificationCount: Int = 0,
    onCreatePostClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onLogoClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .height(54.dp),
        color = MaterialTheme.colorScheme.background,
        shadowElevation = 0.5.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            // Left Action: "+" Add Icon Button
            Box(
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                TopBarIconButton(
                    icon = Icons.Default.Add,
                    contentDescription = "Create Post",
                    onClick = onCreatePostClick
                )
            }

            // Center: Instagram Logo Branding
            Text(
                text = "Instagram",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    fontSize = 26.sp,
                    letterSpacing = (-0.5).sp
                ),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { onLogoClick() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )

            // Right Action: Heart Notification Icon Button (with optional badge)
            Box(
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                BadgedBox(
                    badge = {
                        if (unreadNotificationCount > 0) {
                            Badge(
                                containerColor = InstagramRed,
                                contentColor = Color.White
                            ) {
                                Text(
                                    text = if (unreadNotificationCount > 99) "99+" else unreadNotificationCount.toString(),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                ) {
                    TopBarIconButton(
                        icon = Icons.Outlined.FavoriteBorder,
                        contentDescription = "Notifications",
                        onClick = onNotificationClick
                    )
                }
            }
        }
    }
}

/**
 * 顶部导航栏自缩放图标按钮
 *
 * @param icon 图标ImageVector
 * @param contentDescription 无障碍说明
 * @param onClick 点击事件回调
 */
@Composable
private fun TopBarIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1.0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 600f)
    )

    Box(
        modifier = Modifier
            .size(36.dp)
            .scale(scale)
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.size(28.dp)
        )
    }
}

/**
 * Instagram首页顶部导航栏Composable预览函数
 */
@Preview
@Composable
fun InstagramHomeTopBarPreview() {
    InstagramTheme {
        InstagramHomeTopBar(
            unreadNotificationCount = 5
        )
    }
}
