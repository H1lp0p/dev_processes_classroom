package com.stuf.domain.usecase

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
import com.stuf.domain.model.TeamMember
import com.stuf.domain.model.TeamMemberRole
import com.stuf.domain.model.UserId
import com.stuf.domain.repository.GradeDistributionRepository
import com.stuf.domain.repository.TeamRepository
import com.stuf.domain.repository.TeamSolutionRepository
import com.stuf.domain.usecase.impl.SaveGradeDistributionUseCase
import com.stuf.domain.usecase.impl.SubmitTeamTaskSolutionUseCase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class TeamTaskUseCasesValidationTest {

    private val assignmentId = PostId(UUID.randomUUID())
    private val teamId = TeamId(UUID.randomUUID())
    private val userA = UserId(UUID.randomUUID())
    private val userB = UserId(UUID.randomUUID())

    private val sampleTeam =
        Team(
            id = teamId,
            name = "A",
            members =
                listOf(
                    TeamMember(userA, "a", TeamMemberRole.LEADER),
                    TeamMember(userB, "b", TeamMemberRole.MEMBER),
                ),
        )

    @Test
    fun `save distribution fails when sum exceeds Rraw`() {
        val gradeRepo =
            object : GradeDistributionRepository {
                override suspend fun getGradeDistribution(
                    teamId: TeamId,
                    assignmentId: PostId,
                ): DomainResult<GradeDistribution> =
                    DomainResult.Success(
                        GradeDistribution(
                            teamId = teamId,
                            assignmentId = assignmentId,
                            teamRawScore = 10.0,
                            entries = emptyList(),
                            sumDistributed = 0.0,
                            distributionChanged = false,
                        ),
                    )

                override suspend fun updateGradeDistribution(
                    teamId: TeamId,
                    assignmentId: PostId,
                    entries: List<GradeDistributionEntry>,
                ): DomainResult<GradeDistribution> =
                    error("should not be called")

                override suspend fun voteOnGradeDistribution(
                    teamId: TeamId,
                    assignmentId: PostId,
                    vote: GradeVote,
                ): DomainResult<Unit> = error("unused")
            }

        val teamRepo =
            object : TeamRepository {
                override suspend fun getTeamsForAssignment(assignmentId: PostId) =
                    DomainResult.Success(listOf(sampleTeam))

                override suspend fun getMyTeam(assignmentId: PostId) = error("unused")
                override suspend fun joinTeam(teamId: TeamId) = error("unused")
                override suspend fun leaveTeam(teamId: TeamId) = error("unused")
                override suspend fun transferCaptain(teamId: TeamId, toUserId: UserId) = error("unused")
                override suspend fun isCaptain(teamId: TeamId) = error("unused")
            }

        val useCase: SaveGradeDistribution = SaveGradeDistributionUseCase(gradeRepo, teamRepo)
        val entries =
            listOf(
                GradeDistributionEntry(userA, 6.0),
                GradeDistributionEntry(userB, 5.0),
            )

        val result = runBlocking { useCase(teamId, assignmentId, entries) }

        assertTrue(result is DomainResult.Failure)
        assertTrue((result as DomainResult.Failure).error is DomainError.Validation)
    }

    @Test
    fun `submit team solution fails when not captain`() {
        val teamRepo =
            object : TeamRepository {
                override suspend fun getTeamsForAssignment(assignmentId: PostId) = error("unused")
                override suspend fun getMyTeam(assignmentId: PostId) = error("unused")
                override suspend fun joinTeam(teamId: TeamId) = error("unused")
                override suspend fun leaveTeam(teamId: TeamId) = error("unused")
                override suspend fun transferCaptain(teamId: TeamId, toUserId: UserId) = error("unused")
                override suspend fun isCaptain(teamId: TeamId) = DomainResult.Success(false)
            }

        val solRepo =
            object : TeamSolutionRepository {
                override suspend fun getTeamSolution(taskId: TaskId) = error("unused")
                override suspend fun submitTeamSolution(
                    taskId: TaskId,
                    text: String?,
                    fileIds: List<String>,
                ) = error("unused")

                override suspend fun deleteTeamSolution(taskId: TaskId) = error("unused")
            }

        val useCase: SubmitTeamTaskSolution = SubmitTeamTaskSolutionUseCase(solRepo, teamRepo)
        val result =
            runBlocking {
                useCase(TaskId(UUID.randomUUID()), teamId, "text", emptyList())
            }

        assertTrue(result is DomainResult.Failure)
    }

    @Test
    fun `submit team solution fails when text and files empty`() {
        val teamRepo =
            object : TeamRepository {
                override suspend fun getTeamsForAssignment(assignmentId: PostId) = error("unused")
                override suspend fun getMyTeam(assignmentId: PostId) = error("unused")
                override suspend fun joinTeam(teamId: TeamId) = error("unused")
                override suspend fun leaveTeam(teamId: TeamId) = error("unused")
                override suspend fun transferCaptain(teamId: TeamId, toUserId: UserId) = error("unused")
                override suspend fun isCaptain(teamId: TeamId) = DomainResult.Success(true)
            }

        val solRepo =
            object : TeamSolutionRepository {
                override suspend fun getTeamSolution(taskId: TaskId) = error("unused")
                override suspend fun submitTeamSolution(
                    taskId: TaskId,
                    text: String?,
                    fileIds: List<String>,
                ) = error("unused")

                override suspend fun deleteTeamSolution(taskId: TaskId) = error("unused")
            }

        val useCase: SubmitTeamTaskSolution = SubmitTeamTaskSolutionUseCase(solRepo, teamRepo)
        val result =
            runBlocking {
                useCase(TaskId(UUID.randomUUID()), teamId, "   ", emptyList())
            }

        assertTrue(result is DomainResult.Failure)
    }
}
