/**
 * @File: FeedCommentList.kt
 * @Package: org.example.project.ui.components.feedline
 * @Description: 帖子评论列表整合展示组件
 * @Author: 何聚敛
 * @Date: 2026-07-20
 */
package org.example.project.ui.components.feedline

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.example.project.domain.model.feedline.FeedLineComment
import org.example.project.domain.model.feedline.FeedLineUser
import org.example.project.data.repository.feedline.generateUUID

@Composable
fun FeedCommentList(
    modifier: Modifier = Modifier,
    currentUser: FeedLineUser,
    commentsList: List<FeedLineComment>,
    onCommentClick: (FeedLineComment) -> Unit,
    onDeleteCommentClick: (FeedLineComment) -> Unit,
    onCommentUserClick: (FeedLineUser) -> Unit
) {
    if(commentsList.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFFF3F3F3),
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 8.dp, vertical = 6.dp),
    ) {
        commentsList.forEachIndexed { index, comment ->
            FeedCommentItem(
                currentUser = currentUser,
                comment = comment,
                onCommentClick = onCommentClick,
                onCommentLongClick = onDeleteCommentClick,
                onCommentUserNameClick = onCommentUserClick
            )

            if (index != commentsList.lastIndex) {
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FeedCommentListPreview() {
    FeedCommentList(
        currentUser = FeedLineUser(
            id = generateUUID(),
            name = "test",
            avatarUrl = ""
        ),
        commentsList = listOf(
            FeedLineComment(
                id = generateUUID(),
                postId = "1",
                commentUser = FeedLineUser(
                    id = "1",
                    name = "张三",
                    avatarUrl = ""
                ),
                content = "这个朋友圈写得不错"
            ),
            FeedLineComment(
                id = generateUUID(),
                postId = "1",
                commentUser = FeedLineUser(
                    id = "2",
                    name = "李四",
                    avatarUrl = ""
                ),
                content = "哈哈哈哈哈哈哈哈，这是一条比较长的评论，用来测试自动换行效果。"
            )
        ),
        onCommentClick = {},
        onDeleteCommentClick = {},
        onCommentUserClick = {}
    )
}


