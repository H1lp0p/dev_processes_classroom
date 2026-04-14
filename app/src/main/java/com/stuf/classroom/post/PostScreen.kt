package com.stuf.classroom.post

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.stuf.classroom.post.components.PostCommentItem
import com.stuf.classroom.post.components.PostScreenAnnouncementSection
import com.stuf.classroom.post.components.PostScreenCommentComposer
import com.stuf.classroom.post.components.PostScreenCommentsDivider
import com.stuf.classroom.post.components.PostScreenErrorBlock
import com.stuf.classroom.post.components.PostScreenLoadingIndicator
import com.stuf.classroom.post.components.PostScreenMaterialSection
import com.stuf.classroom.post.components.PostScreenTaskSection
import com.stuf.classroom.post.components.PostScreenTopBar
import com.stuf.domain.model.CommentId
import com.stuf.domain.model.TeamId
import com.stuf.domain.model.UserId
import java.util.UUID

private sealed class PostComposerState {
    data object Closed : PostComposerState()

    data object NewRootComment : PostComposerState()

    /** Приватный комментарий к решению (`/api/solution/{id}/comment`). */
    data object NewPrivateSolutionComment : PostComposerState()

    data class Reply(
        val toCommentId: CommentId,
    ) : PostComposerState()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostScreen(
    state: PostUiState,
    onRetry: () -> Unit,
    onPickSolutionFile: () -> Unit,
    onSubmitTeamSolution: (String) -> Unit,
    onSubmitIndividualSolution: (String) -> Unit,
    onRemovePendingTeamSolutionFile: (String) -> Unit,
    onRemovePendingIndividualSolutionFile: (String) -> Unit,
    onRemoveSavedTeamSolutionFile: (String) -> Unit,
    onRemoveSavedIndividualSolutionFile: (String) -> Unit,
    onJoinTeam: (TeamId) -> Unit,
    onLeaveTeam: (TeamId) -> Unit,
    onVoteCaptain: (TeamId, UserId) -> Unit = { _, _ -> },
    onTransferCaptain: (TeamId, UserId) -> Unit = { _, _ -> },
    onOpenGradeDistribution: () -> Unit = {},
    onDownloadAttachment: (UUID) -> Unit = {},
    onCommentSubmit: (text: String, isPrivate: Boolean, parentCommentId: CommentId?) -> Unit,
    onLoadRepliesClick: (CommentId) -> Unit,
    onBackClick: () -> Unit,
) {
    var composerState: PostComposerState by remember { mutableStateOf(PostComposerState.Closed) }
    val commentSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val showTeamTaskScreen: Boolean =
        state.content is PostScreenContent.TeamTask &&
            state.postLoadError == null &&
            !state.isLoadingPost

    val showIndividualTaskScreen: Boolean =
        state.content is PostScreenContent.Task &&
            state.postLoadError == null &&
            !state.isLoadingPost

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            showTeamTaskScreen -> {
                val teamContent: PostScreenContent.TeamTask = state.content as PostScreenContent.TeamTask
                TeamTaskPostScreen(
                    modifier = Modifier.fillMaxSize(),
                    state = state,
                    post = teamContent.post,
                    onBackClick = onBackClick,
                    onOpenPublicCommentComposer = {
                        composerState = PostComposerState.NewRootComment
                    },
                    onOpenPrivateCommentComposer = {
                        composerState = PostComposerState.NewPrivateSolutionComment
                    },
                    onReplyClick = { commentId: CommentId ->
                        composerState = PostComposerState.Reply(commentId)
                    },
                    onLoadRepliesClick = onLoadRepliesClick,
                    onJoinTeam = onJoinTeam,
                    onLeaveTeam = onLeaveTeam,
                    onVoteCaptain = onVoteCaptain,
                    onTransferCaptain = onTransferCaptain,
                    onTeamTaskPickSolutionFile = onPickSolutionFile,
                    onSubmitTeamSolution = onSubmitTeamSolution,
                    onRemovePendingTeamSolutionFile = onRemovePendingTeamSolutionFile,
                    onRemoveSavedTeamSolutionFile = onRemoveSavedTeamSolutionFile,
                    onOpenGradeDistribution = onOpenGradeDistribution,
                    onDownloadAttachment = onDownloadAttachment,
                )
            }
            showIndividualTaskScreen -> {
                val taskContent: PostScreenContent.Task = state.content as PostScreenContent.Task
                IndividualTaskPostScreen(
                    modifier = Modifier.fillMaxSize(),
                    state = state,
                    post = taskContent.post,
                    onBackClick = onBackClick,
                    onOpenPublicCommentComposer = {
                        composerState = PostComposerState.NewRootComment
                    },
                    onReplyClick = { commentId: CommentId ->
                        composerState = PostComposerState.Reply(commentId)
                    },
                    onLoadRepliesClick = onLoadRepliesClick,
                    onPickSolutionFile = onPickSolutionFile,
                    onSubmitIndividualSolution = onSubmitIndividualSolution,
                    onRemovePendingIndividualSolutionFile = onRemovePendingIndividualSolutionFile,
                    onRemoveSavedIndividualSolutionFile = onRemoveSavedIndividualSolutionFile,
                    onDownloadAttachment = onDownloadAttachment,
                )
            }
            else -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .statusBarsPadding(),
                    ) {
                        PostScreenTopBar(onBackClick = onBackClick)
                    }
                    LazyColumn(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                                .padding(horizontal = 16.dp)
                                .testTag("post_comments_list"),
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        if (state.isLoadingPost && state.content == null) {
                            item { PostScreenLoadingIndicator() }
                        } else if (state.postLoadError != null) {
                            item {
                                PostScreenErrorBlock(
                                    message = state.postLoadError,
                                    onRetry = onRetry,
                                )
                            }
                        } else {
                            val content = state.content
                            if (content != null) {
                                item {
                                    when (content) {
                                        is PostScreenContent.Announcement ->
                                            PostScreenAnnouncementSection(post = content.post)
                                        is PostScreenContent.Material ->
                                            PostScreenMaterialSection(
                                                post = content.post,
                                                onAttachmentDownload = onDownloadAttachment,
                                            )
                                        is PostScreenContent.Task ->
                                            PostScreenTaskSection(
                                                post = content.post,
                                                onAttachmentDownload = onDownloadAttachment,
                                            )
                                        is PostScreenContent.TeamTask ->
                                            Spacer(modifier = Modifier.height(0.dp))
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        if (state.isLoadingComments && state.content != null) {
                            item {
                                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }

                        state.commentsLoadError?.let { msg ->
                            item {
                                PostScreenErrorBlock(message = msg, onRetry = onRetry)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }

                        if (state.content != null && state.postLoadError == null && !state.isLoadingPost) {
                            item {
                                PostScreenCommentsDivider()
                            }
                            item {
                                TextButton(
                                    onClick = {
                                        composerState = PostComposerState.NewRootComment
                                    },
                                    modifier = Modifier.testTag("post_new_comment_button"),
                                ) {
                                    Text("Написать комментарий")
                                }
                            }
                        }

                        items(
                            items = state.comments,
                            key = { it.id },
                        ) { comment ->
                            PostCommentItem(
                                comment = comment,
                                onLoadRepliesClick = onLoadRepliesClick,
                                onReplyClick = { commentId: CommentId ->
                                    composerState = PostComposerState.Reply(commentId)
                                },
                                loadingRepliesForCommentId = state.loadingRepliesForCommentId,
                            )
                        }
                    }
                }
            }
        }

        if (composerState != PostComposerState.Closed) {
            ModalBottomSheet(
                onDismissRequest = { composerState = PostComposerState.Closed },
                sheetState = commentSheetState,
            ) {
                when (val composer = composerState) {
                    PostComposerState.NewRootComment ->
                        PostScreenCommentComposer(
                            isReply = false,
                            onDismiss = { composerState = PostComposerState.Closed },
                            onCommentSubmit = { text, isPrivate ->
                                onCommentSubmit(text, isPrivate, null)
                                composerState = PostComposerState.Closed
                            },
                        )
                    PostComposerState.NewPrivateSolutionComment ->
                        PostScreenCommentComposer(
                            isReply = false,
                            titleText = "Приватный комментарий к решению",
                            submitAsPrivate = true,
                            onDismiss = { composerState = PostComposerState.Closed },
                            onCommentSubmit = { text, isPrivate ->
                                onCommentSubmit(text, isPrivate, null)
                                composerState = PostComposerState.Closed
                            },
                        )
                    is PostComposerState.Reply ->
                        PostScreenCommentComposer(
                            isReply = true,
                            onDismiss = { composerState = PostComposerState.Closed },
                            onCommentSubmit = { text, isPrivate ->
                                onCommentSubmit(text, isPrivate, composer.toCommentId)
                                composerState = PostComposerState.Closed
                            },
                        )
                    PostComposerState.Closed -> {}
                }
            }
        }
    }
}
