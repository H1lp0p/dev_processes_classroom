package com.stuf.domain.usecase

import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.Solution
import com.stuf.domain.model.SolutionStatus
import com.stuf.domain.model.TaskId
import com.stuf.domain.model.UserId

interface GetSolutionsForTask {
    suspend operator fun invoke(
        taskId: TaskId,
        status: SolutionStatus? = null,
        studentId: UserId? = null,
    ): DomainResult<List<Solution>>
}
