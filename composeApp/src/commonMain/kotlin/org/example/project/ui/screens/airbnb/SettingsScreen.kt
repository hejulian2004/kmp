/**
 * @File: SettingsScreen.kt
 * @Package: org.example.project.ui.screens.airbnb
 * @Description: Airbnb 设置容器主 Screen 组件（符合 MVI 架构与响应式状态处理）
 * @Author: 何聚敛
 * @Date: 2026-08-04
 */
package org.example.project.ui.screens.airbnb

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.project.presentation.intent.airbnb.SettingsIntent
import org.example.project.presentation.state.airbnb.SettingsUiState
import org.example.project.ui.components.airbnb.ActionItem
import org.example.project.ui.components.airbnb.SectionCard
import org.example.project.ui.components.airbnb.SectionTitle
import org.example.project.ui.components.airbnb.SettingsPageScaffold
import org.example.project.ui.components.airbnb.ToggleItem
import org.example.project.ui.theme.airbnb.Accent
import org.example.project.ui.theme.airbnb.AirbnbTheme
import org.example.project.ui.theme.airbnb.DividerColor
import org.example.project.ui.theme.airbnb.PageBg
import org.example.project.ui.theme.airbnb.TextPrimary
import org.example.project.ui.theme.airbnb.TextSecondary

@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onIntent: (SettingsIntent) -> Unit = {},
    onBack: () -> Unit = {},
) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("退出登录") },
            text = { Text("确定要退出当前账号吗？") },
            confirmButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("确定退出", color = Accent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("取消")
                }
            },
        )
    }

    SettingsPageScaffold(
        title = "设置",
        onBack = onBack,
    ) {
        // 账号
        Column {
            SectionTitle("账号与安全")
            Spacer(Modifier.height(8.dp))
            SectionCard {
                ActionItem(
                    icon = "👤",
                    label = "个人信息",
                )
                HorizontalDivider(color = DividerColor)
                ActionItem(
                    icon = "🔒",
                    label = "账号与安全",
                )
                HorizontalDivider(color = DividerColor)
                ActionItem(
                    icon = "📋",
                    label = "隐私策略",
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // 通用
        Column {
            SectionTitle("通用设置")
            Spacer(Modifier.height(8.dp))
            SectionCard {
                ToggleItem(
                    title = "关怀模式",
                    checked = state.careModeEnabled,
                    onCheckedChange = { onIntent(SettingsIntent.ToggleCareMode) },
                )
                HorizontalDivider(color = DividerColor)
                ActionItem(
                    icon = "🌙",
                    label = "深色外观模式",
                )
                HorizontalDivider(color = DividerColor)
                ActionItem(
                    icon = "🌐",
                    label = "国家与语言 (${state.selectedLanguage})",
                )
                HorizontalDivider(color = DividerColor)
                ActionItem(
                    icon = "🔔",
                    label = "推送与通知消息",
                )
                HorizontalDivider(color = DividerColor)
                ActionItem(
                    icon = "💾",
                    label = "存储与缓存清理",
                    onClick = { onIntent(SettingsIntent.ClearCache) },
                )
                HorizontalDivider(color = DividerColor)
                SettingsVersionItem(
                    label = "当前应用版本",
                    version = "1.0.0",
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // 帮助与关于
        Column {
            SectionTitle("帮助与关于")
            Spacer(Modifier.height(8.dp))
            SectionCard {
                ActionItem(
                    icon = "💬",
                    label = "帮助与反馈",
                )
                HorizontalDivider(color = DividerColor)
                ActionItem(
                    icon = "ℹ️",
                    label = "关于我们",
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // 退出登录
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "退出登录",
                color = Accent,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .clickable { showLogoutDialog = true }
                    .padding(vertical = 16.dp),
            )
        }
    }
}

@Composable
private fun SettingsVersionItem(label: String, version: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            color = TextPrimary,
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(
            text = version,
            color = TextSecondary,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    AirbnbTheme {
        SettingsScreen(state = SettingsUiState())
    }
}
