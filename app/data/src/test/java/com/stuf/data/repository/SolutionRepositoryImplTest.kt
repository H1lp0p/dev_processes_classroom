package com.stuf.data.repository

import com.stuf.data.api.SolutionApi
import com.stuf.data.model.FileDto
import com.stuf.data.model.IdRequestDto
import com.stuf.data.model.IdRequestDtoApiResponse
import com.stuf.data.model.SolutionListDto
import com.stuf.data.model.SolutionListDtoApiResponse
import com.stuf.data.model.SolutionListItemDto
import com.stuf.data.model.SolutionStatus as ApiSolutionStatus
import com.stuf.data.model.StudentSolutionDetailsDto
import com.stuf.data.model.StudentSolutionDetailsDtoApiResponse
import com.stuf.data.model.SubmitSolutionRequestDto
import com.stuf.data.model.UpdateSolutionRequestDto
import com.stuf.data.model.ApiResponseType
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
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import java.time.OffsetDateTime
import java.util.UUID

private class FakeSolutionApi : SolutionApi {
    var lastSubmitTaskId: UUID? = null
    var lastSubmitRequest: SubmitSolutionRequestDto? = null

    var lastCancelTaskId: UUID? = null
    var lastGetTaskId: UUID? = null

    var lastListTaskId: UUID? = null
    var lastListStatus: ApiSolutionStatus? = null
    var lastListStudentId: UUID? = null

    var lastReviewSolutionId: UUID? = null
    var lastReviewRequest: UpdateSolutionRequestDto? = null

    var submitResponse: Response<IdRequestDtoApiResponse>? = null
    var cancelResponse: Response<IdRequestDtoApiResponse>? = null
    var getUserSolutionResponse: Response<StudentSolutionDetailsDtoApiResponse>? = null
    var listSolutionsResponse: Response<SolutionListDtoApiResponse>? = null
    var reviewResponse: Response<IdRequestDtoApiResponse>? = null

    override suspend fun apiSolutionSolutionIdReviewPost(
        solutionId: UUID,
        updateSolutionRequestDto: UpdateSolutionRequestDto?,
    ): Response<IdRequestDtoApiResponse> {
        lastReviewSolutionId = solutionId
        lastReviewRequest = updateSolutionRequestDto
        return requireNotNull(reviewResponse)
    }

    override suspend fun apiTaskIdSolutionDelete(id: UUID): Response<IdRequestDtoApiResponse> {
        lastCancelTaskId = id
        return requireNotNull(cancelResponse)
    }

    override suspend fun apiTaskIdSolutionGet(id: UUID): Response<StudentSolutionDetailsDtoApiResponse> {
        lastGetTaskId = id
        return requireNotNull(getUserSolutionResponse)
    }

    override suspend fun apiTaskIdSolutionPut(
        id: UUID,
        submitSolutionRequestDto: SubmitSolutionRequestDto?,
    ): Response<IdRequestDtoApiResponse> {
        lastSubmitTaskId = id
        lastSubmitRequest = submitSolutionRequestDto
        return requireNotNull(submitResponse)
    }

    override suspend fun apiTaskIdSolutionsGet(
        id: UUID,
        skip: Int?,
        take: Int?,
        status: ApiSolutionStatus?,
        studentId: UUID?,
    ): Response<SolutionListDtoApiResponse> {
        lastListTaskId = id
        lastListStatus = status
        lastListStudentId = studentId
        return requireNotNull(listSolutionsResponse)
    }
}

class SolutionRepositoryImplTest {

    @Test
    fun `submitSolution success maps solution id and sends correct payload`() {
        val taskId = UUID.fromString("00000000-0000-0000-0000-000000000001")
        val api = FakeSolutionApi().apply {
            submitResponse = Response.success(
                IdRequestDtoApiResponse(
                    type = ApiResponseType.success,
                    message = null,
                    data = IdRequestDto(
                        id = UUID.fromString("00000000-0000-0000-0000-000000000010"),
                    ),
                ),
            )
        }
        val repository: SolutionRepository = SolutionRepositoryImpl(api)

        val result = runBlocking {
            repository.submitSolution(
                taskId = TaskId(taskId),
                text = "My solution",
                fileIds = listOf("file-1", "file-2"),
            )
        }

        assertTrue(result is DomainResult.Success<Solution>)
        val solution = (result as DomainResult.Success<Solution>).value
        assertEquals(SolutionId(UUID.fromString("00000000-0000-0000-0000-000000000010")), solution.id)
        assertEquals(TaskId(taskId), solution.taskId)

        assertEquals(taskId, api.lastSubmitTaskId)
        assertEquals("My solution", api.lastSubmitRequest?.text)
        assertEquals(listOf("file-1", "file-2"), api.lastSubmitRequest?.files)
    }

    @Test
    fun `cancelSolution success returns Unit`() {
        val taskId = UUID.fromString("00000000-0000-0000-0000-000000000020")
        val api = FakeSolutionApi().apply {
            cancelResponse = Response.success(
                IdRequestDtoApiResponse(
                    type = ApiResponseType.success,
                    message = null,
                    data = IdRequestDto(id = taskId),
                ),
            )
        }
        val repository: SolutionRepository = SolutionRepositoryImpl(api)

        val result = runBlocking {
            repository.cancelSolution(TaskId(taskId))
        }

        assertTrue(result is DomainResult.Success<Unit>)
        assertEquals(taskId, api.lastCancelTaskId)
    }

