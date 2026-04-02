package com.stuf.classroom.post.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.stuf.classroom.post.CommentUi
import com.stuf.domain.model.CommentId

@Composable
internal fun PostCommentItem(
    comment: CommentUi,
    onLoadRepliesClick: (CommentId) -> Unit,
    onReplyClick: (CommentId) -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .testTag("post_comment_item"),
    ) {
        Text(text = comment.authorName)
        Text(text = comment.text)
        if (comment.replies.isEmpty()) {
            TextButton(onClick = { onLoadRepliesClick(CommentId(comment.id)) }) {
                Text("Ответы")
            }
        } else {
            Column {
                comment.replies.forEach { reply ->
                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp),
                    ) {
                        Text(text = reply.authorName)
                        Text(text = reply.text)
                    }
                }
            }
        }

        TextButton(onClick = { onReplyClick(CommentId(comment.id)) }) {
            Text("Ответить")
        }
    }
}
