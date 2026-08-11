package org.example.project.ui.components.instagram.content

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import org.example.project.domain.model.instagram.ContentThumbnailData
import org.example.project.domain.model.instagram.PostType

@Composable
fun InstagramContentThumbnail(
    modifier: Modifier = Modifier,
    data: ContentThumbnailData,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
) {
    var imageState by remember { mutableStateOf<AsyncImagePainter.State>(AsyncImagePainter.State.Empty) }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(2.dp))
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { onLongClick() },
                )
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFE0E0E0))
        )
        val isLoading = imageState is AsyncImagePainter.State.Loading
                || imageState is AsyncImagePainter.State.Empty
        if (isLoading) {
            InstagramShimmerBox(modifier = Modifier.fillMaxSize())
        }
        AsyncImage(
            model = data.imageUrl.ifBlank { null },
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        val hasOverlayContent = data.type != PostType.SINGLE || data.duration != null
        if (hasOverlayContent) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.4f)
                    .align(Alignment.BottomStart)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.35f))
                        )
                    )
            )
        }
        val badgeIcon = when (data.type) {
            PostType.CAROUSEL -> "⧉"
            PostType.VIDEO    -> "▶"
            PostType.REEL     -> "⬡"
            PostType.SINGLE   -> null
        }
        if (badgeIcon != null) {
            Text(
                text = badgeIcon,
                color = Color.White,
                fontSize = 12.sp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp),
            )
        }

        if (data.duration != null) {
            Text(
                text = data.duration!!,
                color = Color.White,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(4.dp),
            )
        }
    }
}

@Composable
fun ContentThumbnail(
    modifier: Modifier = Modifier,
    data: ContentThumbnailData,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
) = InstagramContentThumbnail(modifier, data, onClick, onLongClick)

@Composable
fun InstagramShimmerBox(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmerTranslate",
    )
    Box(
        modifier = modifier.background(
            Brush.linearGradient(
                colors = listOf(
                    Color(0xFFE8E8E8),
                    Color(0xFFF5F5F5),
                    Color(0xFFE8E8E8),
                ),
                start = Offset(translateAnim - 500f, 0f),
                end = Offset(translateAnim, 0f),
            )
        )
    )
}

@Composable
fun ShimmerBox(modifier: Modifier = Modifier) = InstagramShimmerBox(modifier)

@Preview
@Composable
fun InstagramContentThumbnailPreview() {
    InstagramContentThumbnail(
        data = ContentThumbnailData(
            id = "1",
            type = PostType.CAROUSEL,
            imageUrl = "",
        ),
        onClick = {},
        onLongClick = {}
    )
}
