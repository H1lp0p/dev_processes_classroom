package com.stuf.domain.usecase

import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.SolutionId
import com.stuf.domain.model.TaskId
import com.stuf.domain.model.TeamId

interface SubmitTeamTaskSolution {
    suspend operator fun invoke(
        taskId: TaskId,
        captainTeamId: TeamId,
        text: String?,
        fileIds: List<String>,
    ): DomainResult<SolutionId>
}
