package com.stuf.domain.usecase.impl

import com.stuf.domain.model.PeerReviewId
import com.stuf.domain.model.SolutionId
import com.stuf.domain.model.TaskId
import com.stuf.domain.repository.PeerReviewRepository
import com.stuf.domain.usecase.FinishIndividualPeerReview
import com.stuf.domain.usecase.GetAvailableTeamPeerReviews
import com.stuf.domain.usecase.GetIndividualPeerReviewProgress
import com.stuf.domain.usecase.GetNextPeerReview
import com.stuf.domain.usecase.GetTeamPeerReviewProgress
import com.stuf.domain.usecase.SubmitPeerReview
import com.stuf.domain.usecase.SubmitTeamPeerReview
import com.stuf.grading.domain.model.SelfAssessmentDraft
import javax.inject.Inject

class GetNextPeerReviewUseCase @Inject constructor(
    private val repository: PeerReviewRepository,
) : GetNextPeerReview {
    override suspend fun invoke(taskId: TaskId) = repository.getNextPeerReview(taskId)
}

class SubmitPeerReviewUseCase @Inject constructor(
    private val repository: PeerReviewRepository,
) : SubmitPeerReview {
    override suspend fun invoke(reviewId: PeerReviewId, draft: SelfAssessmentDraft) =
        repository.submitPeerReview(reviewId, draft)
}

class GetIndividualPeerReviewProgressUseCase @Inject constructor(
    private val repository: PeerReviewRepository,
) : GetIndividualPeerReviewProgress {
    override suspend fun invoke(taskId: TaskId) = repository.getIndividualPeerReviewProgress(taskId)
}

class FinishIndividualPeerReviewUseCase @Inject constructor(
    private val repository: PeerReviewRepository,
) : FinishIndividualPeerReview {
    override suspend fun invoke(taskId: TaskId) = repository.finishIndividualPeerReview(taskId)
}

class GetAvailableTeamPeerReviewsUseCase @Inject constructor(
    private val repository: PeerReviewRepository,
) : GetAvailableTeamPeerReviews {
    override suspend fun invoke(taskId: TaskId) = repository.getAvailableTeamPeerReviews(taskId)
}

class SubmitTeamPeerReviewUseCase @Inject constructor(
    private val repository: PeerReviewRepository,
) : SubmitTeamPeerReview {
    override suspend fun invoke(teamSolutionId: SolutionId, draft: SelfAssessmentDraft) =
        repository.submitTeamPeerReview(teamSolutionId, draft)
}

class GetTeamPeerReviewProgressUseCase @Inject constructor(
    private val repository: PeerReviewRepository,
) : GetTeamPeerReviewProgress {
    override suspend fun invoke(taskId: TaskId) = repository.getTeamPeerReviewProgress(taskId)
}
