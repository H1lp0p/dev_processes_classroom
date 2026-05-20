package com.stuf.domain.model

data class Review(
    val score: Score,
    val status: SolutionStatus,
    val comment: String?,
)
