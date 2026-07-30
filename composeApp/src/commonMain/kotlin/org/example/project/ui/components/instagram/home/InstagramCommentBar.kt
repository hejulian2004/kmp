/**
 * @File: InstagramCommentBar.kt
 * @Package: org.example.project.ui.components.instagram.home
 * @Description: Instagram风格底部快速评论输入栏组件
 * @Author: 何聚敛
 * @Date: 2026-07-29
 */
package org.example.project.ui.components.instagram.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import org.example.project.ui.theme.instagram.InstagramBlue
import org.example.project.ui.theme.instagram.InstagramTheme
import kotlinproject.composeapp.generated.resources.Res
import kotlinproject.composeapp.generated.resources.ins_add_comment_placeholder
import kotlinproject.composeapp.generated.resources.ins_post_comment
import org.jetbrains.compose.resources.stringResource

/**
 * Instagram风格底部弹出快速评论输入栏组件
 *
 * 包含：
 * - 当前用户头像
 * - 圆角TextField输入框
 * - 右侧"Post"发布按钮
 *
 * @param avatarUrl 当前用户头像地址
 * @param value 当前输入的评论文本
 * @param onValueChange 文本变动回调
 * @param onSendClick 点击发布回调
 * @param modifier 外部修饰符
 */
@Composable
fun InstagramCommentBar(
    avatarUrl: String,
    value: String,
    onValueChange: (String) -> Unit,
    onSendClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = avatarUrl,
                contentDescription = "My Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.width(10.dp))

            TextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = {
                    Text(
                        text = stringResource(Res.string.ins_add_comment_placeholder),
                        fontSize = 13.5.sp,
                        color = Color.Gray
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 40.dp, max = 100.dp)
                    .clip(RoundedCornerShape(20.dp)),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSendClick() }),
                singleLine = false,
                maxLines = 3
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = stringResource(Res.string.ins_post_comment),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (value.trim().isNotEmpty()) InstagramBlue else InstagramBlue.copy(alpha = 0.4f),
                modifier = Modifier
                    .clickable(enabled = value.trim().isNotEmpty()) { onSendClick() }
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            )
        }
    }
}

/**
 * Instagram风格底部快速评论输入栏Composable预览函数
 */
@Preview
@Composable
fun InstagramCommentBarPreview() {
    InstagramTheme {
        InstagramCommentBar(
            avatarUrl = "https://picsum.photos/seed/me/200/200",
            value = "Awesome post! 🔥",
            onValueChange = {},
            onSendClick = {}
        )
    }
}
