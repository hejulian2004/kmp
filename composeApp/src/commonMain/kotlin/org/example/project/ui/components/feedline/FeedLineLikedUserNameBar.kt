/**
 * @File: FeedLineLikedUserNameBar.kt
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.domain.model.feedline.FeedLineUser
import org.example.project.ui.theme.feedline.FeedLineCommentBackgroundGray
import org.example.project.ui.theme.feedline.FeedLineLinkBlue

@Composable
fun FeedLikedUserNameBar(
    modifier: Modifier = Modifier,
    likedUserList: List<FeedLineUser>,
    onUserClick: (FeedLineUser) -> Unit
) {
    if(likedUserList.isEmpty())return

    Row(
        modifier = modifier
            .background(
                color = FeedLineCommentBackgroundGray,
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 6.dp, vertical = 6.dp)
            .fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Default.FavoriteBorder,
            contentDescription = "点赞",
            tint = FeedLineLinkBlue,
            modifier = Modifier
                .size(15.dp)
                .alignBy { measured -> measured.measuredHeight * 4 / 5 }
        )

        FlowRow(
            modifier = Modifier
                .padding(start = 4.dp)
                .alignBy(FirstBaseline),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            likedUserList.forEachIndexed { index, user ->
                Text(
                    text = user.name,
                    fontSize = 14.sp,
                    color = FeedLineLinkBlue,
                    modifier = Modifier.clickable {
                        onUserClick(user)
                    }
                )

                if (index != likedUserList.lastIndex) {
                    Text(
                        text = ",",
                        fontSize = 14.sp,
                        color = FeedLineLinkBlue
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun FeedLikedUserNameBarPreview() {
    FeedLikedUserNameBar(
        likedUserList = listOf(
            FeedLineUser(
                id = "1", name = "张三",
                avatarUrl = "https://i.pravatar.cc/1"
            ),
            FeedLineUser(
                id = "2", name = "李四",
                avatarUrl = "https://i.pravatar.cc/2"
            ),
            FeedLineUser(
                id = "3", name = "王五",
                avatarUrl = "https://i.pravatar.cc/3"
            ),
            FeedLineUser(
                id = "4", name = "赵六",
                avatarUrl = "https://i.pravatar.cc/4"
            ),
            FeedLineUser(
                id = "5", name = "钱七",
                avatarUrl = "https://i.pravatar.cc/5"
            )
        ),
        onUserClick = {}
    )
}


