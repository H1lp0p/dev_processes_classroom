package com.stuf.data.repository

import com.stuf.data.api.GradeDistributionApi
import com.stuf.data.common.httpCodeToDomainError
import com.stuf.data.model.ApiResponseType
import com.stuf.data.model.GradeDistributionEntryDto
import com.stuf.data.model.GradeDistributionUpdateRequestDto
import com.stuf.data.model.GradeDistributionVoteRequestDto
import com.stuf.data.model.GradeVoteType
import com.stuf.data.repository.mappers.toDomain
import com.stuf.domain.common.DomainError
import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.GradeDistribution
import com.stuf.domain.model.GradeDistributionEntry
import com.stuf.domain.model.GradeVote
import com.stuf.domain.model.PostId
import com.stuf.domain.model.TeamId
import com.stuf.domain.repository.GradeDistributionRepository
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

class GradeDistributionRepositoryImpl @Inject constructor(
    private val api: GradeDistributionApi,
) : GradeDistributionRepository {

    override suspend fun getGradeDistribution(
        teamId: TeamId,
        assignmentId: PostId,
    ): DomainResult<GradeDistribution> {
        val response =
            safeCall {
                api.apiTeamsTeamIdAssignmentsAssignmentIdGradeDistributionGet(
                    teamId.value,
                    assignmentId.value,
                )
            }
        return when (response) {
            is DomainResult.Success -> parseDistribution(response.value)
            is DomainResult.Failure -> response
        }
    }

    override suspend fun updateGradeDistribution(
        teamId: TeamId,
        assignmentId: PostId,
        entries: List<GradeDistributionEntry>,
    ): DomainResult<GradeDistribution> {
        val dto =
            GradeDistributionUpdateRequestDto(
                propertyEntries = entries.map { GradeDistributionEntryDto(userId = it.userId.value, points = it.points) },
            )
        val response =
            safeCall {
                api.apiTeamsTeamIdAssignmentsAssignmentIdGradeDistributionPut(
                    teamId.value,
                    assignmentId.value,
                    dto,
                )
            }
        return when (response) {
            is DomainResult.Success -> parseDistribution(response.value)
            is DomainResult.Failure -> response
        }
    }

    override suspend fun voteOnGradeDistribution(
        teamId: TeamId,
        assignmentId: PostId,
        vote: GradeVote,
    ): DomainResult<Unit> {
        val apiVote =
            when (vote) {
                GradeVote.FOR -> GradeVoteType.`for`
                GradeVote.AGAINST -> GradeVoteType.against
            }
        val body = GradeDistributionVoteRequestDto(vote = apiVote)
        return safeCallVoid {
            api.apiTeamsTeamIdAssignmentsAssignmentIdGradeDistributionVotePost(
                teamId.value,
                assignmentId.value,
                body,
            )
        }
    }

    private fun parseDistribution(
        body: com.stuf.data.model.GradeDistributionResponseDtoApiResponse,
    ): DomainResult<GradeDistribution> {
        if (body.type != ApiResponseType.success || body.data == null) {
            return DomainResult.Failure(DomainError.Validation(body.message ?: "Failed to load grade distribution"))
        }
        return DomainResult.Success(body.data.toDomain())
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

    private suspend fun safeCallVoid(block: suspend () -> Response<Unit>): DomainResult<Unit> {
        val response =
            try {
                block()
            } catch (e: IOException) {
                return DomainResult.Failure(DomainError.Network(e))
            } catch (e: Exception) {
                return DomainResult.Failure(DomainError.Unknown(e))
            }

        return if (response.isSuccessful) {
            DomainResult.Success(Unit)
        } else {
            DomainResult.Failure(httpCodeToDomainError(response.code()))
        }
    }
}
