package com.stuf.domain.model

import java.time.OffsetDateTime

data class PeerReviewTeamTarget(
    val teamSolutionId: SolutionId,
    val teamName: String,
    val submittedAt: OffsetDateTime,
    val alreadyReviewed: Boolean,
)
