package com.stuf.domain.usecase

import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.TaskId
import com.stuf.grading.domain.model.SelfAssessmentDraft

interface SubmitTeamSelfAssessment {
    suspend operator fun invoke(taskId: TaskId, draft: SelfAssessmentDraft): DomainResult<Unit>
}
