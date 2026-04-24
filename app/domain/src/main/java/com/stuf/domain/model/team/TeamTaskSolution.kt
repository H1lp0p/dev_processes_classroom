package com.stuf.domain.model

import java.time.OffsetDateTime

data class TeamTaskSolution(
    val id: SolutionId?,
    val taskId: TaskId,
    val text: String?,
    val files: List<FileInfo>,
    val score: Score?,
    val status: SolutionStatus,
    val updatedAt: OffsetDateTime,
    val team: Team,
    val submittedBy: UserRef,
)
