package com.stuf.data.repository

import com.stuf.data.api.SolutionApi
import com.stuf.data.common.httpCodeToDomainError
import com.stuf.data.model.ApiResponseType
import com.stuf.data.model.FileDto
import com.stuf.data.model.SolutionListItemDto
import com.stuf.data.model.SolutionStatus as ApiSolutionStatus
import com.stuf.data.model.StudentSolutionDetailsDto
import com.stuf.data.model.SubmitSolutionRequestDto
import com.stuf.data.model.UpdateSolutionRequestDto
import com.stuf.domain.common.DomainError
import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.FileInfo
import com.stuf.domain.model.Review
import com.stuf.domain.model.Score
import com.stuf.domain.model.Solution
import com.stuf.domain.model.SolutionId
import com.stuf.domain.model.SolutionStatus
import com.stuf.domain.model.TaskId
import com.stuf.domain.model.UserId
import com.stuf.domain.repository.SolutionRepository
import retrofit2.Response
import java.io.IOException
import java.time.OffsetDateTime
import java.util.UUID

class SolutionRepositoryImpl(
    private val api: SolutionApi,
) : SolutionRepository {

    override suspend fun submitSolution(
        taskId: TaskId,
        text: String?,
        fileIds: List<String>,
    ): DomainResult<Solution> {
        val dto = SubmitSolutionRequestDto(
            text = text,
            files = fileIds.map(UUID::fromString),
        )
        val response = safeCall { api.apiTaskIdSolutionPut(taskId.value, dto) }
        return when (response) {
            is DomainResult.Success -> {
                val data = response.value.data
                    ?: return DomainResult.Failure(DomainError.Unknown())
                val id = data.id
                DomainResult.Success(
                    Solution(
                        id = SolutionId(id),
                        taskId = taskId,
                        authorId = UserId(UUID.randomUUID()),
                        text = text,
                        files = emptyList(),
                        score = null,
                        status = SolutionStatus.PENDING,
                        updatedAt = OffsetDateTime.now(),
                    ),
                )
            }
            is DomainResult.Failure -> response
        }
    }

    override suspend fun cancelSolution(taskId: TaskId): DomainResult<Unit> {
        val response = safeCall { api.apiTaskIdSolutionDelete(taskId.value) }
        return when (response) {
            is DomainResult.Success -> DomainResult.Success(Unit)
            is DomainResult.Failure -> response
        }
    }

    override suspend fun getUserSolution(taskId: TaskId): DomainResult<Solution?> {
        val response = try {
            api.apiTaskIdSolutionGet(taskId.value)
        } catch (e: IOException) {
            return DomainResult.Failure(DomainError.Network(e))
        } catch (e: Exception) {
            return DomainResult.Failure(DomainError.Unknown(e))
        }

        if (!response.isSuccessful) {
            // 404 трактуем как отсутствие решения
            return if (response.code() == 404) {
                DomainResult.Success(null)
            } else {
                DomainResult.Failure(httpCodeToDomainError(response.code()))
            }
        }

        val body = response.body()
            ?: return DomainResult.Failure(DomainError.Unknown())

        if (body.type != ApiResponseType.success || body.data == null) {
            return DomainResult.Failure(DomainError.Unknown())
        }

        return DomainResult.Success(body.data.toDomain(taskId))
    }

    override suspend fun getTaskSolutions(
        taskId: TaskId,
        status: SolutionStatus?,
        studentId: UserId?,
    ): DomainResult<List<Solution>> {
        val apiStatus = status?.toApi()
        val studentUuid = studentId?.value

        val response = safeCall {
            api.apiTaskIdSolutionsGet(
                id = taskId.value,
                skip = 0,
                take = 20,
                status = apiStatus,
                studentId = studentUuid,
            )
        }
        return when (response) {
            is DomainResult.Success -> {
                val records = response.value.data?.records.orEmpty()
                DomainResult.Success(records.map { it.toDomain(taskId) })
            }
            is DomainResult.Failure -> response
        }
    }

    override suspend fun reviewSolution(solutionId: SolutionId, review: Review): DomainResult<Unit> {
        val dto = UpdateSolutionRequestDto(
            score = review.score.value,
            status = review.status.toApi(),
            comment = review.comment,
        )
        val response = safeCall { api.apiSolutionSolutionIdReviewPost(solutionId.value, dto) }
        return when (response) {
            is DomainResult.Success -> DomainResult.Success(Unit)
            is DomainResult.Failure -> response
        }
    }

    private suspend fun <T> safeCall(block: suspend () -> Response<T>): DomainResult<T> {
        val response = try {
            block()
        } catch (e: IOException) {
            return DomainResult.Failure(DomainError.Network(e))
        } catch (e: Exception) {
            return DomainResult.Failure(DomainError.Unknown(e))
        }

        if (!response.isSuccessful) {
            return DomainResult.Failure(httpCodeToDomainError(response.code()))
        }

        val body = response.body()
            ?: return DomainResult.Failure(DomainError.Unknown())

        return DomainResult.Success(body)
    }

    private fun StudentSolutionDetailsDto.toDomain(taskId: TaskId): Solution {
        val filesDomain: List<FileInfo> = files?.map { it.toDomain() } ?: emptyList()
        val domainStatus = status.toDomain()
        val scoreDomain = score?.let(::Score)

        return Solution(
            id = id?.let(::SolutionId) ?: SolutionId(UUID.randomUUID()),
            taskId = taskId,
            authorId = UserId(UUID.randomUUID()),
            text = text,
            files = filesDomain,
            score = scoreDomain,
            status = domainStatus,
            updatedAt = updatedDate,
        )
    }

    private fun SolutionListItemDto.toDomain(taskId: TaskId): Solution {
        val filesDomain: List<FileInfo> = files?.map { it.toDomain() } ?: emptyList()
        val scoreDomain = score?.let(::Score)

        return Solution(
            id = SolutionId(id),
            taskId = taskId,
            authorId = UserId(user.id),
            text = text,
            files = filesDomain,
            score = scoreDomain,
            status = status.toDomain(),
            updatedAt = updatedDate,
        )
    }

    private fun FileDto.toDomain(): FileInfo =
        FileInfo(
            id = id ?: "",
            name = name ?: "",
        )

    private fun ApiSolutionStatus.toDomain(): SolutionStatus =
        when (this) {
            ApiSolutionStatus.pending -> SolutionStatus.PENDING
            ApiSolutionStatus.checked -> SolutionStatus.CHECKED
            ApiSolutionStatus.returned -> SolutionStatus.RETURNED
        }

    private fun SolutionStatus.toApi(): ApiSolutionStatus =
        when (this) {
            SolutionStatus.PENDING -> ApiSolutionStatus.pending
            SolutionStatus.CHECKED -> ApiSolutionStatus.checked
            SolutionStatus.RETURNED -> ApiSolutionStatus.returned
        }
}

