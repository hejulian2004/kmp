/**
 * @File: InstagramPostHeader.kt
 * @Package: org.example.project.ui.components.instagram.home
 * @Description: Instagram Post动态帖子头部作者信息与更多选项下拉菜单组件
 * @Author: 何聚敛
 * @Date: 2026-07-29
 */
package org.example.project.ui.components.instagram.home

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import org.example.project.data.repository.instagram.createFakeInstagramPosts
import org.example.project.domain.model.instagram.InstagramPost
import org.example.project.domain.model.instagram.ProfileUser
import org.example.project.ui.theme.instagram.InstagramRed
import org.example.project.ui.theme.instagram.InstagramTheme
import kotlinproject.composeapp.generated.resources.Res
import kotlinproject.composeapp.generated.resources.ins_delete
import org.jetbrains.compose.resources.stringResource

/**
 * 帖子顶部用户信息及更多下拉菜单栏组件
 *
 * @param post 帖子实体
 * @param currentUser 当前登录用户
 * @param onAvatarClick 点击头像回调
 * @param onNameClick 点击用户名回调
 * @param onLocationClick 点击地理位置回调
 * @param onAudioClick 点击音乐名称回调
 * @param onDeletePostClick 删除帖子回调
 * @param modifier 外部修饰符
 */
@Composable
fun InstagramPostHeader(
    post: InstagramPost,
    currentUser: ProfileUser,
    onAvatarClick: () -> Unit = {},
    onNameClick: () -> Unit = {},
    onLocationClick: () -> Unit = {},
    onAudioClick: () -> Unit = {},
    onDeletePostClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showMenuDropdown by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 作者头像
        AsyncImage(
            model = post.postUser.avatarUrl,
            contentDescription = post.postUser.username,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .border(0.5.dp, Color.LightGray, CircleShape)
                .clickable { onAvatarClick() }
        )

        Spacer(modifier = Modifier.width(10.dp))

        // 用户名、地理位置或音乐音轨
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = post.postUser.username,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.clickable { onNameClick() }
            )

            if (!post.location.isNullOrBlank() || !post.audioTitle.isNullOrBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!post.audioTitle.isNullOrBlank()) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = "Audio",
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                    }
                    Text(
                        text = post.location ?: post.audioTitle.orEmpty(),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.clickable {
                            if (!post.location.isNullOrBlank()) onLocationClick()
                            else onAudioClick()
                        }
                    )
                }
            }
        }

        // 更多选项下拉菜单图标
        Box {
            IconButton(onClick = { showMenuDropdown = true }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More Options",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            DropdownMenu(
                expanded = showMenuDropdown,
                onDismissRequest = { showMenuDropdown = false }
            ) {
                if (post.postUser.userId == currentUser.userId) {
                    DropdownMenuItem(
                        text = { Text(stringResource(Res.string.ins_delete), color = InstagramRed) },
                        onClick = {
                            showMenuDropdown = false
                            onDeletePostClick()
                        }
                    )
                } else {
                    DropdownMenuItem(
                        text = { Text("About This Account") },
                        onClick = { showMenuDropdown = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Report Post", color = InstagramRed) },
                        onClick = { showMenuDropdown = false }
                    )
                }
            }
        }
    }
}

/**
 * Instagram Post头部用户信息组件Composable预览函数
 */
@Preview
@Composable
fun InstagramPostHeaderPreview() {
    InstagramTheme {
        val fakePosts = createFakeInstagramPosts()
        if (fakePosts.isNotEmpty()) {
            InstagramPostHeader(
                post = fakePosts.first(),
                currentUser = ProfileUser("u_me", "hejulian", "", "", "", "", "")
            )
        }
    }
}
