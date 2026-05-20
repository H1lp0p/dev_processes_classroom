package com.stuf.data.repository

import android.util.Log
import com.stuf.data.api.TeamSolutionApi
import com.stuf.data.common.httpCodeToDomainError
import com.stuf.data.model.ApiResponseType
import com.stuf.data.model.SubmitTeamSolutionRequestDto
import com.stuf.data.repository.mappers.toDomain
import com.stuf.data.repository.mappers.toEvaluationDto
import com.stuf.data.repository.mappers.toGradePreviewRequestDto
import com.stuf.data.repository.mappers.toSubmitSelfAssessmentDto
import com.stuf.data.repository.mappers.toTeamTaskSolution
import com.stuf.domain.common.DomainError
import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.GradeBreakdown
import com.stuf.domain.model.SolutionId
import com.stuf.domain.model.TaskId
import com.stuf.domain.model.TeamTaskSolution
import com.stuf.domain.repository.TeamSolutionRepository
import com.stuf.grading.domain.model.SelfAssessmentDraft
import org.json.JSONObject
import retrofit2.Response
import java.io.IOException
import java.util.UUID
import javax.inject.Inject

class TeamSolutionRepositoryImpl @Inject constructor(
    private val api: TeamSolutionApi,
) : TeamSolutionRepository {

    override suspend fun getTeamSolution(taskId: TaskId): DomainResult<TeamTaskSolution?> {
        val response =
            try {
                api.apiTeamTaskTaskIdSolutionGet(taskId.value)
            } catch (e: IOException) {
                return DomainResult.Failure(DomainError.Network(e))
            } catch (e: Exception) {
                return DomainResult.Failure(DomainError.Unknown(e))
            }

        if (response.code() == 404) {
            return DomainResult.Success(null)
        }

        if (!response.isSuccessful) {
            val rawErrorBody: String =
                runCatching { response.errorBody()?.string() }
                    .getOrNull()
                    .orEmpty()
            val parsedServerMessage: String? =
                runCatching {
                    if (rawErrorBody.isBlank()) null else JSONObject(rawErrorBody).optString("message", null)
                }.getOrNull()
            val fallbackMessage: String = response.message().ifBlank { "no response message" }
            val serverMessage: String =
                parsedServerMessage
                    ?.takeIf { it.isNotBlank() }
                    ?: rawErrorBody.takeIf { it.isNotBlank() }
                    ?: fallbackMessage
            Log.e(
                "TeamSolutionRepository",
                "HTTP ${response.code()} while calling TeamSolution API. message=$serverMessage, rawErrorBody=$rawErrorBody",
            )
            return DomainResult.Failure(httpCodeToDomainError(response.code()))
        }

        val body = response.body() ?: return DomainResult.Failure(DomainError.Unknown())
        if (body.type != ApiResponseType.success || body.data == null) {
            return DomainResult.Failure(DomainError.Validation("Failed to load team solution"))
        }

        return DomainResult.Success(body.data.toTeamTaskSolution(taskId))
    }

    override suspend fun submitTeamSolution(
        taskId: TaskId,
        text: String?,
        fileIds: List<String>,
        selfAssessment: SelfAssessmentDraft?,
    ): DomainResult<SolutionId> {
        val dto =
            SubmitTeamSolutionRequestDto(
                text = text,
                files = fileIds.map { UUID.fromString(it) },
                selfAssessment = selfAssessment?.toEvaluationDto(),
            )
        val response =
            safeCall { api.apiTeamTaskTaskIdSolutionPut(taskId.value, dto) }
        return when (response) {
            is DomainResult.Success -> {
                val body = response.value
                if (body.type != ApiResponseType.success || body.data == null) {
                    DomainResult.Failure(DomainError.Validation(body.message ?: "Submit failed"))
                } else {
                    DomainResult.Success(SolutionId(body.data.id))
                }
            }
            is DomainResult.Failure -> response
        }
    }

    override suspend fun deleteTeamSolution(taskId: TaskId): DomainResult<Unit> {
        val response = safeCall { api.apiTeamTaskTaskIdSolutionDelete(taskId.value) }
        return when (response) {
            is DomainResult.Success -> {
                val body = response.value
                if (body.type != ApiResponseType.success) {
                    DomainResult.Failure(DomainError.Validation(body.message ?: "Delete failed"))
                } else {
                    DomainResult.Success(Unit)
                }
            }
            is DomainResult.Failure -> response
        }
    }

    override suspend fun submitSelfAssessment(
        taskId: TaskId,
        draft: SelfAssessmentDraft,
    ): DomainResult<Unit> {
        val response =
            safeCall {
                api.apiTeamTaskTaskIdSelfAssessmentPut(
                    taskId.value,
                    draft.toSubmitSelfAssessmentDto(),
                )
            }
        return mapIdResponse(response, "Не удалось сохранить самооценку")
    }

    override suspend fun deleteSelfAssessment(taskId: TaskId): DomainResult<Unit> {
        val response = safeCall { api.apiTeamTaskTaskIdSelfAssessmentDelete(taskId.value) }
        return mapIdResponse(response, "Не удалось удалить самооценку")
    }

    override suspend fun previewTeamGrade(
        solutionId: SolutionId,
        draft: SelfAssessmentDraft,
    ): DomainResult<GradeBreakdown> {
        val response =
            safeCall {
                api.apiTeamSolutionSolutionIdPreviewPost(
                    solutionId.value,
                    draft.toGradePreviewRequestDto(),
                )
            }
        return when (response) {
            is DomainResult.Success -> {
                val body = response.value
                if (body.type != ApiResponseType.success || body.data == null) {
                    DomainResult.Failure(DomainError.Validation(body.message ?: "Preview failed"))
                } else {
                    DomainResult.Success(body.data.toDomain())
                }
            }
            is DomainResult.Failure -> response
        }
    }

    private fun mapIdResponse(
        response: DomainResult<com.stuf.data.model.IdRequestDtoApiResponse>,
        errorMessage: String,
    ): DomainResult<Unit> =
        when (response) {
            is DomainResult.Success -> {
                val body = response.value
                if (body.type != ApiResponseType.success) {
                    DomainResult.Failure(DomainError.Validation(body.message ?: errorMessage))
                } else {
                    DomainResult.Success(Unit)
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
            val rawErrorBody: String =
                runCatching { response.errorBody()?.string() }
                    .getOrNull()
                    .orEmpty()
            val parsedServerMessage: String? =
                runCatching {
                    if (rawErrorBody.isBlank()) null else JSONObject(rawErrorBody).optString("message", null)
                }.getOrNull()
            val fallbackMessage: String = response.message().ifBlank { "no response message" }
            val serverMessage: String =
                parsedServerMessage
                    ?.takeIf { it.isNotBlank() }
                    ?: rawErrorBody.takeIf { it.isNotBlank() }
                    ?: fallbackMessage
            Log.e(
                "TeamSolutionRepository",
                "HTTP ${response.code()} while calling TeamSolution API. message=$serverMessage, rawErrorBody=$rawErrorBody",
            )
            return DomainResult.Failure(httpCodeToDomainError(response.code()))
        }

        val body = response.body() ?: return DomainResult.Failure(DomainError.Unknown())
        return DomainResult.Success(body)
    }
}
