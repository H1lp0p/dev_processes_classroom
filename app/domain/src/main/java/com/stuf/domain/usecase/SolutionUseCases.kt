package com.stuf.domain.usecase

import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.Review
import com.stuf.domain.model.Solution
import com.stuf.domain.model.SolutionId
import com.stuf.domain.model.SolutionStatus
import com.stuf.domain.model.TaskId
import com.stuf.domain.model.UserId

interface SubmitSolution {
    suspend operator fun invoke(taskId: TaskId, text: String?, fileIds: List<String>): DomainResult<Solution>
}

interface UpdateSolution {
    suspend operator fun invoke(taskId: TaskId, text: String?, fileIds: List<String>): DomainResult<Solution>
}

interface CancelSolution {
    suspend operator fun invoke(taskId: TaskId): DomainResult<Unit>
}

interface GetSolutionsForTask {
    suspend operator fun invoke(
        taskId: TaskId,
        status: SolutionStatus? = null,
        studentId: UserId? = null,
    ): DomainResult<List<Solution>>
}

interface ReviewSolution {
    suspend operator fun invoke(solutionId: SolutionId, review: Review): DomainResult<Unit>
}

interface GetUserSolution {
    suspend operator fun invoke(taskId: TaskId): DomainResult<Solution?>
}
