package com.stuf.classroom.selfassessment

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stuf.domain.common.DomainError
import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.CourseRole
import com.stuf.domain.model.PostId
import com.stuf.domain.model.SolutionStatus
import com.stuf.domain.model.TaskId
import com.stuf.domain.model.TaskPost
import com.stuf.domain.model.TeamTaskPost
import com.stuf.domain.model.UserId
import com.stuf.domain.model.navigationBlockMessage
import com.stuf.domain.model.selfAssessmentPostInfo
import com.stuf.domain.repository.CurrentUserRepository
import com.stuf.domain.usecase.DeleteIndividualSelfAssessment
import com.stuf.domain.usecase.DeleteTeamSelfAssessment
import com.stuf.domain.usecase.GetPost
import com.stuf.domain.usecase.GetTaskGradingRubric
import com.stuf.domain.usecase.GetTeamTaskSolution
import com.stuf.domain.usecase.GetUserSolution
import com.stuf.domain.usecase.SubmitIndividualSelfAssessment
import com.stuf.domain.usecase.SubmitTeamSelfAssessment
import com.stuf.grading.domain.model.CriterionDefinition
import com.stuf.grading.domain.model.CriterionId
import com.stuf.grading.domain.model.SelfAssessmentDraft
import com.stuf.grading.domain.model.TaskGradingRubric
import com.stuf.grading.domain.preview.RubricPreviewEngine
import com.stuf.grading.domain.preview.RubricPreviewResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SelfAssessmentUiState(
    val isLoading: Boolean = true,
    val loadError: String? = null,
    val taskId: String = "",
    val isTeamTask: Boolean = false,
    val rubric: TaskGradingRubric? = null,
    val draft: SelfAssessmentDraft = SelfAssessmentDraft(),
    /** Расчёт по критериям на клиенте (не серверный preview для учителя). */
    val localPreview: RubricPreviewResult? = null,
    val formSteps: List<SelfAssessmentFormStep> = emptyList(),
    val wizardStepIndex: Int = 0,
    val isReadOnly: Boolean = false,
    val teamSelfAssessmentSubmittedCount: Int = 0,
    val teamSelfAssessmentTotalCount: Int = 0,
    val hasMySubmission: Boolean = false,
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val saveSuccess: Boolean = false,
)

