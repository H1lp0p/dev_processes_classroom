package com.stuf.data.demo

import com.stuf.domain.common.DomainError
import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.GradeDistribution
import com.stuf.domain.model.GradeDistributionEntry
import com.stuf.domain.model.GradeVote
import com.stuf.domain.model.PostId
import com.stuf.domain.model.SolutionId
import com.stuf.domain.model.TaskId
import com.stuf.domain.model.Team
import com.stuf.domain.model.TeamId
import com.stuf.domain.model.GradeBreakdown
import com.stuf.domain.model.TeamTaskSolution
import com.stuf.domain.model.UserId
import com.stuf.grading.domain.model.SelfAssessmentDraft
import com.stuf.grading.domain.preview.RubricPreviewEngine
import com.stuf.domain.repository.CurrentUserRepository
import com.stuf.domain.repository.GradeDistributionRepository
import com.stuf.domain.repository.TeamRepository
import com.stuf.domain.repository.TeamSolutionRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DemoTeamRepository @Inject constructor(
    private val store: DemoDataStore,
) : TeamRepository {

    override suspend fun getTeamsForAssignment(assignmentId: PostId): DomainResult<List<Team>> =
        DomainResult.Success(store.getTeamTaskTeams(assignmentId))

    override suspend fun getMyTeam(assignmentId: PostId): DomainResult<Team?> =
        DomainResult.Success(store.getTeamTaskMyTeam(assignmentId))

    override suspend fun joinTeam(teamId: TeamId): DomainResult<Unit> =
        store.demoJoinTeam(teamId)

    override suspend fun leaveTeam(teamId: TeamId): DomainResult<Unit> =
        store.demoLeaveTeam(teamId)

    override suspend fun transferCaptain(teamId: TeamId, toUserId: UserId): DomainResult<Unit> =
        store.demoTransferCaptain(teamId, toUserId)

    override suspend fun voteCaptain(teamId: TeamId, candidateId: UserId): DomainResult<Unit> =
        store.demoVoteCaptain(teamId, candidateId)

    override suspend fun isCaptain(teamId: TeamId): DomainResult<Boolean> =
        DomainResult.Success(store.demoIsCaptain(teamId))
}

@Singleton
class DemoTeamSolutionRepository @Inject constructor(
    private val store: DemoDataStore,
    private val currentUserRepository: CurrentUserRepository,
) : TeamSolutionRepository {

    override suspend fun getTeamSolution(taskId: TaskId): DomainResult<TeamTaskSolution?> =
        DomainResult.Success(store.getTeamTaskSolution(taskId))

    override suspend fun submitTeamSolution(
        taskId: TaskId,
        text: String?,
        fileIds: List<String>,
        selfAssessment: SelfAssessmentDraft?,
    ): DomainResult<SolutionId> =
        store.demoSubmitTeamSolution(taskId, text, fileIds)

    override suspend fun deleteTeamSolution(taskId: TaskId): DomainResult<Unit> =
        DomainResult.Success(Unit)

    override suspend fun submitSelfAssessment(
        taskId: TaskId,
        draft: SelfAssessmentDraft,
    ): DomainResult<Unit> {
        val userId =
            when (val u = currentUserRepository.getCurrentUser()) {
                is DomainResult.Success -> u.value.id
                is DomainResult.Failure ->
                    return DomainResult.Failure(DomainError.Validation("Не удалось определить пользователя"))
            }
        return store.demoSubmitSelfAssessment(taskId, userId, draft)
    }

    override suspend fun deleteSelfAssessment(taskId: TaskId): DomainResult<Unit> {
        val userId =
            when (val u = currentUserRepository.getCurrentUser()) {
                is DomainResult.Success -> u.value.id
                is DomainResult.Failure ->
                    return DomainResult.Failure(DomainError.Validation("Не удалось определить пользователя"))
            }
        return store.demoDeleteSelfAssessment(taskId, userId)
    }

    override suspend fun previewTeamGrade(
        solutionId: SolutionId,
        draft: SelfAssessmentDraft,
    ): DomainResult<GradeBreakdown> {
        val taskId =
            store.findTaskIdBySolutionId(solutionId)
                ?: return DomainResult.Failure(DomainError.NotFound)
        val rubric =
            store.findGradingRubric(taskId)
                ?: return DomainResult.Failure(DomainError.Validation("Рубрика не найдена"))
        val preview = RubricPreviewEngine.preview(rubric, draft)
        val weightedSum = preview.weightedContributions.sumOf { it.contribution }
        return DomainResult.Success(
            GradeBreakdown(
                baseTeacherScore = weightedSum,
                baseStudentScore = weightedSum,
                baseScore = preview.scoreAfterModifiers,
                afterQualityCoefficient = preview.scoreAfterModifiers,
                latePenalty = 0.0,
                afterLatePenalty = preview.scoreAfterModifiers,
                afterBlocking = preview.scoreAfterBlocking,
                finalScore = preview.finalScore,
                expiredDays = 0,
                thresholdApplied = preview.zeroedByFailThreshold || preview.boostedToMaxBySuccessThreshold,
                thresholdReason = null,
            ),
        )
    }
}

@Singleton
class DemoGradeDistributionRepository @Inject constructor(
    private val store: DemoDataStore,
    private val currentUserRepository: CurrentUserRepository,
) : GradeDistributionRepository {

    override suspend fun getGradeDistribution(
        teamId: TeamId,
        assignmentId: PostId,
    ): DomainResult<GradeDistribution> {
        val uid =
            when (val u = currentUserRepository.getCurrentUser()) {
                is DomainResult.Success -> u.value.id
                is DomainResult.Failure -> null
            }
        return store.gradeDistributionGet(teamId, assignmentId, uid)
    }

    override suspend fun updateGradeDistribution(
        teamId: TeamId,
        assignmentId: PostId,
        entries: List<GradeDistributionEntry>,
    ): DomainResult<GradeDistribution> =
        store.gradeDistributionUpdate(teamId, assignmentId, entries)

    override suspend fun voteOnGradeDistribution(
        teamId: TeamId,
        assignmentId: PostId,
        vote: GradeVote,
    ): DomainResult<Unit> {
        val voterId =
            when (val u = currentUserRepository.getCurrentUser()) {
                is DomainResult.Success -> u.value.id
                is DomainResult.Failure ->
                    return DomainResult.Failure(DomainError.Validation("Не удалось определить пользователя"))
            }
        return store.gradeDistributionVote(teamId, assignmentId, vote, voterId)
    }
}
