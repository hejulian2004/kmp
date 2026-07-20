/**
 * @File: FeedTopBar.kt
 * @Package: org.example.project.ui.components.feed
 * @Description: 朋友圈主界面的顶部标题导航栏组件
 * @Date: 2026-07-20
 */
package org.example.project.ui.components.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FeedTopBar (
    modifier: Modifier = Modifier,
    onShortClickCreatePost: () -> Unit,
    onLongClickCreatePost: () -> Unit
){
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(color = Color(0xFFF5F5F5))
            .statusBarsPadding()//避开顶部系统信息栏
            .height(56.dp)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "朋友圈",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                tint = Color.Black,
                contentDescription = "发布",
                modifier = Modifier
                    .combinedClickable(
                        onClick = {
                            onShortClickCreatePost()
                        },
                        onLongClick = {
                            onLongClickCreatePost()
                        }
                    ).size(25.dp)
            )
        }
    }
}

@Preview
@Composable
fun FeedTopBarPreview(){
    FeedTopBar(
        onShortClickCreatePost = {},
        onLongClickCreatePost = {}
    )
}