    @Test
    fun `getUserSolution success maps solution details`() {
        val taskId = UUID.fromString("00000000-0000-0000-0000-000000000030")
        val solutionId = UUID.fromString("00000000-0000-0000-0000-000000000031")
        val userId = UUID.fromString("00000000-0000-0000-0000-000000000032")
        val api = FakeSolutionApi().apply {
            getUserSolutionResponse = Response.success(
                StudentSolutionDetailsDtoApiResponse(
                    type = ApiResponseType.success,
                    message = null,
                    data = StudentSolutionDetailsDto(
                        id = solutionId,
                        text = "Answer",
                        files = listOf(
                            FileDto(
                                id = "file-id",
                                name = "file.txt",
                            ),
                        ),
                        score = 7,
                        status = ApiSolutionStatus.checked,
                        updatedDate = OffsetDateTime.parse("2024-01-02T12:00:00Z"),
                    ),
                ),
            )
        }
        val repository: SolutionRepository = SolutionRepositoryImpl(api)

        val result = runBlocking {
            repository.getUserSolution(TaskId(taskId))
        }

        assertTrue(result is DomainResult.Success<Solution?>)
        val solution = (result as DomainResult.Success<Solution?>).value!!
        assertEquals(SolutionId(solutionId), solution.id)
        assertEquals(TaskId(taskId), solution.taskId)
        assertEquals("Answer", solution.text)
        assertEquals(1, solution.files.size)
        assertEquals(FileInfo(id = "file-id", name = "file.txt"), solution.files.first())
        assertEquals(Score(7), solution.score)
        assertEquals(SolutionStatus.CHECKED, solution.status)
    }

    @Test
    fun `getUserSolution when not found returns Success_null`() {
        val taskId = UUID.fromString("00000000-0000-0000-0000-000000000040")
        val api = FakeSolutionApi().apply {
            val body = ResponseBody.create("application/json".toMediaType(), "{}")
            getUserSolutionResponse = Response.error(404, body)
        }
        val repository: SolutionRepository = SolutionRepositoryImpl(api)

        val result = runBlocking {
            repository.getUserSolution(TaskId(taskId))
        }

        assertTrue(result is DomainResult.Success<Solution?>)
        assertNull((result as DomainResult.Success<Solution?>).value)
    }

    @Test
    fun `getTaskSolutions success maps list and filters`() {
        val taskId = UUID.fromString("00000000-0000-0000-0000-000000000050")
        val studentId = UUID.fromString("00000000-0000-0000-0000-000000000051")
        val api = FakeSolutionApi().apply {
            listSolutionsResponse = Response.success(
                SolutionListDtoApiResponse(
                    type = ApiResponseType.success,
                    message = null,
                    data = SolutionListDto(
                        records = listOf(
                            SolutionListItemDto(
                                id = UUID.fromString("00000000-0000-0000-0000-000000000052"),
                                user = com.stuf.data.model.UserCredentialsDto(
                                    credentials = "User Name",
                                    id = studentId,
                                ),
                                text = "Solution text",
                                score = 5,
                                status = ApiSolutionStatus.pending,
                                files = listOf(
                                    FileDto(
                                        id = "f1",
                                        name = "f1.txt",
                                    ),
                                ),
                                updatedDate = OffsetDateTime.parse("2024-01-03T12:00:00Z"),
                            ),
                        ),
                        totalRecords = 1,
                    ),
                ),
            )
        }
        val repository: SolutionRepository = SolutionRepositoryImpl(api)

        val result = runBlocking {
            repository.getTaskSolutions(
                taskId = TaskId(taskId),
                status = SolutionStatus.PENDING,
                studentId = UserId(studentId),
            )
        }

        assertTrue(result is DomainResult.Success<List<Solution>>)
        val solutions = (result as DomainResult.Success<List<Solution>>).value
        assertEquals(1, solutions.size)
        val first = solutions.first()
        assertEquals(SolutionStatus.PENDING, first.status)
        assertEquals(SolutionId(UUID.fromString("00000000-0000-0000-0000-000000000052")), first.id)

        assertEquals(taskId, api.lastListTaskId)
        assertEquals(ApiSolutionStatus.pending, api.lastListStatus)
        assertEquals(studentId, api.lastListStudentId)
    }

    @Test
    fun `reviewSolution success returns Unit and passes score status comment`() {
        val solutionId = UUID.fromString("00000000-0000-0000-0000-000000000060")
        val api = FakeSolutionApi().apply {
            reviewResponse = Response.success(
                IdRequestDtoApiResponse(
                    type = ApiResponseType.success,
                    message = null,
                    data = IdRequestDto(id = solutionId),
                ),
            )
        }
        val repository: SolutionRepository = SolutionRepositoryImpl(api)

        val review = Review(
            score = Score(10),
            status = SolutionStatus.CHECKED,
            comment = "Good job",
        )

        val result = runBlocking {
            repository.reviewSolution(SolutionId(solutionId), review)
        }

        assertTrue(result is DomainResult.Success<Unit>)
        assertEquals(solutionId, api.lastReviewSolutionId)
        assertEquals(10, api.lastReviewRequest?.score)
        assertEquals(ApiSolutionStatus.checked, api.lastReviewRequest?.status)
        assertEquals("Good job", api.lastReviewRequest?.comment)
    }
}

