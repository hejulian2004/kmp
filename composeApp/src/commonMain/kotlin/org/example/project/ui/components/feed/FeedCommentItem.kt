/**
 * @File: FeedCommentItem.kt
 * @Package: org.example.project.ui.components.feed
 * @Description: 单条评论内容的列表项展示组件
 * @Date: 2026-07-20
 */
package org.example.project.ui.components.feed

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.example.project.domain.model.FeedComment
import org.example.project.domain.model.FeedUser

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FeedCommentItem(
    modifier: Modifier = Modifier,
    currentUser: FeedUser,
    comment: FeedComment,
    onCommentClick: (FeedComment) -> Unit,
    onCommentLongClick: (FeedComment) -> Unit,
    onCommentUserNameClick: (FeedUser) -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Text(
            text = "${comment.commentUser.name}:",
            color = Color(0xFF576B95),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.clickable{
                onCommentUserNameClick(comment.commentUser)
            }
        )

        if(currentUser.id == comment.commentUser.id){
            Text(
                text = comment.content,
                fontSize = 14.sp,
                modifier = Modifier
                    .weight(1f)
                    .combinedClickable(
                        onClick = {
                            onCommentClick(comment)
                        },
                        onLongClick = {
                            onCommentLongClick(comment)
                        }
                    )
            )
        } else{
            SelectionContainer{
                Text(
                    text = comment.content,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .weight(1f)
                        .clickable{
                            onCommentClick(comment)
                        }
                )
            }
        }
    }
}


