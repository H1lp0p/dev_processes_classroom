package com.stuf.domain.model

import java.time.OffsetDateTime

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
