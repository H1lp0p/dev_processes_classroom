package com.stuf.domain.usecase

import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.Review
import com.stuf.domain.model.SolutionId

interface ReviewSolution {
    suspend operator fun invoke(solutionId: SolutionId, review: Review): DomainResult<Unit>
}
