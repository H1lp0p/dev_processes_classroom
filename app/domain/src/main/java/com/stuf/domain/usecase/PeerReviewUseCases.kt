package com.stuf.domain.usecase

import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.PeerReviewId
import com.stuf.domain.model.PeerReviewProgress
import com.stuf.domain.model.PeerReviewTarget
import com.stuf.domain.model.PeerReviewTeamTarget
import com.stuf.domain.model.SolutionId
import com.stuf.domain.model.TaskId
import com.stuf.grading.domain.model.SelfAssessmentDraft

interface GetNextPeerReview {
    suspend operator fun invoke(taskId: TaskId): DomainResult<PeerReviewTarget?>
}

interface SubmitPeerReview {
    suspend operator fun invoke(
        reviewId: PeerReviewId,
        draft: SelfAssessmentDraft,
    ): DomainResult<PeerReviewProgress>
}

interface GetIndividualPeerReviewProgress {
    suspend operator fun invoke(taskId: TaskId): DomainResult<PeerReviewProgress>
}

interface FinishIndividualPeerReview {
    suspend operator fun invoke(taskId: TaskId): DomainResult<PeerReviewProgress>
}

interface GetAvailableTeamPeerReviews {
    suspend operator fun invoke(taskId: TaskId): DomainResult<List<PeerReviewTeamTarget>>
}

interface SubmitTeamPeerReview {
    suspend operator fun invoke(
        teamSolutionId: SolutionId,
        draft: SelfAssessmentDraft,
    ): DomainResult<PeerReviewProgress>
}

interface GetTeamPeerReviewProgress {
    suspend operator fun invoke(taskId: TaskId): DomainResult<PeerReviewProgress>
}
