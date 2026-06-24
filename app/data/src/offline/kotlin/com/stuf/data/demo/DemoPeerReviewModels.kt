package com.stuf.data.demo

import com.stuf.domain.model.PeerReviewId
import com.stuf.domain.model.SolutionId
import com.stuf.domain.model.TaskId
import com.stuf.domain.model.TeamId
import com.stuf.domain.model.UserId
import java.time.OffsetDateTime

internal enum class DemoPeerReviewStatus {
    ASSIGNED,
    COMPLETED,
}

internal class DemoPeerReviewRecord(
    val id: PeerReviewId,
    val taskId: TaskId,
    val reviewerId: UserId,
    val solutionId: SolutionId?,
    val solutionAuthorId: UserId?,
    var status: DemoPeerReviewStatus,
    val assignedAt: OffsetDateTime,
    var completedAt: OffsetDateTime?,
    val reviewerTeamId: TeamId?,
    val teamSolutionId: SolutionId?,
)
