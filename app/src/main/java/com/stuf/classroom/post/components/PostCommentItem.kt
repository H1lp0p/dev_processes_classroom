package com.stuf.classroom.post.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    onEditCommentClick: (CommentId, String, Boolean) -> Unit,
    onDeleteCommentClick: (CommentId, Boolean) -> Unit,
    currentUserId: String?,
    loadingRepliesForCommentId: String?,
    /** Для ветки без тредов (например, комментарии к решению). */
    showThreadActions: Boolean = true,
) {
    val canManageComment: Boolean = comment.isOwn || (currentUserId != null && comment.authorId == currentUserId)
    var isMenuExpanded: Boolean by remember(comment.id) { mutableStateOf(false) }
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = comment.authorName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (canManageComment) {
                    Box {
                        IconButton(
                            onClick = { isMenuExpanded = true },
                            modifier =
                                Modifier
                                    .height(28.dp)
                                    .testTag("post_comment_actions_button"),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MoreHoriz,
                                contentDescription = "Действия с комментарием",
                                modifier = Modifier.size(20.dp).padding(4.dp),
                            )
                        }
                        DropdownMenu(
                            expanded = isMenuExpanded,
                            onDismissRequest = { isMenuExpanded = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text("Редактировать") },
                                onClick = {
                                    isMenuExpanded = false
                                    onEditCommentClick(CommentId(comment.id), comment.text, comment.isPrivate)
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("Удалить") },
                                onClick = {
                                    isMenuExpanded = false
                                    onDeleteCommentClick(CommentId(comment.id), comment.isPrivate)
                                },
                            )
                        }
                    }
                }
            }
            Text(
                text = comment.text,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 0.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = comment.createdAtLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (showThreadActions) {
                    TextButton(
                        onClick = { onReplyClick(CommentId(comment.id)) },
                        modifier = Modifier.testTag("post_comment_reply_button"),
                    ) {
                        Text("Ответить")
                    }
                }
            }

            if (showThreadActions) {
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
                                    onEditCommentClick = onEditCommentClick,
                                    onDeleteCommentClick = onDeleteCommentClick,
                                    currentUserId = currentUserId,
                                    loadingRepliesForCommentId = loadingRepliesForCommentId,
                                    showThreadActions = true,
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
                        TextButton(
                            onClick = { onLoadRepliesClick(CommentId(comment.id)) },
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                        ) {
                            Text("Показать ответы")
                        }
                    }
                }
            }
        }
    }
}
