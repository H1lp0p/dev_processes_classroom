package com.stuf.classroom.post

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stuf.classroom.di.MainDispatcher
import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.Comment
import com.stuf.domain.model.CommentId
import com.stuf.domain.model.CourseRole
import com.stuf.domain.model.Post
import com.stuf.domain.model.PostId
import com.stuf.domain.usecase.AddCommentReply
import com.stuf.domain.usecase.AddPostComment
import com.stuf.domain.usecase.GetCommentReplies
import com.stuf.domain.usecase.GetPost
import com.stuf.domain.usecase.GetPostComments
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class PostScreenViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getPost: GetPost,
    private val getPostComments: GetPostComments,
    private val getCommentReplies: GetCommentReplies,
    private val addPostComment: AddPostComment,
    private val addCommentReply: AddCommentReply,
    @MainDispatcher private val dispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val currentUserRole: CourseRole? =
        when (savedStateHandle.get<String>("role")?.lowercase()) {
            "teacher" -> CourseRole.TEACHER
            "student" -> CourseRole.STUDENT
            else -> null
        }

    private val _uiState: MutableStateFlow<PostUiState> =
        MutableStateFlow(
            PostUiState(
                currentUserRole = currentUserRole ?: CourseRole.STUDENT,
            ),
        )
    val uiState: StateFlow<PostUiState> = _uiState.asStateFlow()

    fun onRetry() {
        val postIdValue: String = savedStateHandle.get<String>("postId") ?: return
        val postId: PostId = PostId(java.util.UUID.fromString(postIdValue))

        _uiState.value =
            _uiState.value.copy(
                isLoadingPost = true,
                postLoadError = null,
                isLoadingComments = true,
                commentsLoadError = null,
                content = null,
            )

        viewModelScope.launch(dispatcher) {
            val postResult: DomainResult<Post> = getPost(postId)
            when (postResult) {
                is DomainResult.Success -> {
                    val post: Post = postResult.value
                    _uiState.value =
                        _uiState.value.copy(
                            isLoadingPost = false,
                            content = post.toPostScreenContent(),
                        )

                    val commentsResult: DomainResult<List<Comment>> = getPostComments(postId)
                    val comments: List<Comment> =
                        when (commentsResult) {
                            is DomainResult.Success -> commentsResult.value
                            is DomainResult.Failure -> emptyList()
                        }
                    val commentsUi: List<CommentUi> = comments.map { it.toUi() }
                    _uiState.value =
                        _uiState.value.copy(
                            isLoadingComments = false,
                            comments = commentsUi,
                            commentsLoadError =
                                if (commentsResult is DomainResult.Failure) {
                                    "Не удалось загрузить комментарии"
                                } else {
                                    null
                                },
                        )
                }
                is DomainResult.Failure -> {
                    _uiState.value =
                        _uiState.value.copy(
                            isLoadingPost = false,
                            isLoadingComments = false,
                            postLoadError = "Failed to load post",
                            content = null,
                        )
                }
            }
        }
    }

    fun onCommentSubmit(
        text: String,
        isPrivate: Boolean,
        parentCommentId: CommentId?,
    ) {
        val postIdValue: String = savedStateHandle.get<String>("postId") ?: return
        val postId: PostId = PostId(java.util.UUID.fromString(postIdValue))

        viewModelScope.launch(dispatcher) {
            val result: DomainResult<Comment> =
                if (parentCommentId == null) {
                    addPostComment(postId, text)
                } else {
                    addCommentReply(parentCommentId, text)
                }

            when (result) {
                is DomainResult.Success -> {
                    val created: Comment = result.value
                    val createdUi: CommentUi = created.toUi()
                    val current: PostUiState = _uiState.value
                    val updatedComments: List<CommentUi> =
                        if (parentCommentId == null) {
                            current.comments + createdUi
                        } else {
                            current.comments.map { existing ->
                                if (existing.id == parentCommentId.value) {
                                    existing.copy(
                                        replies = existing.replies + createdUi,
                                        repliesLoaded = true,
                                    )
                                } else {
                                    existing
                                }
                            }
                        }
                    _uiState.value = current.copy(comments = updatedComments)
                }
                is DomainResult.Failure -> {
                    // keep error handling minimal for now
                }
            }
        }
    }

    fun onLoadRepliesClick(commentId: CommentId) {
        viewModelScope.launch(dispatcher) {
            _uiState.value = _uiState.value.copy(loadingRepliesForCommentId = commentId.value)
            val result: DomainResult<List<Comment>> = getCommentReplies(commentId)
            if (result is DomainResult.Success) {
                val repliesUi: List<CommentUi> = result.value.map { it.toUi() }
                val current: PostUiState = _uiState.value
                val updated: List<CommentUi> =
                    current.comments.map { commentUi ->
                        if (commentUi.id == commentId.value) {
                            commentUi.copy(replies = repliesUi, repliesLoaded = true)
                        } else {
                            commentUi
                        }
                    }
                _uiState.value =
                    current.copy(
                        comments = updated,
                        loadingRepliesForCommentId = null,
                    )
            } else {
                _uiState.value = _uiState.value.copy(loadingRepliesForCommentId = null)
            }
        }
    }

    private fun Comment.toUi(): CommentUi =
        CommentUi(
            id = id,
            authorName = author.credentials,
            text = text,
            createdAtLabel = formatCommentDate(createdAt),
            isPrivate = isPrivate,
            replies = emptyList(),
            repliesLoaded = false,
        )
}
