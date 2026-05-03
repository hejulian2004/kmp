package org.example.project.ui.components.profilescreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlinproject.composeapp.generated.resources.Res
import kotlinproject.composeapp.generated.resources.add_interest
import kotlinproject.composeapp.generated.resources.add_signature
import kotlinproject.composeapp.generated.resources.stat_followers
import kotlinproject.composeapp.generated.resources.stat_following
import kotlinproject.composeapp.generated.resources.stat_posts
import kotlinproject.composeapp.generated.resources.user_avatar
import org.example.project.ui.theme.size
import org.example.project.ui.theme.spacing
import org.jetbrains.compose.resources.stringResource

@Composable
fun ProfileInfoSection(
    avatarUrl: String = "",
    username: String = "",
    postCount: String = "0",
    followerCount: String = "0",
    followingCount: String = "0",
    signature: String = "",
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
                contentDescription = stringResource(Res.string.user_avatar),
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
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = username,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.xs))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.lg)
                ) {
                    StatItem(
                        modifier = Modifier.weight(1f),
                        count = postCount,
                        label = stringResource(Res.string.stat_posts),
                        onClick = onPostClick
                    )
                    StatItem(
                        modifier = Modifier.weight(1f),
                        count = followerCount,
                        label = stringResource(Res.string.stat_followers),
                        onClick = onFollowerClick
                    )
                    StatItem(
                        modifier = Modifier.weight(1f),
                        count = followingCount,
                        label = stringResource(Res.string.stat_following),
                        onClick = onFollowingClick
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.sm))
        if(signature.isBlank()){
            Text(
                text = stringResource(Res.string.add_signature),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.clickable { onSignatureClick() }
            )
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.sm))
            Text(
                text = stringResource(Res.string.add_interest),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.clickable { onSignatureClick() },
                color = MaterialTheme.colorScheme.primary
            )
        } else{
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = signature,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.width(MaterialTheme.spacing.xxs))
                Icon(
                    Icons.Filled.Circle,
                    contentDescription = null,
                    modifier = Modifier.size(5.dp)
                )
                Spacer(modifier = Modifier.width(MaterialTheme.spacing.xxs))
                Text(
                    text = stringResource(Res.string.add_interest),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.clickable { onSignatureClick() },
                    color = MaterialTheme.colorScheme.primary
                )
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


@Preview
@Composable
fun ProfileInfoSectionPreview(){
    ProfileInfoSection()
}