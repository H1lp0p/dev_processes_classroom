package com.stuf.domain.usecase.impl

import com.stuf.domain.common.DomainError
import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.GradeDistribution
import com.stuf.domain.model.GradeDistributionEntry
import com.stuf.domain.model.PostId
import com.stuf.domain.model.TeamId
import com.stuf.domain.model.GradeVote
import com.stuf.domain.repository.GradeDistributionRepository
import com.stuf.domain.repository.TeamRepository
import com.stuf.domain.usecase.GetGradeDistribution
import com.stuf.domain.usecase.SaveGradeDistribution
import com.stuf.domain.usecase.VoteOnGradeDistribution
import javax.inject.Inject

class GetGradeDistributionUseCase @Inject constructor(
    private val repository: GradeDistributionRepository,
) : GetGradeDistribution {

    override suspend fun invoke(teamId: TeamId, assignmentId: PostId) =
        repository.getGradeDistribution(teamId, assignmentId)
}

class SaveGradeDistributionUseCase @Inject constructor(
    private val gradeDistributionRepository: GradeDistributionRepository,
    private val teamRepository: TeamRepository,
) : SaveGradeDistribution {

    override suspend fun invoke(
        teamId: TeamId,
        assignmentId: PostId,
        entries: List<GradeDistributionEntry>,
    ): DomainResult<GradeDistribution> {
        val current = gradeDistributionRepository.getGradeDistribution(teamId, assignmentId)
        val rraw =
            when (current) {
                is DomainResult.Success -> current.value.teamRawScore
                is DomainResult.Failure -> return current
            }

        if (entries.any { it.points < 0 }) {
            return DomainResult.Failure(DomainError.Validation("Points must be non-negative"))
        }

        val sum = entries.sumOf { it.points }
        if (sum > rraw + EPS) {
            return DomainResult.Failure(
                DomainError.Validation("Sum of distributed points must not exceed team score (Rraw)"),
            )
        }

        val userIds = entries.map { it.userId }
        if (userIds.distinct().size != userIds.size) {
            return DomainResult.Failure(DomainError.Validation("Duplicate entries for the same user"))
        }

        val teams = teamRepository.getTeamsForAssignment(assignmentId)
        val team =
            when (teams) {
                is DomainResult.Success ->
                    teams.value.firstOrNull { it.id == teamId }
                        ?: return DomainResult.Failure(DomainError.Validation("Team not found for this assignment"))
                is DomainResult.Failure -> return teams
            }

        val memberIds = team.members.map { it.userId }.toSet()
        val entryIdSet = entries.map { it.userId }.toSet()
        if (entryIdSet != memberIds) {
            return DomainResult.Failure(
                DomainError.Validation("Distribution must include exactly one entry per team member"),
            )
        }

        return gradeDistributionRepository.updateGradeDistribution(teamId, assignmentId, entries)
    }

    private companion object {
        private const val EPS = 1e-9
    }
}

class VoteOnGradeDistributionUseCase @Inject constructor(
    private val repository: GradeDistributionRepository,
) : VoteOnGradeDistribution {

    override suspend fun invoke(
        teamId: TeamId,
        assignmentId: PostId,
        vote: GradeVote,
    ) = repository.voteOnGradeDistribution(teamId, assignmentId, vote)
}
