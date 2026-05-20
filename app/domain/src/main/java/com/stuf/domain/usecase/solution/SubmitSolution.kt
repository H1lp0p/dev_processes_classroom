package com.stuf.domain.usecase

import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.Solution
import com.stuf.domain.model.TaskId

interface SubmitSolution {
    suspend operator fun invoke(taskId: TaskId, text: String?, fileIds: List<String>): DomainResult<Solution>
}
