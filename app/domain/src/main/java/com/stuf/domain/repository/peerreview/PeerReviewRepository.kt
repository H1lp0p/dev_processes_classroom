package com.stuf.domain.repository

import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.PeerReviewId
import com.stuf.domain.model.PeerReviewProgress
import com.stuf.domain.model.PeerReviewTarget
import com.stuf.domain.model.PeerReviewTeamTarget
import com.stuf.domain.model.SolutionId
import com.stuf.domain.model.TaskId
import com.stuf.grading.domain.model.SelfAssessmentDraft

interface PeerReviewRepository {
    suspend fun getNextPeerReview(taskId: TaskId): DomainResult<PeerReviewTarget?>

    suspend fun submitPeerReview(
        reviewId: PeerReviewId,
        draft: SelfAssessmentDraft,
    ): DomainResult<PeerReviewProgress>

    suspend fun getIndividualPeerReviewProgress(taskId: TaskId): DomainResult<PeerReviewProgress>

    suspend fun finishIndividualPeerReview(taskId: TaskId): DomainResult<PeerReviewProgress>

    suspend fun getAvailableTeamPeerReviews(taskId: TaskId): DomainResult<List<PeerReviewTeamTarget>>

    suspend fun submitTeamPeerReview(
        teamSolutionId: SolutionId,
        draft: SelfAssessmentDraft,
    ): DomainResult<PeerReviewProgress>

    suspend fun getTeamPeerReviewProgress(taskId: TaskId): DomainResult<PeerReviewProgress>
}
