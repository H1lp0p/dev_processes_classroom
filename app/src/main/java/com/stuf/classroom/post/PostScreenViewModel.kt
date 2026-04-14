package com.stuf.classroom.post

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stuf.classroom.di.MainDispatcher
import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.Comment
import com.stuf.domain.model.CommentId
import com.stuf.domain.model.CourseRole
import com.stuf.domain.model.FileInfo
import com.stuf.domain.model.Post
import com.stuf.domain.model.PostId
import com.stuf.domain.model.SolutionId
import com.stuf.domain.model.TaskId
import com.stuf.domain.model.Team
import com.stuf.domain.model.TeamId
import com.stuf.domain.model.TeamTaskPost
import com.stuf.domain.model.TeamTaskSolution
import com.stuf.domain.model.UserId
import com.stuf.domain.repository.CurrentUserRepository
import com.stuf.domain.repository.FileRepository
import com.stuf.domain.usecase.AddCommentReply
import com.stuf.domain.usecase.AddPostComment
import com.stuf.domain.usecase.AddSolutionComment
import com.stuf.domain.usecase.CheckTeamCaptain
import com.stuf.domain.usecase.GetCommentReplies
import com.stuf.domain.usecase.GetMyTeamForTeamTask
import com.stuf.domain.usecase.GetPost
import com.stuf.domain.usecase.GetPostComments
import com.stuf.domain.usecase.GetSolutionComments
import com.stuf.domain.usecase.GetTeamTaskSolution
import com.stuf.domain.usecase.GetTeamsForTeamTask
import com.stuf.domain.usecase.JoinTeam
import com.stuf.domain.usecase.LeaveTeam
import com.stuf.domain.usecase.SubmitTeamTaskSolution
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.OffsetDateTime
import kotlinx.coroutines.launch

