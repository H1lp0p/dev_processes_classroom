package com.stuf.domain.model

import java.time.OffsetDateTime

@JvmInline
value class Score(val value: Int) {
    init {
        require(value >= 0) { "Score must be non-negative" }
    }
}

enum class SolutionStatus {
    PENDING,
    CHECKED,
    RETURNED,
}

data class Solution(
    val id: SolutionId,
    val taskId: TaskId,
    val authorId: UserId,
    val text: String?,
    val files: List<FileInfo>,
    val score: Score?,
    val status: SolutionStatus,
    val updatedAt: OffsetDateTime,
)

data class Review(
    val score: Score,
    val status: SolutionStatus,
    val comment: String?,
)

data class FileInfo(
    val id: String,
    val name: String,
)

