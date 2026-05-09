package org.example.project.ui.components.profilescreen

import androidx.compose.runtime.Composable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBackIos
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import kotlinproject.composeapp.generated.resources.Res
import kotlinproject.composeapp.generated.resources.collapse
import org.example.project.ui.theme.AppFontWeight
import org.example.project.ui.theme.size
import org.example.project.ui.theme.spacing
import org.jetbrains.compose.resources.stringResource

data class TopBarSpan(
    val icon: ImageVector? = null,
    val contentDescription: String = "",
    val alignment: Alignment = Alignment.Center,
    val onPress: () -> Unit = {},
    val testTag: String = ""
)

@Composable
fun TopBar(
    title: String = "Title",
    leftSpan1: TopBarSpan = TopBarSpan(),
    leftSpan2: TopBarSpan = TopBarSpan(),
    rightSpan1: TopBarSpan = TopBarSpan(),
    rightSpan2: TopBarSpan = TopBarSpan(),
    titleClickable: Boolean = true,
    onTitlePress: (isExpanded: Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isTitleExpanded by remember { mutableStateOf(false) }
    MaterialTheme.spacing
    MaterialTheme.size
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(MaterialTheme.size.avatarMd),
        color = MaterialTheme.colorScheme.background,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(MaterialTheme.size.avatarMd),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SpanCell(span = leftSpan1, modifier = Modifier.weight(1f))
            SpanCell(span = leftSpan2, modifier = Modifier.weight(1f))
            TitleSlot(
                title = title,
                clickable = titleClickable,
                isExpanded = isTitleExpanded,
                modifier = Modifier.weight(3f),
                onPress = {
                    isTitleExpanded = !isTitleExpanded
                    onTitlePress(isTitleExpanded)
                }
            )
            SpanCell(span = rightSpan1, modifier = Modifier.weight(1f))
            SpanCell(span = rightSpan2, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun SpanCell(span: TopBarSpan, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.height(MaterialTheme.size.avatarMd),
        contentAlignment = span.alignment
    ) {
        span.icon?.let {
            TopBarIconButton(
                icon = it,
                contentDescription = span.contentDescription,
                onPress = span.onPress,
                size = MaterialTheme.size.avatarMd
            )
        }
    }
}

@Composable
private fun TopBarIconButton(
    icon: ImageVector,
    contentDescription: String,
    onPress: () -> Unit,
    size: Dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 600f)
    )
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onPress)
            .scale(scale),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(MaterialTheme.size.iconMd)
        )
    }
}

@Composable
private fun TitleSlot(
    title: String,
    clickable: Boolean,
    isExpanded: Boolean,
    onPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = if (clickable) {
                Modifier.clickable(interactionSource = interactionSource, indication = null, onClick = onPress)
            } else Modifier
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = AppFontWeight.Bold,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (isExpanded) {
                IconButton(
                    onClick = onPress,
                    modifier = Modifier.size(MaterialTheme.size.iconLg)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ExpandMore,
                        contentDescription = stringResource(Res.string.collapse),
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(MaterialTheme.size.iconMd)
                    )
                }
            }
        }
    }
}


@Preview
@Composable
fun TopBarPreview() {
    TopBar(
        leftSpan1 = TopBarSpan(
            icon = Icons.AutoMirrored.Rounded.ArrowBackIos,
            alignment = Alignment.CenterStart,
            onPress = { }
        ),
        leftSpan2 = TopBarSpan(),
        rightSpan1 = TopBarSpan(
            icon = Icons.Rounded.Search,
            alignment = Alignment.Center,
            onPress = {  }
        ),
        rightSpan2 = TopBarSpan(
            icon = Icons.Rounded.MoreVert,
            alignment = Alignment.CenterEnd,
            onPress = {  }
        ),
        onTitlePress = { isExpanded ->
        }
    )
}
