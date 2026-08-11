/**
 * @File: InstagramPostHeader.kt
 * @Package: org.example.project.ui.components.instagram.home
 * @Description: InstagramPost动态帖子头部作者信息、关注按钮与更多选项下拉菜单组件
 * @Author: 何聚敛
 * @Date: 2026-07-29
 */
package org.example.project.ui.components.instagram.home

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import org.example.project.ui.theme.instagram.InstagramLightGray
import org.example.project.ui.theme.instagram.InstagramRed
import org.example.project.ui.theme.instagram.InstagramTheme
import kotlinproject.composeapp.generated.resources.Res
import kotlinproject.composeapp.generated.resources.ins_about_this_account
import kotlinproject.composeapp.generated.resources.ins_delete
import kotlinproject.composeapp.generated.resources.ins_follow
import kotlinproject.composeapp.generated.resources.ins_following
import kotlinproject.composeapp.generated.resources.ins_report_post
import kotlinproject.composeapp.generated.resources.ins_suggested_for_you
import org.jetbrains.compose.resources.stringResource

/**
 * 帖子顶部用户信息、关注按键及更多下拉菜单栏组件
 *
 * @param post帖子实体
 * @param currentUser当前登录用户
 * @param onAvatarClick点击头像回调
 * @param onNameClick点击用户名回调
 * @param onLocationClick点击地理位置回调
 * @param onAudioClick点击音乐名称回调
 * @param onFollowClick点击关注按钮回调(传递最新关注状态)
 * @param onDeletePostClick删除帖子回调
 * @param modifier外部修饰符
 */
@Composable
fun InstagramPostHeader(
    post: InstagramPost,
    currentUser: ProfileUser,
    onAvatarClick: () -> Unit = {},
    onNameClick: () -> Unit = {},
    onLocationClick: () -> Unit = {},
    onAudioClick: () -> Unit = {},
    onFollowClick: (Boolean) -> Unit = {},
    onDeletePostClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showMenuDropdown by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
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
            } else {
                Text(
                    text = stringResource(Res.string.ins_suggested_for_you),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f)
                )
            }
        }

        // 关注按钮（非本人帖子展示关注/已关注按键）
        if (post.postUser.userId != currentUser.userId) {
            var isFollowing by remember(post.postUser.userId, post.postUser.isFollowing) {
                mutableStateOf(post.postUser.isFollowing)
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isFollowing) InstagramLightGray.copy(alpha = 0.6f)
                        else InstagramLightGray
                    )
                    .clickable {
                        isFollowing = !isFollowing
                        onFollowClick(isFollowing)
                    }
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (isFollowing) stringResource(Res.string.ins_following) else stringResource(Res.string.ins_follow),
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.width(4.dp))
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
                        text = { Text(stringResource(Res.string.ins_about_this_account)) },
                        onClick = { showMenuDropdown = false }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(Res.string.ins_report_post), color = InstagramRed) },
                        onClick = { showMenuDropdown = false }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
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
