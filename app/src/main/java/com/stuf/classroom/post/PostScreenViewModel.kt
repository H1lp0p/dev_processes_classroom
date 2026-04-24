package com.stuf.classroom.post

import android.util.Log
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
import com.stuf.domain.model.TaskPost
import com.stuf.domain.model.PostId
import com.stuf.domain.model.SolutionId
import com.stuf.domain.model.TaskId
import com.stuf.domain.model.Team
import com.stuf.domain.model.TeamId
import com.stuf.domain.model.TeamTaskPost
import com.stuf.domain.model.TeamTaskSolution
import com.stuf.domain.model.UserId
import com.stuf.data.di.ApiBaseUrl
import com.stuf.domain.repository.CurrentUserRepository
import com.stuf.domain.repository.FileRepository
import com.stuf.domain.usecase.AddCommentReply
import com.stuf.domain.usecase.AddPostComment
import com.stuf.domain.usecase.AddSolutionComment
import com.stuf.domain.usecase.CheckTeamCaptain
import com.stuf.domain.usecase.CancelSolution
import com.stuf.domain.usecase.CancelTeamTaskSolution
import com.stuf.domain.usecase.DeleteComment
import com.stuf.domain.usecase.EditComment
import com.stuf.domain.usecase.GetCommentReplies
import com.stuf.domain.usecase.GetMyTeamForTeamTask
import com.stuf.domain.usecase.GetPost
import com.stuf.domain.usecase.GetPostComments
import com.stuf.domain.usecase.GetUserSolution
import com.stuf.domain.usecase.GetSolutionComments
import com.stuf.domain.usecase.GetTeamTaskSolution
import com.stuf.domain.usecase.GetTeamsForTeamTask
import com.stuf.domain.usecase.JoinTeam
import com.stuf.domain.usecase.LeaveTeam
import com.stuf.domain.usecase.SubmitSolution
import com.stuf.domain.usecase.SubmitTeamTaskSolution
import com.stuf.domain.usecase.TransferTeamCaptain
import com.stuf.domain.usecase.VoteTeamCaptain
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.OffsetDateTime
import java.util.UUID
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
    private val editComment: EditComment,
    private val deleteComment: DeleteComment,
    private val getTeamsForTeamTask: GetTeamsForTeamTask,
    private val getMyTeamForTeamTask: GetMyTeamForTeamTask,
    private val joinTeam: JoinTeam,
    private val getTeamTaskSolution: GetTeamTaskSolution,
    private val checkTeamCaptain: CheckTeamCaptain,
    private val submitTeamTaskSolution: SubmitTeamTaskSolution,
    private val transferTeamCaptain: TransferTeamCaptain,
    private val voteTeamCaptain: VoteTeamCaptain,
    private val getUserSolution: GetUserSolution,
    private val submitSolution: SubmitSolution,
    private val cancelSolution: CancelSolution,
    private val cancelTeamTaskSolution: CancelTeamTaskSolution,
    private val fileRepository: FileRepository,
    @ApiBaseUrl private val apiBaseUrl: String,
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

    private val _attachmentDownloadEvents =
        MutableSharedFlow<AttachmentDownloadUiEvent>(extraBufferCapacity = 1)
    val attachmentDownloadEvents: SharedFlow<AttachmentDownloadUiEvent> =
        _attachmentDownloadEvents.asSharedFlow()

    private val _transientEvents = MutableSharedFlow<PostTransientUiEvent>(extraBufferCapacity = 1)
    val transientEvents: SharedFlow<PostTransientUiEvent> = _transientEvents.asSharedFlow()

    /** Открытие прямой ссылки на файл ([GET] `api/files/{id}`). */
    fun downloadAttachment(fileId: UUID) {
        viewModelScope.launch(dispatcher) {
            val url = buildFileDownloadUrl(apiBaseUrl, fileId)
            _attachmentDownloadEvents.emit(AttachmentDownloadUiEvent.OpenUrl(url))
        }
    }

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
                individualTask = null,
                isLoadingTeamSection = false,
                teamSectionError = null,
            )

        viewModelScope.launch(dispatcher) {
            val currentUser =
                when (val result = currentUserRepository.getCurrentUser()) {
                    is DomainResult.Success -> result.value
                    is DomainResult.Failure -> null
                }
            val currentUserId: UserId? = currentUser?.id
            val currentUserName: String? = currentUser?.credentials
            val postResult: DomainResult<Post> = getPost(postId)
            when (postResult) {
                is DomainResult.Success -> {
                    val post: Post = postResult.value
                    _uiState.value =
                        _uiState.value.copy(
                            isLoadingPost = false,
                            content = post.toPostScreenContent(),
                            currentUserId = currentUserId,
                            currentUserName = currentUserName,
                            individualTask =
                                if (post is TaskPost) {
                                    IndividualTaskPostState(
                                        isLoadingSolutionSection = true,
                                        sectionError = null,
                                    )
                                } else {
                                    null
                                },
                        )

                    if (post is TeamTaskPost) {
                        launch(dispatcher) { loadTeamTaskSection(post) }
                    }
                    if (post is TaskPost) {
                        launch(dispatcher) { loadIndividualTaskSection(post) }
                    }

                    val commentsResult: DomainResult<List<Comment>> =
                        loadPostCommentsWithFallback(
                            routePostId = postId,
                            loadedPost = post,
                        )
                    val comments: List<Comment> =
                        when (commentsResult) {
                            is DomainResult.Success -> commentsResult.value
                            is DomainResult.Failure -> emptyList()
                        }
                    val commentsUi: List<CommentUi> = comments.map { it.toUi(currentUserId) }
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

    private suspend fun loadPostCommentsWithFallback(
        routePostId: PostId,
        loadedPost: Post,
    ): DomainResult<List<Comment>> {
        Log.d(
            "PostScreenViewModel",
            "Public comments load started routePostId=${routePostId.value}, loadedPostId=${loadedPost.id.value}, type=${loadedPost::class.simpleName}",
        )
        val primary: DomainResult<List<Comment>> = getPostComments(routePostId)
        if (primary is DomainResult.Success) {
            Log.d(
                "PostScreenViewModel",
                "Public comments loaded by routePostId count=${primary.value.size}",
            )
            return primary
        }
        val primaryErrorText: String =
            when (primary) {
                is DomainResult.Failure -> primary.error.toString()
                else -> "unknown"
            }

        if (loadedPost.id.value == routePostId.value) {
            Log.e(
                "PostScreenViewModel",
                "Public comments failed and fallback skipped (same ids): $primaryErrorText",
            )
            return primary
        }

        Log.e(
            "PostScreenViewModel",
            "Public comments by routePostId failed, trying loadedPostId: $primaryErrorText",
        )
        val fallback: DomainResult<List<Comment>> = getPostComments(loadedPost.id)
        if (fallback is DomainResult.Success) {
            Log.d(
                "PostScreenViewModel",
                "Public comments loaded by loadedPostId count=${fallback.value.size}",
            )
        } else if (fallback is DomainResult.Failure) {
            Log.e(
                "PostScreenViewModel",
                "Public comments fallback failed: ${fallback.error}",
            )
        }
        return fallback
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
        val teamTask: TeamTaskPostState = _uiState.value.teamTask ?: return
        if (!teamTask.canLeaveTeam) return

        viewModelScope.launch(dispatcher) {
            when (leaveTeam(teamId)) {
                is DomainResult.Success -> loadTeamTaskSection(ttPost)
                is DomainResult.Failure ->
                    _uiState.value =
                        _uiState.value.copy(teamSectionError = "Не удалось покинуть команду")
            }
        }
    }

    fun onVoteCaptain(teamId: TeamId, candidateId: UserId) {
        val ttPost: TeamTaskPost =
            (_uiState.value.content as? PostScreenContent.TeamTask)?.post ?: return

        viewModelScope.launch(dispatcher) {
            when (voteTeamCaptain(teamId, candidateId)) {
                is DomainResult.Success -> {
                    _transientEvents.emit(PostTransientUiEvent.ShowMessage("Голос учтен"))
                    loadTeamTaskSection(ttPost)
                }
                is DomainResult.Failure ->
                    _transientEvents.emit(PostTransientUiEvent.ShowMessage("Что-то пошло не так"))
            }
        }
    }

    fun onTransferCaptain(teamId: TeamId, toUserId: UserId) {
        val ttPost: TeamTaskPost =
            (_uiState.value.content as? PostScreenContent.TeamTask)?.post ?: return

        viewModelScope.launch(dispatcher) {
            when (transferTeamCaptain(teamId, toUserId)) {
                is DomainResult.Success -> {
                    _transientEvents.emit(PostTransientUiEvent.ShowMessage("Роль капитана передана"))
                    loadTeamTaskSection(ttPost)
                }
                is DomainResult.Failure ->
                    _transientEvents.emit(PostTransientUiEvent.ShowMessage("Что-то пошло не так"))
            }
        }
    }

    /** Загрузка файла в черновик решения (командное или индивидуальное задание). */
    fun onPickedSolutionFile(bytes: ByteArray, fileName: String) {
        when (_uiState.value.content) {
            is PostScreenContent.TeamTask -> onPickedTeamSolutionFile(bytes, fileName)
            is PostScreenContent.Task -> onPickedIndividualSolutionFile(bytes, fileName)
            else -> Unit
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

    fun onPickedIndividualSolutionFile(bytes: ByteArray, fileName: String) {
        val taskPost: TaskPost =
            (_uiState.value.content as? PostScreenContent.Task)?.post ?: return
        val dl = taskPost.taskDetails.deadline
        if (dl != null && dl.isBefore(OffsetDateTime.now())) {
            _uiState.value =
                _uiState.value.copy(
                    individualTask =
                        (_uiState.value.individualTask ?: IndividualTaskPostState())
                            .copy(sectionError = "Срок сдачи прошёл"),
                )
            return
        }
        viewModelScope.launch(dispatcher) {
            when (val upload = fileRepository.uploadFile(bytes, fileName)) {
                is DomainResult.Success -> {
                    val file: FileInfo = upload.value
                    val ind: IndividualTaskPostState =
                        _uiState.value.individualTask ?: return@launch
                    _uiState.value =
                        _uiState.value.copy(
                            individualTask =
                                ind.copy(
                                    pendingSolutionFiles = ind.pendingSolutionFiles + file,
                                    sectionError = null,
                                ),
                        )
                }
                is DomainResult.Failure ->
                    _uiState.value =
                        _uiState.value.copy(
                            individualTask =
                                (_uiState.value.individualTask ?: IndividualTaskPostState())
                                    .copy(sectionError = "Не удалось загрузить файл"),
                        )
            }
        }
    }

    fun onRemovePendingIndividualSolutionFile(fileId: String) {
        val ind: IndividualTaskPostState = _uiState.value.individualTask ?: return
        _uiState.value =
            _uiState.value.copy(
                individualTask =
                    ind.copy(
                        pendingSolutionFiles = ind.pendingSolutionFiles.filter { it.id != fileId },
                    ),
            )
    }

    fun onRemoveSavedIndividualSolutionFile(fileId: String) {
        val ind: IndividualTaskPostState = _uiState.value.individualTask ?: return
        _uiState.value =
            _uiState.value.copy(
                individualTask =
                    ind.copy(
                        removedSavedSolutionFileIds = ind.removedSavedSolutionFileIds + fileId,
                    ),
            )
    }

    fun onSubmitIndividualSolution(text: String) {
        val taskPost: TaskPost =
            (_uiState.value.content as? PostScreenContent.Task)?.post ?: return
        val dl = taskPost.taskDetails.deadline
        if (dl != null && dl.isBefore(OffsetDateTime.now())) {
            _uiState.value =
                _uiState.value.copy(
                    individualTask =
                        (_uiState.value.individualTask ?: IndividualTaskPostState())
                            .copy(sectionError = "Срок сдачи прошёл"),
                )
            return
        }
        val ind: IndividualTaskPostState = _uiState.value.individualTask ?: return
        val taskId = TaskId(taskPost.id.value)
        val keptFromSaved: List<String> =
            ind.solution?.files
                ?.map { it.id }
                ?.filter { it !in ind.removedSavedSolutionFileIds }
                .orEmpty()
        val pendingIds: List<String> = ind.pendingSolutionFiles.map { it.id }
        val fileIds: List<String> = keptFromSaved + pendingIds

        viewModelScope.launch(dispatcher) {
            Log.d(
                "PostScreenViewModel",
                "Submit individual solution started taskId=${taskId.value}, savedFiles=${keptFromSaved.size}, pendingFiles=${pendingIds.size}, textBlank=${text.isBlank()}",
            )
            val result =
                submitSolution(
                    taskId = taskId,
                    text = text.ifBlank { null },
                    fileIds = fileIds,
                )
            when (result) {
                is DomainResult.Success -> {
                    Log.d(
                        "PostScreenViewModel",
                        "Submit individual solution success taskId=${taskId.value}",
                    )
                    _transientEvents.emit(PostTransientUiEvent.ShowMessage("Решение сохранено"))
                    loadIndividualTaskSection(taskPost)
                }
                is DomainResult.Failure -> {
                    Log.e(
                        "PostScreenViewModel",
                        "Submit individual solution failed taskId=${taskId.value}, error=${result.error}",
                    )
                    _uiState.value =
                        _uiState.value.copy(
                            individualTask = ind.copy(sectionError = "Не удалось отправить решение"),
                        )
                    _transientEvents.emit(PostTransientUiEvent.ShowMessage("Не удалось сохранить решение"))
                }
            }
        }
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
        val myTeamMembersCount: Int = teamTask.myTeam?.members?.size ?: 0
        if (myTeamMembersCount < 2) {
            _uiState.value =
                _uiState.value.copy(
                    teamSectionError = "В команде должно быть минимум 2 участника",
                )
            viewModelScope.launch(dispatcher) {
                _transientEvents.emit(
                    PostTransientUiEvent.ShowMessage("Нельзя отправить решение: в команде должно быть минимум 2 участника"),
                )
            }
            return
        }
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
            Log.d(
                "PostScreenViewModel",
                "Submit team solution started taskId=${taskId.value}, teamId=${teamId.value}, savedFiles=${keptFromSaved.size}, pendingFiles=${pendingIds.size}, textBlank=${text.isBlank()}",
            )
            val result =
                submitTeamTaskSolution(
                    taskId = taskId,
                    captainTeamId = teamId,
                    text = text.ifBlank { null },
                    fileIds = fileIds,
                )
            when (result) {
                is DomainResult.Success -> {
                    Log.d(
                        "PostScreenViewModel",
                        "Submit team solution success taskId=${taskId.value}, teamId=${teamId.value}",
                    )
                    _transientEvents.emit(PostTransientUiEvent.ShowMessage("Решение сохранено"))
                    loadTeamTaskSection(ttPost)
                }
                is DomainResult.Failure -> {
                    Log.e(
                        "PostScreenViewModel",
                        "Submit team solution failed taskId=${taskId.value}, teamId=${teamId.value}, error=${result.error}",
                    )
                    _uiState.value =
                        _uiState.value.copy(teamSectionError = "Не удалось отправить решение")
                    _transientEvents.emit(PostTransientUiEvent.ShowMessage("Не удалось сохранить решение"))
                }
            }
        }
    }

    fun onDeleteIndividualSolution() {
        val taskPost: TaskPost =
            (_uiState.value.content as? PostScreenContent.Task)?.post ?: return
        val ind = _uiState.value.individualTask ?: return
        if (ind.solution == null) return

        viewModelScope.launch(dispatcher) {
            when (cancelSolution(TaskId(taskPost.id.value))) {
                is DomainResult.Success -> {
                    _transientEvents.emit(PostTransientUiEvent.ShowMessage("Решение удалено"))
                    loadIndividualTaskSection(taskPost)
                }
                is DomainResult.Failure -> {
                    _uiState.value =
                        _uiState.value.copy(
                            individualTask = ind.copy(sectionError = "Не удалось удалить решение"),
                        )
                    _transientEvents.emit(PostTransientUiEvent.ShowMessage("Не удалось удалить решение"))
                }
            }
        }
    }

    fun onDeleteTeamSolution() {
        val ttPost: TeamTaskPost =
            (_uiState.value.content as? PostScreenContent.TeamTask)?.post ?: return
        val teamTask = _uiState.value.teamTask ?: return
        if (teamTask.solution == null) return

        viewModelScope.launch(dispatcher) {
            when (cancelTeamTaskSolution(TaskId(ttPost.id.value))) {
                is DomainResult.Success -> {
                    _transientEvents.emit(PostTransientUiEvent.ShowMessage("Решение удалено"))
                    loadTeamTaskSection(ttPost)
                }
                is DomainResult.Failure -> {
                    _uiState.value =
                        _uiState.value.copy(teamSectionError = "Не удалось удалить решение")
                    _transientEvents.emit(PostTransientUiEvent.ShowMessage("Не удалось удалить решение"))
                }
            }
        }
    }

    fun onEditComment(
        commentId: CommentId,
        newText: String,
        isPrivate: Boolean,
    ) {
        val trimmed = newText.trim()
        if (trimmed.isBlank()) {
            viewModelScope.launch(dispatcher) {
                _transientEvents.emit(PostTransientUiEvent.ShowMessage("Комментарий не должен быть пустым"))
            }
            return
        }
        viewModelScope.launch(dispatcher) {
            when (editComment(commentId, trimmed)) {
                is DomainResult.Success -> {
                    if (isPrivate) {
                        val teamTask = _uiState.value.teamTask ?: return@launch
                        _uiState.value =
                            _uiState.value.copy(
                                teamTask =
                                    teamTask.copy(
                                        solutionComments = updateCommentText(teamTask.solutionComments, commentId.value, trimmed),
                                    ),
                            )
                    } else {
                        _uiState.value =
                            _uiState.value.copy(
                                comments = updateCommentText(_uiState.value.comments, commentId.value, trimmed),
                            )
                    }
                    _transientEvents.emit(PostTransientUiEvent.ShowMessage("Комментарий обновлён"))
                }
                is DomainResult.Failure ->
                    _transientEvents.emit(PostTransientUiEvent.ShowMessage("Не удалось обновить комментарий"))
            }
        }
    }

    fun onDeleteComment(commentId: CommentId, isPrivate: Boolean) {
        viewModelScope.launch(dispatcher) {
            when (deleteComment(commentId)) {
                is DomainResult.Success -> {
                    if (isPrivate) {
                        val teamTask = _uiState.value.teamTask ?: return@launch
                        _uiState.value =
                            _uiState.value.copy(
                                teamTask =
                                    teamTask.copy(
                                        solutionComments = removeComment(teamTask.solutionComments, commentId.value),
                                    ),
                            )
                    } else {
                        _uiState.value =
                            _uiState.value.copy(
                                comments = removeComment(_uiState.value.comments, commentId.value),
                            )
                    }
                    _transientEvents.emit(PostTransientUiEvent.ShowMessage("Комментарий удалён"))
                }
                is DomainResult.Failure ->
                    _transientEvents.emit(PostTransientUiEvent.ShowMessage("Не удалось удалить комментарий"))
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
                    Log.d(
                        "PostScreenViewModel",
                        "TeamTask comments load started solutionId=${solutionId.value}",
                    )
                    getSolutionComments(solutionId)
                } else {
                    Log.d(
                        "PostScreenViewModel",
                        "TeamTask comments skipped: solutionId is null",
                    )
                    null
                }
            if (solutionCommentsResult is DomainResult.Failure) {
                Log.e(
                    "PostScreenViewModel",
                    "TeamTask comments load failed: ${solutionCommentsResult.error}",
                )
            }
            val solutionCommentsUi: List<CommentUi> =
                when (solutionCommentsResult) {
                    null -> emptyList()
                    is DomainResult.Success ->
                        solutionCommentsResult.value.map { it.toUi(currentUserId).copy(isPrivate = true) }
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

            val assignmentDeadline = post.taskDetails.deadline
            val deadlinePassed: Boolean =
                assignmentDeadline?.isBefore(OffsetDateTime.now()) == true
            val canLeaveByApi: Boolean = post.allowLeaveTeam ?: true
            val canLeaveTeam: Boolean = canLeaveByApi && !deadlinePassed

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
                            canLeaveTeam = canLeaveTeam,
                        ),
                )
        }
    }

    private suspend fun loadIndividualTaskSection(post: TaskPost) {
        coroutineScope {
            _uiState.value =
                _uiState.value.copy(
                    individualTask =
                        IndividualTaskPostState(
                            isLoadingSolutionSection = true,
                            sectionError = null,
                        ),
                )
            val taskId = TaskId(post.id.value)
            val solDeferred = async { getUserSolution(taskId) }
            val currentUserDeferred = async { currentUserRepository.getCurrentUser() }

            val solResult = solDeferred.await()
            val currentUserId: UserId? =
                when (val u = currentUserDeferred.await()) {
                    is DomainResult.Success -> u.value.id
                    is DomainResult.Failure -> null
                }

            val solution =
                when (solResult) {
                    is DomainResult.Success -> solResult.value
                    is DomainResult.Failure -> null
                }

            val sectionError: String? =
                if (solResult is DomainResult.Failure) {
                    "Не удалось загрузить решение"
                } else {
                    null
                }

            _uiState.value =
                _uiState.value.copy(
                    individualTask =
                        IndividualTaskPostState(
                            solution = solution,
                            isLoadingSolutionSection = false,
                            sectionError = sectionError,
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
                        val createdUi: CommentUi =
                            normalizeCreatedCommentAuthor(
                                comment = created.toUi(current.currentUserId),
                                current = current,
                            )
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
                        val createdUi: CommentUi =
                            normalizeCreatedCommentAuthor(
                                comment = result.value.toUi(current.currentUserId),
                                current = current,
                            ).copy(isPrivate = true)
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
                    val createdUi: CommentUi =
                        normalizeCreatedCommentAuthor(
                            comment = created.toUi(current.currentUserId),
                            current = current,
                        )
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
                val repliesUi: List<CommentUi> = result.value.map { it.toUi(_uiState.value.currentUserId) }
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

    private fun Comment.toUi(currentUserId: UserId?): CommentUi =
        CommentUi(
            id = id,
            authorId = author.id.value.toString(),
            authorName = author.credentials,
            text = text,
            createdAtLabel = formatCommentDate(createdAt),
            isOwn = currentUserId != null && author.id == currentUserId,
            isPrivate = isPrivate,
            replies = emptyList(),
            repliesLoaded = false,
        )

    private fun removeComment(comments: List<CommentUi>, commentId: String): List<CommentUi> =
        comments
            .filterNot { it.id == commentId }
            .map { comment ->
                comment.copy(replies = removeComment(comment.replies, commentId))
            }

    private fun updateCommentText(
        comments: List<CommentUi>,
        commentId: String,
        newText: String,
    ): List<CommentUi> =
        comments.map { comment ->
            when {
                comment.id == commentId -> comment.copy(text = newText)
                else ->
                    comment.copy(
                        replies = updateCommentText(comment.replies, commentId, newText),
                    )
            }
        }

    private fun normalizeCreatedCommentAuthor(comment: CommentUi, current: PostUiState): CommentUi {
        val fallbackAuthorId: String? = current.currentUserId?.value?.toString()
        val fallbackAuthorName: String = current.currentUserName?.takeIf { it.isNotBlank() } ?: "Вы"
        return comment.copy(
            authorId = comment.authorId.takeIf { it.isNotBlank() } ?: fallbackAuthorId.orEmpty(),
            authorName = comment.authorName.takeIf { it.isNotBlank() } ?: fallbackAuthorName,
            isOwn = true,
        )
    }
}
