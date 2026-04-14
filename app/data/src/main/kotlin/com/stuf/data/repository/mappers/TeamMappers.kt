package com.stuf.data.repository.mappers

import com.stuf.data.model.SolutionStatus as ApiSolutionStatus
import com.stuf.data.model.StudentTeamSolutionDetailsDto
import com.stuf.data.model.TeamDto
import com.stuf.data.model.TeamMemberDto
import com.stuf.data.model.TeamMemberRole as ApiTeamMemberRole
import com.stuf.data.model.GradeDistributionEntryDto
import com.stuf.data.model.GradeDistributionResponseDto
import com.stuf.domain.model.FileInfo
import com.stuf.domain.model.GradeDistribution
import com.stuf.domain.model.GradeDistributionEntry
import com.stuf.domain.model.PostId
import com.stuf.domain.model.Score
import com.stuf.domain.model.SolutionId
import com.stuf.domain.model.SolutionStatus
import com.stuf.domain.model.TaskId
import com.stuf.domain.model.Team
import com.stuf.domain.model.TeamId
import com.stuf.domain.model.TeamMember
import com.stuf.domain.model.TeamMemberRole
import com.stuf.domain.model.TeamTaskSolution
import com.stuf.domain.model.UserId
import com.stuf.domain.model.UserRef

internal fun TeamDto.toDomain(): Team =
    Team(
        id = TeamId(id),
        name = name,
        members = members.map { it.toDomain() },
    )

private fun TeamMemberDto.toDomain(): TeamMember =
    TeamMember(
        userId = UserId(userId),
        credentials = credentials,
        role =
            when (role) {
                ApiTeamMemberRole.member -> TeamMemberRole.MEMBER
                ApiTeamMemberRole.leader -> TeamMemberRole.LEADER
            },
    )

internal fun StudentTeamSolutionDetailsDto.toTeamTaskSolution(taskId: TaskId): TeamTaskSolution {
    val filesDomain =
        files?.map { f ->
            FileInfo(id = f.id ?: "", name = f.name ?: "")
        }.orEmpty()
    val scoreDomain = score?.let { Score(it) }
    return TeamTaskSolution(
        id = id?.let(::SolutionId),
        taskId = taskId,
        text = text,
        files = filesDomain,
        score = scoreDomain,
        status = status.toDomain(),
        updatedAt = updatedDate,
        team = team.toDomain(),
        submittedBy =
            UserRef(
                id = UserId(submittedBy.id),
                credentials = submittedBy.credentials,
            ),
    )
}

private fun ApiSolutionStatus.toDomain(): SolutionStatus =
    when (this) {
        ApiSolutionStatus.pending -> SolutionStatus.PENDING
        ApiSolutionStatus.checked -> SolutionStatus.CHECKED
        ApiSolutionStatus.returned -> SolutionStatus.RETURNED
    }

internal fun GradeDistributionResponseDto.toDomain(): GradeDistribution =
    GradeDistribution(
        teamId = TeamId(teamId),
        assignmentId = PostId(assignmentId),
        teamRawScore = teamRawScore,
        entries = propertyEntries.map { it.toDomain() },
        sumDistributed = sumDistributed,
        distributionChanged = distributionChanged,
        currentUserVote = null,
    )

private fun GradeDistributionEntryDto.toDomain(): GradeDistributionEntry =
    GradeDistributionEntry(
        userId = UserId(userId),
        points = points,
    )