@HiltViewModel
class PostScreenViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getPost: GetPost,
    private val getPostComments: GetPostComments,
    private val getSolutionComments: GetSolutionComments,
    private val getCommentReplies: GetCommentReplies,
    private val addPostComment: AddPostComment,
    private val addSolutionComment: AddSolutionComment,
    private val addCommentReply: AddCommentReply,
    private val getTeamsForTeamTask: GetTeamsForTeamTask,
    private val getMyTeamForTeamTask: GetMyTeamForTeamTask,
    private val joinTeam: JoinTeam,
    private val getTeamTaskSolution: GetTeamTaskSolution,
    private val checkTeamCaptain: CheckTeamCaptain,
    private val submitTeamTaskSolution: SubmitTeamTaskSolution,
    private val fileRepository: FileRepository,
    private val currentUserRepository: CurrentUserRepository,
    private val leaveTeam: LeaveTeam,
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
                teamTask = null,
                isLoadingTeamSection = false,
                teamSectionError = null,
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

                    if (post is TeamTaskPost) {
                        launch(dispatcher) { loadTeamTaskSection(post) }
                    }

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

    fun onJoinTeam(teamId: TeamId) {
        val ttPost: TeamTaskPost =
            (_uiState.value.content as? PostScreenContent.TeamTask)?.post ?: return

        viewModelScope.launch(dispatcher) {
            when (joinTeam(teamId)) {
                is DomainResult.Success -> loadTeamTaskSection(ttPost)
                is DomainResult.Failure ->
                    _uiState.value =
                        _uiState.value.copy(teamSectionError = "Не удалось присоединиться к команде")
            }
        }
    }

    fun onLeaveTeam(teamId: TeamId) {
        val ttPost: TeamTaskPost =
            (_uiState.value.content as? PostScreenContent.TeamTask)?.post ?: return

        viewModelScope.launch(dispatcher) {
            when (leaveTeam(teamId)) {
                is DomainResult.Success -> loadTeamTaskSection(ttPost)
                is DomainResult.Failure ->
                    _uiState.value =
                        _uiState.value.copy(teamSectionError = "Не удалось покинуть команду")
            }
        }
    }

    /** Загрузка файла в черновик решения (без отправки). */
    fun onPickedTeamSolutionFile(bytes: ByteArray, fileName: String) {
        val ttPost: TeamTaskPost =
            (_uiState.value.content as? PostScreenContent.TeamTask)?.post ?: return
        val dl = ttPost.taskDetails.deadline
        if (dl != null && dl.isBefore(OffsetDateTime.now())) {
            _uiState.value = _uiState.value.copy(teamSectionError = "Срок сдачи прошёл")
            return
        }
        viewModelScope.launch(dispatcher) {
            when (val upload = fileRepository.uploadFile(bytes, fileName)) {
                is DomainResult.Success -> {
                    val file: FileInfo = upload.value
                    val tt: TeamTaskPostState = _uiState.value.teamTask ?: return@launch
                    _uiState.value =
                        _uiState.value.copy(
                            teamTask =
                                tt.copy(pendingSolutionFiles = tt.pendingSolutionFiles + file),
                            teamSectionError = null,
                        )
                }
                is DomainResult.Failure ->
                    _uiState.value =
                        _uiState.value.copy(teamSectionError = "Не удалось загрузить файл")
            }
        }
    }

    fun onRemovePendingTeamSolutionFile(fileId: String) {
        val tt: TeamTaskPostState = _uiState.value.teamTask ?: return
        _uiState.value =
            _uiState.value.copy(
                teamTask =
                    tt.copy(
                        pendingSolutionFiles = tt.pendingSolutionFiles.filter { it.id != fileId },
                    ),
            )
    }

    /** Убрать файл из состава уже сохранённого решения (до «Сохранить»). */
    fun onRemoveSavedTeamSolutionFile(fileId: String) {
        val tt: TeamTaskPostState = _uiState.value.teamTask ?: return
        _uiState.value =
            _uiState.value.copy(
                teamTask =
                    tt.copy(
                        removedSavedSolutionFileIds = tt.removedSavedSolutionFileIds + fileId,
                    ),
            )
    }

    fun onSubmitTeamSolution(text: String) {
        val ttPost: TeamTaskPost =
            (_uiState.value.content as? PostScreenContent.TeamTask)?.post ?: return
        val dl = ttPost.taskDetails.deadline
        if (dl != null && dl.isBefore(OffsetDateTime.now())) {
            _uiState.value = _uiState.value.copy(teamSectionError = "Срок сдачи прошёл")
            return
        }
        val teamTask: TeamTaskPostState = _uiState.value.teamTask ?: return
        val teamId: TeamId = teamTask.myTeam?.id ?: return
        val taskId = TaskId(ttPost.id.value)
        val keptFromSaved: List<String> =
            teamTask.solution?.files
                ?.map { it.id }
                ?.filter { it !in teamTask.removedSavedSolutionFileIds }
                .orEmpty()
        val pendingIds: List<String> = teamTask.pendingSolutionFiles.map { it.id }
        val fileIds: List<String> = keptFromSaved + pendingIds

        viewModelScope.launch(dispatcher) {
            when (
                submitTeamTaskSolution(
                    taskId = taskId,
                    captainTeamId = teamId,
                    text = text.ifBlank { null },
                    fileIds = fileIds,
                )
            ) {
                is DomainResult.Success -> loadTeamTaskSection(ttPost)
                is DomainResult.Failure ->
                    _uiState.value =
                        _uiState.value.copy(teamSectionError = "Не удалось отправить решение")
            }
        }
    }

    private suspend fun loadTeamTaskSection(post: TeamTaskPost) {
        coroutineScope {
            _uiState.value =
                _uiState.value.copy(
                    isLoadingTeamSection = true,
                    teamSectionError = null,
                )
            val pid = post.id
            val taskId = TaskId(pid.value)

            val teamsDeferred = async { getTeamsForTeamTask(pid) }
            val myTeamDeferred = async { getMyTeamForTeamTask(pid) }
            val solDeferred = async { getTeamTaskSolution(taskId) }
            val currentUserDeferred = async { currentUserRepository.getCurrentUser() }

            val teamsResult = teamsDeferred.await()
            val myTeamResult = myTeamDeferred.await()
            val solResult = solDeferred.await()
            val currentUserId: UserId? =
                when (val u = currentUserDeferred.await()) {
                    is DomainResult.Success -> u.value.id
                    is DomainResult.Failure -> null
                }

            val teamsList: List<Team> =
                when (teamsResult) {
                    is DomainResult.Success -> teamsResult.value
                    is DomainResult.Failure -> emptyList()
                }
            val myTeam: Team? =
                when (myTeamResult) {
                    is DomainResult.Success -> myTeamResult.value
                    is DomainResult.Failure -> null
                }
            val solution: TeamTaskSolution? =
                when (solResult) {
                    is DomainResult.Success -> solResult.value
                    is DomainResult.Failure -> null
                }

            val solutionId: SolutionId? = solution?.id
            val solutionCommentsResult: DomainResult<List<Comment>>? =
                if (solutionId != null) {
                    getSolutionComments(solutionId)
                } else {
                    null
                }
            val solutionCommentsUi: List<CommentUi> =
                when (solutionCommentsResult) {
                    null -> emptyList()
                    is DomainResult.Success ->
                        solutionCommentsResult.value.map { it.toUi().copy(isPrivate = true) }
                    is DomainResult.Failure -> emptyList()
                }
            val solutionCommentsErr: String? =
                when (solutionCommentsResult) {
                    is DomainResult.Failure -> "Не удалось загрузить приватные комментарии"
                    else -> null
                }

            val isCaptain: Boolean =
                if (myTeam != null) {
                    when (val cap = checkTeamCaptain(myTeam.id)) {
                        is DomainResult.Success -> cap.value
                        is DomainResult.Failure -> false
                    }
                } else {
                    false
                }

            val sectionError: String? =
                when {
                    teamsResult is DomainResult.Failure -> "Не удалось загрузить команды"
                    myTeamResult is DomainResult.Failure -> "Не удалось загрузить вашу команду"
                    else -> null
                }

            _uiState.value =
                _uiState.value.copy(
                    isLoadingTeamSection = false,
                    teamSectionError = sectionError,
                    teamTask =
                        TeamTaskPostState(
                            teams = teamsList,
                            myTeam = myTeam,
                            solution = solution,
                            solutionComments = solutionCommentsUi,
                            isLoadingSolutionComments = false,
                            solutionCommentsError = solutionCommentsErr,
                            isCaptain = isCaptain,
                            currentUserId = currentUserId,
                            pendingSolutionFiles = emptyList(),
                            removedSavedSolutionFileIds = emptySet(),
                        ),
                )
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
            val current: PostUiState = _uiState.value
            val teamTaskContent: PostScreenContent.TeamTask? =
                current.content as? PostScreenContent.TeamTask
            val teamTaskState: TeamTaskPostState? = current.teamTask

            if (parentCommentId != null) {
                when (val result = addCommentReply(parentCommentId, text)) {
                    is DomainResult.Success -> {
                        val created: Comment = result.value
                        val createdUi: CommentUi = created.toUi()
                        val updatedComments: List<CommentUi> =
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
                        _uiState.value = current.copy(comments = updatedComments)
                    }
                    is DomainResult.Failure -> Unit
                }
                return@launch
            }

            if (isPrivate && teamTaskContent != null) {
                val tt: TeamTaskPostState = teamTaskState ?: return@launch
                val sid: SolutionId? = tt.solution?.id
                if (sid == null) {
                    _uiState.value =
                        current.copy(
                            teamTask =
                                tt.copy(
                                    solutionCommentsError = "Сначала отправьте решение команды",
                                ),
                        )
                    return@launch
                }
                when (val result = addSolutionComment(sid, text)) {
                    is DomainResult.Success -> {
                        val createdUi: CommentUi = result.value.toUi().copy(isPrivate = true)
                        _uiState.value =
                            current.copy(
                                teamTask =
                                    tt.copy(
                                        solutionComments = tt.solutionComments + createdUi,
                                        solutionCommentsError = null,
                                    ),
                            )
                    }
                    is DomainResult.Failure -> Unit
                }
                return@launch
            }

            when (val result = addPostComment(postId, text)) {
                is DomainResult.Success -> {
                    val created: Comment = result.value
                    val createdUi: CommentUi = created.toUi()
                    _uiState.value = current.copy(comments = current.comments + createdUi)
                }
                is DomainResult.Failure -> Unit
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
