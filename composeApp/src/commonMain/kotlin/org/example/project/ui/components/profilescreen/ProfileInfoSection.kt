package org.example.project.ui.components.profilescreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.example.project.ui.theme.size
import org.example.project.ui.theme.spacing

@Composable
fun ProfileInfoSection(
    avatarUrl: String = "",
    username: String = "默认昵称",
    postCount: String = "0",
    followerCount: String = "0",
    followingCount: String = "0",
    signature: String = "添加个性签名",
    interests: List<String> = listOf("添加兴趣"),
    onAvatarClick: () -> Unit = {},
    onSignatureClick: () -> Unit = {},
    onPostClick: () -> Unit = {},
    onFollowerClick: () -> Unit = {},
    onFollowingClick: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(MaterialTheme.spacing.md)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = avatarUrl,
                contentDescription = "用户头像",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(MaterialTheme.size.avatarMd)
                    .clip(CircleShape)
                    .clickable { onAvatarClick() }
            )
            Spacer(modifier = Modifier.width(MaterialTheme.spacing.md))
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = username,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.xs))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.lg)
                ) {
                    StatItem(count = postCount, label = "帖子", onClick = onPostClick)
                    StatItem(count = followerCount, label = "粉丝", onClick = onFollowerClick)
                    StatItem(count = followingCount, label = "关注", onClick = onFollowingClick)
                }
            }
        }
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.sm))
        Text(
            text = signature,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.clickable { onSignatureClick() }
        )
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.sm))
        Row(
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)
        ) {
            interests.forEach { interest ->
                ChipItem(label = interest)
            }
        }
    }
}

@Composable
fun StatItem(
    count: String,
    label: String,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clickable(
                enabled = onClick != null,
                onClick = { onClick?.invoke() },
                interactionSource = MutableInteractionSource(),
                indication = null
            )
    ) {
        Text(
            text = count,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun ChipItem(label: String) {
    Box(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                shape = CircleShape
            )
            .padding(horizontal = MaterialTheme.spacing.sm, vertical = MaterialTheme.spacing.xxs)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}