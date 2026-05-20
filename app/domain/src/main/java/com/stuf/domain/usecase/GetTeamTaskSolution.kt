package com.stuf.domain.usecase

import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.TaskId
import com.stuf.domain.model.TeamTaskSolution

interface GetTeamTaskSolution {
    suspend operator fun invoke(taskId: TaskId): DomainResult<TeamTaskSolution?>
}
