package com.stuf.classroom.grade

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stuf.classroom.di.MainDispatcher
import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.GradeDistributionEntry
import com.stuf.domain.model.GradeVote
import com.stuf.domain.model.PostId
import com.stuf.domain.model.TeamId
import com.stuf.domain.model.UserId
import com.stuf.domain.usecase.CheckTeamCaptain
import com.stuf.domain.usecase.GetGradeDistribution
import com.stuf.domain.usecase.GetTeamsForTeamTask
import com.stuf.domain.usecase.SaveGradeDistribution
import com.stuf.domain.usecase.VoteOnGradeDistribution
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID
import kotlin.math.abs

data class GradeDistributionMemberRow(
    val userId: UserId,
    val displayName: String,
)

data class GradeDistributionUiState(
    val isLoading: Boolean = true,
    val loadError: String? = null,
    val teamRawScore: Double = 0.0,
    val members: List<GradeDistributionMemberRow> = emptyList(),
    val draftPoints: Map<String, String> = emptyMap(),
    val remainder: Double = 0.0,
    val remainderNegative: Boolean = false,
    val isCaptain: Boolean = false,
    val currentUserVote: GradeVote? = null,
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val isVoting: Boolean = false,
)

@HiltViewModel
class GradeDistributionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getGradeDistribution: GetGradeDistribution,
    private val saveGradeDistribution: SaveGradeDistribution,
    private val voteOnGradeDistribution: VoteOnGradeDistribution,
    private val getTeamsForTeamTask: GetTeamsForTeamTask,
    private val checkTeamCaptain: CheckTeamCaptain,
    @MainDispatcher private val dispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val teamId: TeamId =
        TeamId(UUID.fromString(checkNotNull(savedStateHandle["teamId"]) as String))
    private val assignmentId: PostId =
        PostId(UUID.fromString(checkNotNull(savedStateHandle["postId"]) as String))

    private val _uiState = MutableStateFlow(GradeDistributionUiState())
    val uiState: StateFlow<GradeDistributionUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch(dispatcher) { loadInternal() }
    }

    private suspend fun loadInternal() {
        _uiState.value = _uiState.value.copy(isLoading = true, loadError = null, saveError = null)
        val cap =
            when (val c = checkTeamCaptain(teamId)) {
                is DomainResult.Success -> c.value
                is DomainResult.Failure -> false
            }
        when (val dist = getGradeDistribution(teamId, assignmentId)) {
            is DomainResult.Failure -> {
                _uiState.value =
                    GradeDistributionUiState(
                        isLoading = false,
                        loadError = "Не удалось загрузить распределение",
                    )
            }
            is DomainResult.Success -> {
                val gd = dist.value
                val teams =
                    when (val t = getTeamsForTeamTask(assignmentId)) {
                        is DomainResult.Success -> t.value
                        is DomainResult.Failure -> emptyList()
                    }
                val team = teams.find { it.id == teamId }
                val membersList: List<GradeDistributionMemberRow> =
                    team?.members?.map { m ->
                        GradeDistributionMemberRow(
                            userId = m.userId,
                            displayName = m.credentials,
                        )
                    }
                        ?: gd.entries.map { e ->
                            GradeDistributionMemberRow(
                                userId = e.userId,
                                displayName = e.userId.value.toString(),
                            )
                        }
                val draft: Map<String, String> =
                    gd.entries.associate { e ->
                        e.userId.value.toString() to formatPoints(e.points)
                    }
                val sum = gd.entries.sumOf { it.points }
                val rem = gd.teamRawScore - sum
                _uiState.value =
                    GradeDistributionUiState(
                        isLoading = false,
                        teamRawScore = gd.teamRawScore,
                        members = membersList,
                        draftPoints = draft,
                        remainder = rem,
                        remainderNegative = rem < -1e-9,
                        isCaptain = cap,
                        currentUserVote = gd.currentUserVote,
                        isSaving = false,
                        isVoting = false,
                    )
            }
        }
    }

    fun onDraftChange(userId: UserId, text: String) {
        val idStr = userId.value.toString()
        val st = _uiState.value
        val nextDraft = st.draftPoints + (idStr to text)
        val sum =
            st.members.sumOf { m ->
                parsePoints(nextDraft[m.userId.value.toString()].orEmpty())
            }
        val rem = st.teamRawScore - sum
        _uiState.value =
            st.copy(
                draftPoints = nextDraft,
                remainder = rem,
                remainderNegative = rem < -1e-9,
                saveError = null,
            )
    }

    fun onSave() {
        val st = _uiState.value
        if (!st.isCaptain || st.remainderNegative || st.isSaving) return
        viewModelScope.launch(dispatcher) {
            _uiState.value = st.copy(isSaving = true, saveError = null)
            val entries: List<GradeDistributionEntry> =
                st.members.map { m ->
                    val raw = st.draftPoints[m.userId.value.toString()].orEmpty()
                    GradeDistributionEntry(m.userId, parsePoints(raw))
                }
            when (saveGradeDistribution(teamId, assignmentId, entries)) {
                is DomainResult.Success -> loadInternal()
                is DomainResult.Failure -> {
                    _uiState.value =
                        _uiState.value.copy(
                            isSaving = false,
                            saveError = "Не удалось сохранить",
                        )
                }
            }
        }
    }

    fun onVote(vote: GradeVote) {
        if (_uiState.value.currentUserVote != null || _uiState.value.isVoting) return
        viewModelScope.launch(dispatcher) {
            _uiState.value = _uiState.value.copy(isVoting = true)
            when (voteOnGradeDistribution(teamId, assignmentId, vote)) {
                is DomainResult.Success -> loadInternal()
                is DomainResult.Failure -> Unit
            }
            _uiState.value = _uiState.value.copy(isVoting = false)
        }
    }

    private fun parsePoints(s: String): Double {
        val t = s.trim().replace(',', '.')
        if (t.isEmpty()) return 0.0
        return t.toDoubleOrNull() ?: 0.0
    }

    private fun formatPoints(p: Double): String {
        if (abs(p - p.toLong()) < 1e-9) return p.toLong().toString()
        return String.format(Locale.US, "%.1f", p).trimEnd('0').trimEnd('.')
    }
}
