package com.stuf.domain.repository

import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.Review
import com.stuf.domain.model.Solution
import com.stuf.domain.model.SolutionId
import com.stuf.domain.model.SolutionStatus
import com.stuf.domain.model.TaskId
import com.stuf.domain.model.UserId

interface SolutionRepository {
    suspend fun submitSolution(taskId: TaskId, text: String?, fileIds: List<String>): DomainResult<Solution>
    suspend fun cancelSolution(taskId: TaskId): DomainResult<Unit>
    suspend fun getUserSolution(taskId: TaskId): DomainResult<Solution?>
    suspend fun getTaskSolutions(
        taskId: TaskId,
        status: SolutionStatus? = null,
        studentId: UserId? = null,
    ): DomainResult<List<Solution>>

    suspend fun reviewSolution(solutionId: SolutionId, review: Review): DomainResult<Unit>
}
