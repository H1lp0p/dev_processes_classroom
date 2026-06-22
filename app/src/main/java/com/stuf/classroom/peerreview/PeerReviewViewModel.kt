package com.stuf.classroom.peerreview

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stuf.classroom.grading.RubricWizardUiState
import com.stuf.classroom.navigation.PeerReviewNavArgs
import com.stuf.classroom.post.AttachmentDownloadUiEvent
import com.stuf.classroom.post.buildFileDownloadUrl
import com.stuf.classroom.selfassessment.SelfAssessmentFormStep
import com.stuf.data.di.ApiBaseUrl
import com.stuf.domain.common.DomainError
import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.AnonymizedSolution
import com.stuf.domain.model.CourseRole
import com.stuf.domain.model.GradingMode
import com.stuf.domain.model.PeerReviewTarget
import com.stuf.domain.model.PeerReviewTeamTarget
import com.stuf.domain.model.PostId
import com.stuf.domain.model.TaskId
import com.stuf.domain.model.TaskPost
import com.stuf.domain.model.TeamTaskPost
import com.stuf.domain.model.navigationBlockMessage
import com.stuf.domain.model.peerReviewPostInfo
import com.stuf.domain.usecase.GetAvailableTeamPeerReviews
import com.stuf.domain.usecase.GetNextPeerReview
import com.stuf.domain.usecase.GetPost
import com.stuf.domain.usecase.GetTeamTaskSolution
import com.stuf.domain.usecase.GetUserSolution
import com.stuf.domain.usecase.SubmitPeerReview
import com.stuf.domain.usecase.SubmitTeamPeerReview
import com.stuf.grading.domain.model.CriterionId
import com.stuf.grading.domain.model.SelfAssessmentDraft
import com.stuf.grading.domain.model.TaskGradingRubric
import com.stuf.grading.domain.preview.RubricPreviewEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class PeerReviewMode {
    INDIVIDUAL,
    TEAM,
}

data class PeerReviewUiState(
    val isLoading: Boolean = true,
    val loadError: String? = null,
    val mode: PeerReviewMode = PeerReviewMode.INDIVIDUAL,
    val taskTitle: String = "",
    val reviewTarget: PeerReviewTarget? = null,
    val anonymizedSolution: AnonymizedSolution? = null,
    val selectedTeamTarget: PeerReviewTeamTarget? = null,
    val availableTeamTargets: List<PeerReviewTeamTarget> = emptyList(),
    val rubric: TaskGradingRubric? = null,
    val draft: SelfAssessmentDraft = SelfAssessmentDraft(),
    val localPreview: com.stuf.grading.domain.preview.RubricPreviewResult? = null,
    val formSteps: List<SelfAssessmentFormStep> = emptyList(),
    val wizardStepIndex: Int = 0,
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val submitSuccess: Boolean = false,
)

