package com.stuf.domain.model

import com.stuf.grading.domain.model.SelfAssessmentDraft
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
    val selfAssessment: SelfAssessmentDraft? = null,
    val peerReviewProgress: PeerReviewProgress? = null,
)
