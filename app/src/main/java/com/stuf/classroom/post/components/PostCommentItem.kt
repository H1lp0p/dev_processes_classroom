package com.stuf.classroom.post.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.stuf.classroom.post.CommentUi
import com.stuf.domain.model.CommentId

@Composable
internal fun PostCommentItem(
    comment: CommentUi,
    depth: Int = 0,
    onLoadRepliesClick: (CommentId) -> Unit,
    onReplyClick: (CommentId) -> Unit,
    loadingRepliesForCommentId: String?,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(start = 16.dp + 12.dp * depth, top = 8.dp, bottom = 8.dp)
                .testTag("post_comment_item"),
    ) {
        Box(
            modifier =
                Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = comment.authorName.take(1).uppercase(),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp),
        ) {
            Text(
                text = comment.authorName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = comment.text,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp),
            )
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = comment.createdAtLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TextButton(
                    onClick = { onReplyClick(CommentId(comment.id)) },
                    modifier = Modifier.testTag("post_comment_reply_button"),
                ) {
                    Text("Ответить")
                }
            }

            when {
                loadingRepliesForCommentId == comment.id -> {
                    CircularProgressIndicator(
                        modifier =
                            Modifier
                                .padding(top = 8.dp)
                                .size(20.dp),
                        strokeWidth = 2.dp,
                    )
                }
                comment.replies.isNotEmpty() -> {
                    Column(modifier = Modifier.padding(top = 4.dp)) {
                        comment.replies.forEach { reply ->
                            PostCommentItem(
                                comment = reply,
                                depth = depth + 1,
                                onLoadRepliesClick = onLoadRepliesClick,
                                onReplyClick = onReplyClick,
                                loadingRepliesForCommentId = loadingRepliesForCommentId,
                            )
                        }
                    }
                }
                comment.repliesLoaded && comment.replies.isEmpty() -> {
                    Text(
                        text = "Нет ответов",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
                else -> {
                    TextButton(onClick = { onLoadRepliesClick(CommentId(comment.id)) }) {
                        Text("Показать ответы")
                    }
                }
            }
        }
    }
}