@HiltViewModel
class PeerReviewViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getPost: GetPost,
    private val getUserSolution: GetUserSolution,
    private val getTeamTaskSolution: GetTeamTaskSolution,
    private val getNextPeerReview: GetNextPeerReview,
    private val submitPeerReview: SubmitPeerReview,
    private val getAvailableTeamPeerReviews: GetAvailableTeamPeerReviews,
    private val submitTeamPeerReview: SubmitTeamPeerReview,
    private val draftStorage: PeerReviewDraftStorage,
    @ApiBaseUrl private val apiBaseUrl: String,
) : ViewModel() {

    private val postId: String = checkNotNull(savedStateHandle["postId"]) as String
    private val teamSolutionIdArg: String? =
        savedStateHandle.get<String>("teamSolutionId")?.takeUnless { it == PeerReviewNavArgs.NO_TEAM_SOLUTION }

    @Suppress("UNUSED_PARAMETER")
    private val role: CourseRole =
        when ((checkNotNull(savedStateHandle["role"]) as String).lowercase()) {
            "teacher" -> CourseRole.TEACHER
            else -> CourseRole.STUDENT
        }

    private val _uiState = MutableStateFlow(PeerReviewUiState())
    val uiState: StateFlow<PeerReviewUiState> = _uiState.asStateFlow()

    private val _attachmentDownloadEvents =
        MutableSharedFlow<AttachmentDownloadUiEvent>(extraBufferCapacity = 1)
    val attachmentDownloadEvents: SharedFlow<AttachmentDownloadUiEvent> =
        _attachmentDownloadEvents.asSharedFlow()

    private var storageKey: String? = null

    init {
        viewModelScope.launch { load() }
    }

    fun onTeamTargetSelected(target: PeerReviewTeamTarget) {
        if (target.alreadyReviewed) return
        viewModelScope.launch { setupTeamReview(target) }
    }

    fun onWizardNavigateBack(): Boolean {
        val s = _uiState.value
        if (s.rubric == null) return true
        if (s.formSteps.isEmpty()) return true
        val isResult = s.wizardStepIndex >= s.formSteps.size
        return when {
            isResult -> {
                _uiState.update { it.copy(wizardStepIndex = s.formSteps.lastIndex) }
                persistDraft()
                false
            }
            s.wizardStepIndex > 0 -> {
                _uiState.update { it.copy(wizardStepIndex = s.wizardStepIndex - 1) }
                persistDraft()
                false
            }
            else -> true
        }
    }

    fun onWizardNavigateNext() {
        val s = _uiState.value
        if (s.formSteps.isEmpty() || s.rubric == null) return
        if (s.wizardStepIndex < s.formSteps.size) {
            val nextIndex = s.wizardStepIndex + 1
            _uiState.update { it.copy(wizardStepIndex = nextIndex) }
            persistDraft()
        }
    }

    fun onWeightedScoreChange(criterionId: CriterionId, value: Double) {
        val rubric = _uiState.value.rubric ?: return
        val def =
            rubric.criteria.filterIsInstance<com.stuf.grading.domain.model.CriterionDefinition.Weighted>()
                .find { it.id == criterionId } ?: return
        applyDraft(
            _uiState.value.draft.copy(
                weightedScores = _uiState.value.draft.weightedScores + (criterionId to value.coerceIn(0.0, def.maxScore)),
            ),
        )
    }

    fun onToggleChange(criterionId: CriterionId, enabled: Boolean) {
        applyDraft(
            _uiState.value.draft.copy(
                toggledEnabled = _uiState.value.draft.toggledEnabled + (criterionId to enabled),
            ),
        )
    }

    fun submitReview() {
        val s = _uiState.value
        if (s.rubric == null || s.isSaving) return
        val taskUuid = TaskId(UUID.fromString(postId))
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null) }
            val result =
                when (s.mode) {
                    PeerReviewMode.INDIVIDUAL -> {
                        val reviewId = s.reviewTarget?.reviewId ?: run {
                            _uiState.update { it.copy(isSaving = false, saveError = "Нет назначения") }
                            return@launch
                        }
                        submitPeerReview(reviewId, s.draft)
                    }
                    PeerReviewMode.TEAM -> {
                        val teamSolutionId = s.selectedTeamTarget?.teamSolutionId ?: run {
                            _uiState.update { it.copy(isSaving = false, saveError = "Выберите команду") }
                            return@launch
                        }
                        submitTeamPeerReview(teamSolutionId, s.draft)
                    }
                }
            when (result) {
                is DomainResult.Success -> {
                    storageKey?.let { draftStorage.clear(it) }
                    _uiState.update { it.copy(isSaving = false, submitSuccess = true) }
                }
                is DomainResult.Failure -> {
                    _uiState.update {
                        it.copy(isSaving = false, saveError = mapError(result.error))
                    }
                }
            }
        }
    }

    fun onSubmitSuccessConsumed() {
        _uiState.update { it.copy(submitSuccess = false) }
    }

    fun downloadAttachment(fileId: UUID) {
        val url = buildFileDownloadUrl(apiBaseUrl, fileId)
        _attachmentDownloadEvents.tryEmit(AttachmentDownloadUiEvent.OpenUrl(url))
    }

    fun wizardUiState(): RubricWizardUiState? {
        val s = _uiState.value
        val rubric = s.rubric ?: return null
        val preview = s.localPreview ?: return null
        return RubricWizardUiState(
            rubric = rubric,
            draft = s.draft,
            localPreview = preview,
            formSteps = s.formSteps,
            wizardStepIndex = s.wizardStepIndex,
            isSaving = s.isSaving,
            saveError = s.saveError,
            showStudentScoreWeightHint = false,
        )
    }

    private suspend fun load() {
        _uiState.update { it.copy(isLoading = true, loadError = null) }
        val postIdUuid = PostId(UUID.fromString(postId))
        val post =
            when (val postRes = getPost(postIdUuid)) {
                is DomainResult.Success -> postRes.value
                is DomainResult.Failure -> {
                    _uiState.update { it.copy(isLoading = false, loadError = mapError(postRes.error)) }
                    return
                }
            }
        val taskUuid = TaskId(UUID.fromString(postId))
        when (post) {
            is TaskPost -> {
                if (post.gradingMode != GradingMode.PEER_TO_PEER) {
                    _uiState.update {
                        it.copy(isLoading = false, loadError = "Задание не в режиме P2P")
                    }
                    return
                }
                val hasSolution =
                    when (val solutionRes = getUserSolution(taskUuid)) {
                        is DomainResult.Success -> solutionRes.value != null
                        is DomainResult.Failure -> {
                            _uiState.update {
                                it.copy(isLoading = false, loadError = mapError(solutionRes.error))
                            }
                            return
                        }
                    }
                post.peerReviewPostInfo().navigationBlockMessage(hasSolution)?.let { block ->
                    _uiState.update { it.copy(isLoading = false, loadError = block) }
                    return
                }
                when (val nextRes = getNextPeerReview(taskUuid)) {
                    is DomainResult.Success -> {
                        val target = nextRes.value
                        if (target == null) {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    loadError = "Нет доступных работ для оценки",
                                    taskTitle = post.title,
                                    mode = PeerReviewMode.INDIVIDUAL,
                                )
                            }
                            return
                        }
                        setupIndividualReview(post.title, post.gradingRubric, target)
                    }
                    is DomainResult.Failure -> {
                        _uiState.update {
                            it.copy(isLoading = false, loadError = mapError(nextRes.error))
                        }
                    }
                }
            }
            is TeamTaskPost -> {
                if (post.gradingMode != GradingMode.PEER_TO_PEER) {
                    _uiState.update {
                        it.copy(isLoading = false, loadError = "Задание не в режиме P2P")
                    }
                    return
                }
                val hasSolution =
                    when (val solutionRes = getTeamTaskSolution(taskUuid)) {
                        is DomainResult.Success -> solutionRes.value != null
                        is DomainResult.Failure -> {
                            _uiState.update {
                                it.copy(isLoading = false, loadError = mapError(solutionRes.error))
                            }
                            return
                        }
                    }
                post.peerReviewPostInfo().navigationBlockMessage(hasSolution)?.let { block ->
                    _uiState.update { it.copy(isLoading = false, loadError = block) }
                    return
                }
                when (val listRes = getAvailableTeamPeerReviews(taskUuid)) {
                    is DomainResult.Success -> {
                        val available = listRes.value.filter { !it.alreadyReviewed }
                        val preselected =
                            teamSolutionIdArg?.let { id ->
                                available.firstOrNull {
                                    it.teamSolutionId.value.toString() == id
                                }
                            }
                        if (preselected != null) {
                            setupTeamReview(preselected, post.title, post.gradingRubric)
                        } else {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    loadError = null,
                                    taskTitle = post.title,
                                    mode = PeerReviewMode.TEAM,
                                    availableTeamTargets = available,
                                    rubric = post.gradingRubric,
                                )
                            }
                        }
                    }
                    is DomainResult.Failure -> {
                        _uiState.update {
                            it.copy(isLoading = false, loadError = mapError(listRes.error))
                        }
                    }
                }
            }
            else -> {
                _uiState.update { it.copy(isLoading = false, loadError = "Не задание") }
            }
        }
    }

    private suspend fun setupIndividualReview(
        title: String,
        postRubric: TaskGradingRubric?,
        target: PeerReviewTarget,
    ) {
        val rubric = buildRubric(postRubric, target.criteria, title)
        storageKey =
            draftStorage.storageKey(postId, target.reviewId.value.toString())
        restoreOrDefaultDraft(rubric, storageKey!!)
        _uiState.update {
            it.copy(
                isLoading = false,
                loadError = null,
                taskTitle = title,
                mode = PeerReviewMode.INDIVIDUAL,
                reviewTarget = target,
                anonymizedSolution = target.solution,
                rubric = rubric,
            )
        }
    }

    private suspend fun setupTeamReview(target: PeerReviewTeamTarget) {
        val postIdUuid = PostId(UUID.fromString(postId))
        val post =
            when (val postRes = getPost(postIdUuid)) {
                is DomainResult.Success -> postRes.value as? TeamTaskPost
                is DomainResult.Failure -> null
            } ?: return
        setupTeamReview(target, post.title, post.gradingRubric)
    }

    private fun setupTeamReview(
        target: PeerReviewTeamTarget,
        title: String,
        postRubric: TaskGradingRubric?,
    ) {
        val rubric = buildRubric(postRubric, postRubric?.criteria.orEmpty(), title)
        storageKey =
            draftStorage.storageKey(postId, target.teamSolutionId.value.toString())
        restoreOrDefaultDraft(rubric, storageKey!!)
        _uiState.update {
            it.copy(
                isLoading = false,
                loadError = null,
                taskTitle = title,
                mode = PeerReviewMode.TEAM,
                selectedTeamTarget = target,
                availableTeamTargets = emptyList(),
                anonymizedSolution = null,
                rubric = rubric,
            )
        }
    }

    private fun restoreOrDefaultDraft(rubric: TaskGradingRubric, key: String) {
        val saved = draftStorage.load(key)
        val draft = saved?.draft ?: RubricPreviewEngine.defaultDraft(rubric)
        val preview = RubricPreviewEngine.preview(rubric, draft)
        val steps = SelfAssessmentFormStep.build(rubric)
        _uiState.update {
            it.copy(
                draft = draft,
                localPreview = preview,
                formSteps = steps,
                wizardStepIndex = saved?.wizardStepIndex?.coerceIn(0, steps.size) ?: 0,
            )
        }
    }

    private fun applyDraft(draft: SelfAssessmentDraft) {
        val rubric = _uiState.value.rubric ?: return
        val preview = RubricPreviewEngine.preview(rubric, draft)
        _uiState.update { it.copy(draft = draft, localPreview = preview, saveError = null) }
        persistDraft()
    }

    private fun persistDraft() {
        val key = storageKey ?: return
        val s = _uiState.value
        draftStorage.save(key, s.draft, s.wizardStepIndex)
    }

    private fun buildRubric(
        base: TaskGradingRubric?,
        criteria: List<com.stuf.grading.domain.model.CriterionDefinition>,
        title: String,
    ): TaskGradingRubric {
        if (base != null && criteria.isEmpty()) return base
        val useCriteria = criteria.ifEmpty { base?.criteria.orEmpty() }
        val maxScore =
            base?.assignmentMaxScore
                ?: useCriteria.filterIsInstance<com.stuf.grading.domain.model.CriterionDefinition.Weighted>()
                    .sumOf { it.maxScore * it.weight }
                    .coerceAtLeast(1.0)
        return TaskGradingRubric(
            taskId = postId,
            title = base?.title ?: title,
            assignmentMaxScore = maxScore,
            criteria = useCriteria,
            studentScoreWeight = base?.studentScoreWeight ?: 0.0,
        )
    }

    private fun mapError(error: DomainError): String =
        when (error) {
            DomainError.Unauthorized -> "Не авторизован"
            DomainError.Forbidden -> "Доступ запрещён"
            DomainError.NotFound -> "Не найдено"
            is DomainError.Validation -> error.message
            is DomainError.Network -> "Ошибка сети"
            is DomainError.Unknown -> error.cause?.message ?: "Неизвестная ошибка"
        }
}
