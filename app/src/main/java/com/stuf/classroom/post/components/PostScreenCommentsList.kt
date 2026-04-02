package com.stuf.classroom.post.components

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.stuf.classroom.post.CommentUi
import com.stuf.domain.model.CommentId

@Composable
internal fun PostScreenCommentsList(
    modifier: Modifier = Modifier,
    comments: List<CommentUi>,
    onLoadRepliesClick: (CommentId) -> Unit,
    onReplyClick: (CommentId) -> Unit,
) {
    LazyColumn(
        modifier = modifier.testTag("post_comments_list"),
    ) {
        items(comments) { comment ->
            PostCommentItem(
                comment = comment,
                onLoadRepliesClick = onLoadRepliesClick,
                onReplyClick = onReplyClick,
            )
        }
    }
}
