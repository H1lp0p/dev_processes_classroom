package com.stuf.domain.usecase

import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.PostId
import com.stuf.grading.domain.model.TaskGradingRubric

interface GetTaskGradingRubric {
    suspend operator fun invoke(postId: PostId): DomainResult<TaskGradingRubric?>
}
