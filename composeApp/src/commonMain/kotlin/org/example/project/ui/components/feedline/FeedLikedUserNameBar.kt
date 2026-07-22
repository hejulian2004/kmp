/**
 * @File: FeedLikedUserNameBar.kt
 * @Package: org.example.project.ui.components.feedline
 * @Description: 帖子点赞用户姓名列表展现组件
 * @Author: 何聚敛
 * @Date: 2026-07-20
 */
package org.example.project.ui.components.feedline

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.domain.model.feedline.FeedLineUser

@Composable
fun FeedLikedUserAvatarBar(
    modifier: Modifier = Modifier,
    likedUserList: List<FeedLineUser>,
    onUserClick: (FeedLineUser) -> Unit
) {
    if(likedUserList.isEmpty())return

    Row(
        modifier = modifier
            .background(
                color = Color(0xFFF3F3F3),
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 6.dp, vertical = 6.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Default.FavoriteBorder,
            contentDescription = "点赞",
            tint = Color(0xFF576B95),
            modifier = Modifier.size(15.dp)
        )

        FlowRow(
            modifier = Modifier.padding(start = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            likedUserList.forEachIndexed { index, user ->
                Text(
                    text = user.name,
                    fontSize = 14.sp,
                    color = Color(0xFF576B95),
                    modifier = Modifier.clickable {
                        onUserClick(user)
                    }
                )

                if (index != likedUserList.lastIndex) {
                    Text(
                        text = ",",
                        fontSize = 14.sp,
                        color = Color(0xFF576B95)
                    )
                }
            }
        }
    }
}




