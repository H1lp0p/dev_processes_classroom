package com.stuf.domain.usecase

import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.Comment
import com.stuf.domain.model.SolutionId

interface AddSolutionComment {
    suspend operator fun invoke(solutionId: SolutionId, text: String): DomainResult<Comment>
}
