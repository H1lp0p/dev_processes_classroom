package com.stuf.classroom.post

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.stuf.classroom.post.components.PostCommentInput
import com.stuf.classroom.post.components.PostScreenCommentsList
import com.stuf.classroom.post.components.PostScreenErrorBlock
import com.stuf.classroom.post.components.PostScreenLoadingIndicator
import com.stuf.classroom.post.components.PostScreenPostHeaderCard
import com.stuf.classroom.post.components.PostScreenTeacherSolutionsSection
import com.stuf.classroom.post.components.PostScreenTopBar
import com.stuf.domain.model.CommentId
import com.stuf.domain.model.CourseRole
import com.stuf.domain.model.SolutionId

@Composable
fun PostScreen(
    state: PostUiState,
    onRetry: () -> Unit,
    onAttachSolutionClick: () -> Unit,
    onCommentSubmit: (text: String, isPrivate: Boolean, parentCommentId: CommentId?) -> Unit,
    onLoadRepliesClick: (CommentId) -> Unit,
    onToggleCommentsVisibility: () -> Unit,
    onSolutionClick: (SolutionId) -> Unit,
    onBackClick: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
    ) {
        PostScreenTopBar(onBackClick = onBackClick)

        Spacer(modifier = Modifier.height(8.dp))

        PostScreenPostHeaderCard(
            state = state,
            onAttachSolutionClick = onAttachSolutionClick,
        )

        if (state.isLoading) {
            PostScreenLoadingIndicator()
        }

        state.error?.let { msg ->
            PostScreenErrorBlock(message = msg, onRetry = onRetry)
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (state.isTask && state.currentUserRole == CourseRole.TEACHER) {
            PostScreenTeacherSolutionsSection(
                state = state,
                onToggleCommentsVisibility = onToggleCommentsVisibility,
                onSolutionClick = onSolutionClick,
            )
        }

        val replyTarget: MutableState<CommentId?> = remember { mutableStateOf(null) }

        if (!(state.isTask && state.currentUserRole == CourseRole.TEACHER && state.areCommentsCollapsedForTeacher)) {
            Spacer(modifier = Modifier.height(8.dp))

            PostScreenCommentsList(
                modifier = Modifier.fillMaxWidth().weight(1f),
                comments = state.comments,
                onLoadRepliesClick = onLoadRepliesClick,
                onReplyClick = { commentId: CommentId ->
                    replyTarget.value = commentId
                },
            )
        }

        PostCommentInput(
            replyTo = replyTarget.value,
            onCancelReply = { replyTarget.value = null },
            onCommentSubmit = { text: String, isPrivate: Boolean ->
                val parent: CommentId? = replyTarget.value
                onCommentSubmit(text, isPrivate, parent)
                replyTarget.value = null
            },
        )
    }
}
