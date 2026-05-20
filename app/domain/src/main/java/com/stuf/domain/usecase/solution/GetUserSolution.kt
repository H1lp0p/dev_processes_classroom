package com.stuf.domain.usecase

import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.Solution
import com.stuf.domain.model.TaskId

interface GetUserSolution {
    suspend operator fun invoke(taskId: TaskId): DomainResult<Solution?>
}
