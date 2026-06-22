package com.stuf.data.repository

import com.stuf.data.api.PeerReviewApi
import com.stuf.data.common.httpCodeToDomainError
import com.stuf.data.model.ApiResponseType
import com.stuf.data.repository.mappers.toDomain
import com.stuf.data.repository.mappers.toSubmitPeerReviewDto
import com.stuf.domain.common.DomainError
import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.PeerReviewId
import com.stuf.domain.model.PeerReviewProgress
import com.stuf.domain.model.PeerReviewTarget
import com.stuf.domain.model.PeerReviewTeamTarget
import com.stuf.domain.model.SolutionId
import com.stuf.domain.model.TaskId
import com.stuf.domain.repository.PeerReviewRepository
import com.stuf.grading.domain.model.SelfAssessmentDraft
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

class PeerReviewRepositoryImpl @Inject constructor(
    private val api: PeerReviewApi,
) : PeerReviewRepository {

    override suspend fun getNextPeerReview(taskId: TaskId): DomainResult<PeerReviewTarget?> =
        when (val response = safeCall { api.apiTaskTaskIdPeerReviewNextGet(taskId.value) }) {
            is DomainResult.Success -> {
                val body = response.value
                if (body.type != ApiResponseType.success) {
                    DomainResult.Failure(DomainError.Validation(body.message ?: "Failed to load peer review"))
                } else {
                    DomainResult.Success(body.data?.toDomain())
                }
            }
            is DomainResult.Failure -> response
        }

    override suspend fun submitPeerReview(
        reviewId: PeerReviewId,
        draft: SelfAssessmentDraft,
    ): DomainResult<PeerReviewProgress> =
        mapProgressResponse(
            safeCall {
                api.apiPeerReviewReviewIdSubmitPost(
                    reviewId.value,
                    draft.toSubmitPeerReviewDto(),
                )
            },
        )

    override suspend fun getIndividualPeerReviewProgress(taskId: TaskId): DomainResult<PeerReviewProgress> =
        mapProgressResponse(safeCall { api.apiTaskTaskIdPeerReviewProgressGet(taskId.value) })

    override suspend fun finishIndividualPeerReview(taskId: TaskId): DomainResult<PeerReviewProgress> =
        mapProgressResponse(safeCall { api.apiTaskTaskIdPeerReviewFinishPost(taskId.value) })

    override suspend fun getAvailableTeamPeerReviews(taskId: TaskId): DomainResult<List<PeerReviewTeamTarget>> =
        when (val response = safeCall { api.apiTeamTaskTaskIdPeerReviewAvailableGet(taskId.value) }) {
            is DomainResult.Success -> {
                val body = response.value
                if (body.type != ApiResponseType.success || body.data == null) {
                    DomainResult.Failure(DomainError.Validation(body.message ?: "Failed to load team peer reviews"))
                } else {
                    DomainResult.Success(body.data.records.map { it.toDomain() })
                }
            }
            is DomainResult.Failure -> response
        }

    override suspend fun submitTeamPeerReview(
        teamSolutionId: SolutionId,
        draft: SelfAssessmentDraft,
    ): DomainResult<PeerReviewProgress> =
        mapProgressResponse(
            safeCall {
                api.apiTeamSolutionTeamSolutionIdPeerReviewPost(
                    teamSolutionId.value,
                    draft.toSubmitPeerReviewDto(),
                )
            },
        )

    override suspend fun getTeamPeerReviewProgress(taskId: TaskId): DomainResult<PeerReviewProgress> =
        mapProgressResponse(safeCall { api.apiTeamTaskTaskIdPeerReviewProgressGet(taskId.value) })

    private fun mapProgressResponse(
        response: DomainResult<com.stuf.data.model.PeerReviewProgressDtoApiResponse>,
    ): DomainResult<PeerReviewProgress> =
        when (response) {
            is DomainResult.Success -> {
                val body = response.value
                if (body.type != ApiResponseType.success || body.data == null) {
                    DomainResult.Failure(DomainError.Validation(body.message ?: "Peer review request failed"))
                } else {
                    DomainResult.Success(body.data.toDomain())
                }
            }
            is DomainResult.Failure -> response
        }

    private suspend fun <T> safeCall(block: suspend () -> Response<T>): DomainResult<T> {
        val response =
            try {
                block()
            } catch (e: IOException) {
                return DomainResult.Failure(DomainError.Network(e))
            } catch (e: Exception) {
                return DomainResult.Failure(DomainError.Unknown(e))
            }

        if (!response.isSuccessful) {
            return DomainResult.Failure(httpCodeToDomainError(response.code()))
        }

        val body = response.body() ?: return DomainResult.Failure(DomainError.Unknown())
        return DomainResult.Success(body)
    }
}
