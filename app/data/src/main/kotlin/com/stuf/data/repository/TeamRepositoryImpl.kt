package com.stuf.data.repository

import com.stuf.data.api.TeamApi
import com.stuf.data.common.httpCodeToDomainError
import com.stuf.data.model.ApiResponseType
import com.stuf.data.model.ObjectApiResponse
import com.stuf.data.repository.mappers.toDomain
import com.stuf.domain.common.DomainError
import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.PostId
import com.stuf.domain.model.Team
import com.stuf.domain.model.TeamId
import com.stuf.domain.model.UserId
import com.stuf.domain.repository.TeamRepository
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

class TeamRepositoryImpl @Inject constructor(
    private val api: TeamApi,
) : TeamRepository {

    override suspend fun getTeamsForAssignment(assignmentId: PostId): DomainResult<List<Team>> {
        val response = safeCall { api.apiTeamTaskAssignmentIdTeamsGet(assignmentId.value) }
        return when (response) {
            is DomainResult.Success -> parseTeamList(response.value)
            is DomainResult.Failure -> response
        }
    }

    override suspend fun getMyTeam(assignmentId: PostId): DomainResult<Team?> {
        val response =
            try {
                api.apiTeamTaskAssignmentIdMyTeamGet(assignmentId.value)
            } catch (e: IOException) {
                return DomainResult.Failure(DomainError.Network(e))
            } catch (e: Exception) {
                return DomainResult.Failure(DomainError.Unknown(e))
            }

        if (response.code() == 404) {
            return DomainResult.Success(null)
        }

        if (!response.isSuccessful) {
            return DomainResult.Failure(httpCodeToDomainError(response.code()))
        }

        val body = response.body() ?: return DomainResult.Failure(DomainError.Unknown())
        if (body.type != ApiResponseType.success || body.data == null) {
            return DomainResult.Failure(DomainError.Validation("Failed to load my team"))
        }

        return DomainResult.Success(body.data.toDomain())
    }

    override suspend fun joinTeam(teamId: TeamId): DomainResult<Unit> {
        val response = safeCall { api.apiTeamsTeamIdJoinPost(teamId.value) }
        return when (response) {
            is DomainResult.Success -> parseObjectResponse(response.value)
            is DomainResult.Failure -> response
        }
    }

    override suspend fun leaveTeam(teamId: TeamId): DomainResult<Unit> {
        val response = safeCall { api.apiTeamsTeamIdLeavePost(teamId.value) }
        return when (response) {
            is DomainResult.Success -> parseObjectResponse(response.value)
            is DomainResult.Failure -> response
        }
    }

    override suspend fun transferCaptain(teamId: TeamId, toUserId: UserId): DomainResult<Unit> {
        val response =
            safeCall {
                api.apiTeamsTeamIdTransferCaptainToUserIdPost(teamId.value, toUserId.value)
            }
        return when (response) {
            is DomainResult.Success -> parseObjectResponse(response.value)
            is DomainResult.Failure -> response
        }
    }

    override suspend fun voteCaptain(teamId: TeamId, candidateId: UserId): DomainResult<Unit> {
        val response =
            safeCall {
                api.apiTeamsTeamIdVoteCandidateIdPost(teamId.value, candidateId.value)
            }
        return when (response) {
            is DomainResult.Success -> parseObjectResponse(response.value)
            is DomainResult.Failure -> response
        }
    }

    override suspend fun isCaptain(teamId: TeamId): DomainResult<Boolean> {
        val response = safeCall { api.apiTeamsTeamIdIsCaptainGet(teamId.value) }
        return when (response) {
            is DomainResult.Success -> {
                val body = response.value
                if (body.type != ApiResponseType.success || body.data == null) {
                    DomainResult.Failure(DomainError.Validation("Failed to read captain flag"))
                } else {
                    DomainResult.Success(body.data)
                }
            }
            is DomainResult.Failure -> response
        }
    }

    private fun parseTeamList(body: com.stuf.data.model.TeamDtoListApiResponse): DomainResult<List<Team>> {
        if (body.type != ApiResponseType.success || body.data == null) {
            return DomainResult.Failure(DomainError.Validation("Failed to load teams"))
        }
        return DomainResult.Success(body.data.map { it.toDomain() })
    }

    private fun parseObjectResponse(body: ObjectApiResponse): DomainResult<Unit> {
        return if (body.type == ApiResponseType.success) {
            DomainResult.Success(Unit)
        } else {
            DomainResult.Failure(DomainError.Validation(body.message ?: "Request failed"))
        }
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
