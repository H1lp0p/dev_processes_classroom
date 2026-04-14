package com.stuf.domain.repository

import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.SolutionId
import com.stuf.domain.model.TaskId
import com.stuf.domain.model.TeamTaskSolution

interface TeamSolutionRepository {
    suspend fun getTeamSolution(taskId: TaskId): DomainResult<TeamTaskSolution?>

    suspend fun submitTeamSolution(taskId: TaskId, text: String?, fileIds: List<String>): DomainResult<SolutionId>

    suspend fun deleteTeamSolution(taskId: TaskId): DomainResult<Unit>
}
