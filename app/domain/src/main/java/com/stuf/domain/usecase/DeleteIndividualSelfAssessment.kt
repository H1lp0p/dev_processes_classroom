package com.stuf.domain.usecase

import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.TaskId

interface DeleteIndividualSelfAssessment {
    suspend operator fun invoke(taskId: TaskId): DomainResult<Unit>
}
