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
import com.stuf.domain.model.PostKind
import com.stuf.domain.model.TaskDetails
import com.stuf.domain.model.TaskId
import com.stuf.domain.model.User
import com.stuf.domain.usecase.AddCommentReply
import com.stuf.domain.usecase.AddPostComment
import com.stuf.domain.usecase.GetCommentReplies
import com.stuf.domain.usecase.GetPost
import com.stuf.domain.usecase.GetPostComments
import com.stuf.domain.usecase.GetSolutionsForTask
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
    private val getSolutionsForTask: GetSolutionsForTask,
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

        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch(dispatcher) {
            val postResult: DomainResult<Post> = getPost(postId)
            when (postResult) {
                is DomainResult.Success -> {
                    val post: Post = postResult.value
                    val commentsResult: DomainResult<List<Comment>> = getPostComments(postId)
                    val comments: List<Comment> =
                        when (commentsResult) {
                            is DomainResult.Success -> commentsResult.value
                            is DomainResult.Failure -> emptyList()
                        }

                    val taskDetails: TaskDetails? = post.taskDetails
                    val isTask: Boolean = post.kind == PostKind.TASK && taskDetails != null

                    val solutionsUi: List<SolutionUi> =
                        if (isTask && currentUserRole == CourseRole.TEACHER) {
                            val taskId: TaskId = TaskId(post.id.value)
                            when (val solutionsResult = getSolutionsForTask(taskId)) {
                                is DomainResult.Success -> {
                                    solutionsResult.value.map { solution ->
                                        val student: User? = null
                                        val studentName: String = student?.credentials ?: "Student"
                                        SolutionUi(
                                            id = solution.id,
                                            studentName = studentName,
                                            status = solution.status.name,
                                        )
                                    }
                                }
                                is DomainResult.Failure -> emptyList()
                            }
                        } else {
                            emptyList()
                        }

                    val commentsUi: List<CommentUi> = comments.map { it.toUi() }

                    _uiState.value =
                        _uiState.value.copy(
                            isLoading = false,
                            error = null,
                            postTitle = post.title,
                            postText = post.text,
                            isTask = isTask,
                            comments = commentsUi,
                            solutions = solutionsUi,
                        )
                }
                is DomainResult.Failure -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Failed to load post")
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
                                    existing.copy(replies = existing.replies + createdUi)
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
            val result: DomainResult<List<Comment>> = getCommentReplies(commentId)
            if (result is DomainResult.Success) {
                val repliesUi: List<CommentUi> = result.value.map { it.toUi() }
                val current: PostUiState = _uiState.value
                val updated: List<CommentUi> =
                    current.comments.map { commentUi ->
                        if (commentUi.id == commentId.value) {
                            commentUi.copy(replies = repliesUi)
                        } else {
                            commentUi
                        }
                    }
                _uiState.value = current.copy(comments = updated)
            }
        }
    }

    fun onToggleCommentsVisibility() {
        val current: PostUiState = _uiState.value
        _uiState.value =
            current.copy(areCommentsCollapsedForTeacher = !current.areCommentsCollapsedForTeacher)
    }

    private fun Comment.toUi(): CommentUi =
        CommentUi(
            id = id,
            authorName = author.credentials,
            text = text,
            isPrivate = isPrivate,
        )
}

