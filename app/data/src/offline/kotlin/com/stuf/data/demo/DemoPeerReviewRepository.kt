package com.stuf.data.demo

import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.PeerReviewId
import com.stuf.domain.model.PeerReviewProgress
import com.stuf.domain.model.PeerReviewTarget
import com.stuf.domain.model.PeerReviewTeamTarget
import com.stuf.domain.model.SolutionId
import com.stuf.domain.model.TaskId
import com.stuf.domain.repository.PeerReviewRepository
import com.stuf.grading.domain.model.SelfAssessmentDraft
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DemoPeerReviewRepository @Inject constructor(
    private val store: DemoDataStore,
) : PeerReviewRepository {

    override suspend fun getNextPeerReview(taskId: TaskId): DomainResult<PeerReviewTarget?> =
        store.demoGetNextPeerReview(taskId)

    override suspend fun submitPeerReview(
        reviewId: PeerReviewId,
        draft: SelfAssessmentDraft,
    ): DomainResult<PeerReviewProgress> {
        val taskId =
            store.findPeerReviewTaskId(reviewId)
                ?: return DomainResult.Failure(com.stuf.domain.common.DomainError.NotFound)
        return store.demoSubmitPeerReview(reviewId, draft, taskId)
    }

    override suspend fun getIndividualPeerReviewProgress(taskId: TaskId): DomainResult<PeerReviewProgress> =
        store.demoGetIndividualPeerReviewProgress(taskId)

    override suspend fun finishIndividualPeerReview(taskId: TaskId): DomainResult<PeerReviewProgress> =
        store.demoFinishIndividualPeerReview(taskId)

    override suspend fun getAvailableTeamPeerReviews(taskId: TaskId): DomainResult<List<PeerReviewTeamTarget>> =
        store.demoGetAvailableTeamPeerReviews(taskId)

    override suspend fun submitTeamPeerReview(
        teamSolutionId: SolutionId,
        draft: SelfAssessmentDraft,
    ): DomainResult<PeerReviewProgress> {
        val taskId =
            store.findTaskIdBySolutionId(teamSolutionId)
                ?: store.findPeerReviewTeamSolutionTaskId(teamSolutionId)
                ?: return DomainResult.Failure(com.stuf.domain.common.DomainError.NotFound)
        return store.demoSubmitTeamPeerReview(taskId, teamSolutionId, draft)
    }

    override suspend fun getTeamPeerReviewProgress(taskId: TaskId): DomainResult<PeerReviewProgress> =
        store.demoGetTeamPeerReviewProgress(taskId)
}
