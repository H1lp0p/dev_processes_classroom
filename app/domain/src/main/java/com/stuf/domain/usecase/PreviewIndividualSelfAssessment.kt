package com.stuf.domain.usecase

import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.GradeBreakdown
import com.stuf.domain.model.SolutionId
import com.stuf.grading.domain.model.SelfAssessmentDraft

interface PreviewIndividualSelfAssessment {
    suspend operator fun invoke(solutionId: SolutionId, draft: SelfAssessmentDraft): DomainResult<GradeBreakdown>
}
