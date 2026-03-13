package com.stuf.classroom.post

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBackClick) {
                Text("<")
            }
            TextButton(onClick = onBackClick) {
                Text("Назад")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .testTag("post_header_card"),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = state.postTitle,
                    modifier = Modifier.testTag("post_title"),
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = state.postText,
                    modifier = Modifier.testTag("post_text"),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (state.isTask) "Задание" else "Объявление",
                    modifier = Modifier.testTag("post_type_label"),
                )

                if (state.isTask && state.currentUserRole == CourseRole.STUDENT) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onAttachSolutionClick,
                        modifier = Modifier.testTag("post_attach_solution_button"),
                    ) {
                        Text("Прикрепить решение")
                    }
                }
            }
        }

        if (state.isLoading) {
            Spacer(modifier = Modifier.height(8.dp))
            CircularProgressIndicator(modifier = Modifier.testTag("post_loading_indicator"))
        }

        state.error?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Column {
                Text(text = it, modifier = Modifier.testTag("post_error"))
                Button(
                    onClick = onRetry,
                    modifier = Modifier.testTag("post_retry_button"),
                ) {
                    Text("Повторить")
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (state.isTask && state.currentUserRole == CourseRole.TEACHER) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(
                    onClick = onToggleCommentsVisibility,
                    modifier = Modifier.testTag("post_teacher_toggle_comments_button"),
                ) {
                    Text("Комментарии")
                }
            }

            LazyColumn(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .testTag("post_solutions_list"),
            ) {
                items(state.solutions) { solution ->
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .testTag("post_solution_item")
                                .clickable { onSolutionClick(solution.id) },
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(text = solution.studentName)
                        Text(text = solution.status)
                    }
                }
            }
        }

        val replyTarget: MutableState<CommentId?> = remember { mutableStateOf(null) }

        if (!(state.isTask && state.currentUserRole == CourseRole.TEACHER && state.areCommentsCollapsedForTeacher)) {
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("post_comments_list"),
            ) {
                items(state.comments) { comment ->
                    CommentItem(
                        comment = comment,
                        onLoadRepliesClick = onLoadRepliesClick,
                        onReplyClick = { commentId: CommentId ->
                            replyTarget.value = commentId
                        },
                    )
                }
            }
        }

        CommentInput(
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

@Composable
private fun CommentItem(
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

@Composable
private fun CommentInput(
    replyTo: CommentId?,
    onCancelReply: () -> Unit,
    onCommentSubmit: (text: String, isPrivate: Boolean) -> Unit,
) {
    val textState: MutableState<String> = remember { mutableStateOf("") }

    Column {
        if (replyTo != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "Ответ на комментарий")
                TextButton(onClick = onCancelReply) {
                    Text("Отменить")
                }
            }
        }

        OutlinedTextField(
            value = textState.value,
            onValueChange = { textState.value = it },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .testTag("post_comment_input"),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            Button(
                onClick = {
                    val text: String = textState.value
                    if (text.isNotBlank()) {
                        onCommentSubmit(text, false)
                        textState.value = ""
                    }
                },
                modifier = Modifier.testTag("post_comment_send_button"),
            ) {
                Text("Отправить")
            }
        }
    }
}

