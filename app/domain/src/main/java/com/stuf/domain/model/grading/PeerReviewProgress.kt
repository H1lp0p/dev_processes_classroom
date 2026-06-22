package com.stuf.domain.model

data class PeerReviewProgress(
    val required: Int,
    val completed: Int,
    val canFinish: Boolean,
    val isCounted: Boolean,
)
