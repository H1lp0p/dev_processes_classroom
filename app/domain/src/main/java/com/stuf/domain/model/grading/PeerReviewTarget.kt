package com.stuf.domain.model

import com.stuf.grading.domain.model.CriterionDefinition
import java.time.OffsetDateTime

data class PeerReviewTarget(
    val reviewId: PeerReviewId,
    val taskId: TaskId,
    val solution: AnonymizedSolution,
    val criteria: List<CriterionDefinition>,
    val assignedAt: OffsetDateTime,
)