@HiltViewModel
class SelfAssessmentViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getPost: GetPost,
    private val getTaskGradingRubric: GetTaskGradingRubric,
    private val getUserSolution: GetUserSolution,
    private val getTeamTaskSolution: GetTeamTaskSolution,
    private val submitIndividualSelfAssessment: SubmitIndividualSelfAssessment,
    private val submitTeamSelfAssessment: SubmitTeamSelfAssessment,
    private val deleteTeamSelfAssessment: DeleteTeamSelfAssessment,
    private val deleteIndividualSelfAssessment: DeleteIndividualSelfAssessment,
    private val currentUserRepository: CurrentUserRepository,
    private val featureConfig: SelfAssessmentFeatureConfig,
) : ViewModel() {

    private val taskId: String = checkNotNull(savedStateHandle["postId"]) as String

    @Suppress("UNUSED_PARAMETER")
    private val role: CourseRole =
        when ((checkNotNull(savedStateHandle["role"]) as String).lowercase()) {
            "teacher" -> CourseRole.TEACHER
            else -> CourseRole.STUDENT
        }

    private val _uiState = MutableStateFlow(SelfAssessmentUiState())
    val uiState: StateFlow<SelfAssessmentUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            load()
        }
    }

    private suspend fun load() {
        _uiState.update { it.copy(isLoading = true, loadError = null, saveError = null, saveSuccess = false) }
        if (!featureConfig.enableTeamReflexyScore) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    loadError = "Самооценка отключена в конфигурации приложения",
                    taskId = taskId,
                )
            }
            return
        }

        val postId = PostId(java.util.UUID.fromString(taskId))
        val rubricResult = getTaskGradingRubric(postId)
        val rubric =
            when (rubricResult) {
                is DomainResult.Success -> rubricResult.value
                is DomainResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            loadError = mapError(rubricResult.error),
                            taskId = taskId,
                        )
                    }
                    return
                }
            }
        if (rubric == null) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    loadError = "Самооценка для этого задания не настроена",
                    taskId = taskId,
                )
            }
            return
        }

        val post =
            when (val postRes = getPost(postId)) {
                is DomainResult.Success -> postRes.value
                is DomainResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            loadError = mapError(postRes.error),
                            taskId = taskId,
                        )
                    }
                    return
                }
            }

        val isTeam = post is TeamTaskPost
        val saInfo =
            when (post) {
                is TeamTaskPost -> post.selfAssessmentPostInfo()
                is TaskPost -> post.selfAssessmentPostInfo()
                else -> null
            }

        val taskUuid = TaskId(java.util.UUID.fromString(taskId))
        var myDraft: SelfAssessmentDraft? = null
        var hasSolution = false
        var solutionChecked = false
        var submittedCount = 0
        var teamSize = 0

        if (isTeam) {
            when (val solutionResult = getTeamTaskSolution(taskUuid)) {
                is DomainResult.Success -> {
                    val solution = solutionResult.value
                    hasSolution = solution != null
                    solutionChecked = solution?.status == SolutionStatus.CHECKED
                    val currentUserId = resolveCurrentUserId()
                    myDraft =
                        solution
                            ?.memberSelfAssessments
                            ?.firstOrNull { currentUserId != null && it.userId == currentUserId }
                            ?.evaluation
                    submittedCount =
                        solution?.memberSelfAssessments?.count { it.evaluation != null } ?: 0
                    teamSize = solution?.team?.members?.size ?: 0
                }
                is DomainResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            loadError = mapError(solutionResult.error),
                            taskId = taskId,
                        )
                    }
                    return
                }
            }
        } else if (post is TaskPost) {
            when (val solutionResult = getUserSolution(taskUuid)) {
                is DomainResult.Success -> {
                    val solution = solutionResult.value
                    hasSolution = solution != null
                    solutionChecked = solution?.status == SolutionStatus.CHECKED
                    myDraft = solution?.selfAssessment
                }
                is DomainResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            loadError = mapError(solutionResult.error),
                            taskId = taskId,
                        )
                    }
                    return
                }
            }
        }

        saInfo?.navigationBlockMessage(
            hasSolution = hasSolution,
            isTeamTask = isTeam,
            isSolutionChecked = solutionChecked,
        )?.let { block ->
            _uiState.update {
                it.copy(
                    isLoading = false,
                    loadError = block,
                    taskId = taskId,
                )
            }
            return
        }

        val draft = myDraft ?: RubricPreviewEngine.defaultDraft(rubric)
        val localPreview = RubricPreviewEngine.preview(rubric, draft)
        val steps = SelfAssessmentFormStep.build(rubric)
        val deadlineClosed = saInfo?.isCurrentlyOpen == false

        _uiState.update {
            it.copy(
                isLoading = false,
                loadError = null,
                taskId = taskId,
                isTeamTask = isTeam,
                rubric = rubric,
                draft = draft,
                localPreview = localPreview,
                formSteps = steps,
                wizardStepIndex = 0,
                isReadOnly = deadlineClosed || solutionChecked || role == CourseRole.TEACHER,
                teamSelfAssessmentSubmittedCount = submittedCount,
                teamSelfAssessmentTotalCount = teamSize,
                hasMySubmission = myDraft != null,
                isSaving = false,
            )
        }
    }

    fun onWizardNavigateBack(): Boolean {
        val s = _uiState.value
        if (s.rubric == null) return true
        if (s.formSteps.isEmpty()) {
            return true
        }
        val isResult = s.wizardStepIndex >= s.formSteps.size
        return when {
            isResult -> {
                _uiState.update { it.copy(wizardStepIndex = s.formSteps.lastIndex) }
                false
            }
            s.wizardStepIndex > 0 -> {
                _uiState.update { it.copy(wizardStepIndex = s.wizardStepIndex - 1) }
                false
            }
            else -> true
        }
    }

    fun onWizardNavigateNext() {
        val s = _uiState.value
        if (s.formSteps.isEmpty()) return
        if (s.wizardStepIndex < s.formSteps.size) {
            val nextIndex = s.wizardStepIndex + 1
            _uiState.update { it.copy(wizardStepIndex = nextIndex) }
            if (nextIndex >= s.formSteps.size) {
                applyDraft(s.rubric!!, s.draft)
            }
        }
    }

    fun onWeightedScoreChange(criterionId: CriterionId, value: Double) {
        val rubric = _uiState.value.rubric ?: return
        if (_uiState.value.isReadOnly) return
        val def =
            rubric.criteria.filterIsInstance<CriterionDefinition.Weighted>().find { it.id == criterionId }
                ?: return
        val coerced = value.coerceIn(0.0, def.maxScore)
        val newDraft =
            _uiState.value.draft.copy(
                weightedScores = _uiState.value.draft.weightedScores + (criterionId to coerced),
            )
        applyDraft(rubric, newDraft)
    }

    fun onToggleChange(criterionId: CriterionId, enabled: Boolean) {
        val rubric = _uiState.value.rubric ?: return
        if (_uiState.value.isReadOnly) return
        val isBonusPenalty =
            rubric.criteria
                .filterIsInstance<CriterionDefinition.ManualBonusPenalty>()
                .any { it.id == criterionId }
        if (!isBonusPenalty) return
        val newDraft =
            _uiState.value.draft.copy(
                toggledEnabled = _uiState.value.draft.toggledEnabled + (criterionId to enabled),
            )
        applyDraft(rubric, newDraft)
    }

    fun submitSelfAssessment() {
        if (_uiState.value.rubric == null) return
        if (_uiState.value.isReadOnly || _uiState.value.isSaving) return
        val taskUuid = TaskId(java.util.UUID.fromString(taskId))
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null, saveSuccess = false) }
            val result =
                if (_uiState.value.isTeamTask) {
                    submitTeamSelfAssessment(taskUuid, _uiState.value.draft)
                } else {
                    submitIndividualSelfAssessment(taskUuid, _uiState.value.draft)
                }
            when (result) {
                is DomainResult.Success -> {
                    _uiState.update { it.copy(isSaving = false, saveSuccess = true, hasMySubmission = true) }
                }
                is DomainResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            saveError = mapError(result.error),
                        )
                    }
                }
            }
        }
    }

    fun onSaveSuccessConsumed() {
        _uiState.update { it.copy(saveSuccess = false) }
    }

    fun deleteMySelfAssessment() {
        if (_uiState.value.isReadOnly || _uiState.value.isSaving) return
        val taskUuid = TaskId(java.util.UUID.fromString(taskId))
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null) }
            val result =
                if (_uiState.value.isTeamTask) {
                    deleteTeamSelfAssessment(taskUuid)
                } else {
                    deleteIndividualSelfAssessment(taskUuid)
                }
            when (result) {
                is DomainResult.Success -> load()
                is DomainResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            saveError = mapError(result.error),
                        )
                    }
                }
            }
        }
    }

    private fun applyDraft(rubric: TaskGradingRubric, draft: SelfAssessmentDraft) {
        val localPreview = RubricPreviewEngine.preview(rubric, draft)
        _uiState.update {
            it.copy(
                draft = draft,
                localPreview = localPreview,
                saveSuccess = false,
            )
        }
    }

    private suspend fun resolveCurrentUserId(): UserId? =
        when (val u = currentUserRepository.getCurrentUser()) {
            is DomainResult.Success -> u.value.id
            is DomainResult.Failure -> null
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
