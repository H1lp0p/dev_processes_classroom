package com.stuf.domain.usecase.impl

import com.stuf.domain.common.DomainError
import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.PostId
import com.stuf.domain.model.SolutionId
import com.stuf.domain.model.TaskId
import com.stuf.domain.model.TeamId
import com.stuf.domain.model.UserId
import com.stuf.domain.repository.TeamRepository
import com.stuf.domain.repository.TeamSolutionRepository
import com.stuf.domain.usecase.CancelTeamTaskSolution
import com.stuf.domain.usecase.CheckTeamCaptain
import com.stuf.domain.usecase.GetMyTeamForTeamTask
import com.stuf.domain.usecase.GetTeamTaskSolution
import com.stuf.domain.usecase.GetTeamsForTeamTask
import com.stuf.domain.usecase.JoinTeam
import com.stuf.domain.usecase.LeaveTeam
import com.stuf.domain.usecase.SubmitTeamTaskSolution
import com.stuf.domain.usecase.TransferTeamCaptain
import javax.inject.Inject

class GetTeamsForTeamTaskUseCase @Inject constructor(
    private val repository: TeamRepository,
) : GetTeamsForTeamTask {

    override suspend fun invoke(assignmentId: PostId) = repository.getTeamsForAssignment(assignmentId)
}

class GetMyTeamForTeamTaskUseCase @Inject constructor(
    private val repository: TeamRepository,
) : GetMyTeamForTeamTask {

    override suspend fun invoke(assignmentId: PostId) = repository.getMyTeam(assignmentId)
}

class JoinTeamUseCase @Inject constructor(
    private val repository: TeamRepository,
) : JoinTeam {

    override suspend fun invoke(teamId: TeamId) = repository.joinTeam(teamId)
}

class LeaveTeamUseCase @Inject constructor(
    private val repository: TeamRepository,
) : LeaveTeam {

    override suspend fun invoke(teamId: TeamId) = repository.leaveTeam(teamId)
}

class TransferTeamCaptainUseCase @Inject constructor(
    private val repository: TeamRepository,
) : TransferTeamCaptain {

    override suspend fun invoke(teamId: TeamId, toUserId: UserId) =
        repository.transferCaptain(teamId, toUserId)
}

class CheckTeamCaptainUseCase @Inject constructor(
    private val repository: TeamRepository,
) : CheckTeamCaptain {

    override suspend fun invoke(teamId: TeamId) = repository.isCaptain(teamId)
}

class GetTeamTaskSolutionUseCase @Inject constructor(
    private val repository: TeamSolutionRepository,
) : GetTeamTaskSolution {

    override suspend fun invoke(taskId: TaskId) = repository.getTeamSolution(taskId)
}

class SubmitTeamTaskSolutionUseCase @Inject constructor(
    private val teamSolutionRepository: TeamSolutionRepository,
    private val teamRepository: TeamRepository,
) : SubmitTeamTaskSolution {

    override suspend fun invoke(
        taskId: TaskId,
        captainTeamId: TeamId,
        text: String?,
        fileIds: List<String>,
    ): DomainResult<SolutionId> {
        val isCaptainResult = teamRepository.isCaptain(captainTeamId)
        val isCaptain =
            when (isCaptainResult) {
                is DomainResult.Success -> isCaptainResult.value
                is DomainResult.Failure -> return isCaptainResult
            }
        if (!isCaptain) {
            return DomainResult.Failure(DomainError.Validation("Only team captain can submit the solution"))
        }

        val isTextBlank = text?.isBlank() != false
        val hasFiles = fileIds.isNotEmpty()
        if (isTextBlank && !hasFiles) {
            return DomainResult.Failure(DomainError.Validation("Solution must have text or files"))
        }

        return teamSolutionRepository.submitTeamSolution(
            taskId = taskId,
            text = if (isTextBlank) null else text,
            fileIds = fileIds,
        )
    }
}

class CancelTeamTaskSolutionUseCase @Inject constructor(
    private val repository: TeamSolutionRepository,
) : CancelTeamTaskSolution {

    override suspend fun invoke(taskId: TaskId) = repository.deleteTeamSolution(taskId)
}
