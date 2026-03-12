package com.stuf.domain.usecase

import com.stuf.domain.common.DomainError
import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.Solution
import com.stuf.domain.model.TaskId
import com.stuf.domain.repository.SolutionRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.OffsetDateTime
import java.util.UUID

private class FakeGetUserSolutionRepository : SolutionRepository {
    var lastGetUserSolutionTaskId: TaskId? = null
    val missingTaskId: TaskId = TaskId(UUID.randomUUID())

    override suspend fun submitSolution(
        taskId: TaskId,
        text: String?,
        fileIds: List<String>,
    ): DomainResult<Solution> = error("Not needed in this fake")

    override suspend fun cancelSolution(taskId: TaskId): DomainResult<Unit> {
        error("Not needed in this fake")
    }

    override suspend fun getUserSolution(taskId: TaskId): DomainResult<Solution?> {
        if (taskId == missingTaskId) {
            return DomainResult.Failure(DomainError.NotFound)
        }
        lastGetUserSolutionTaskId = taskId
        return DomainResult.Success(null)
    }

    override suspend fun getTaskSolutions(
        taskId: TaskId,
        status: com.stuf.domain.model.SolutionStatus?,
        studentId: com.stuf.domain.model.UserId?,
    ): DomainResult<List<Solution>> = error("Not needed in this fake")

    override suspend fun reviewSolution(
        solutionId: com.stuf.domain.model.SolutionId,
        review: com.stuf.domain.model.Review,
    ): DomainResult<Unit> = error("Not needed in this fake")
}

class GetUserSolutionUseCaseTest {

    @Test
    fun `delegates to repository and returns result`() {
        val repo = FakeGetUserSolutionRepository()
        val useCase : GetUserSolution = GetUserSolutionUseCase(repo)
        val taskId = TaskId(UUID.randomUUID())

        val result = kotlinx.coroutines.runBlocking {
            useCase(taskId)
        }

        assertTrue(result is DomainResult.Success<*>)
        assertEquals(taskId, repo.lastGetUserSolutionTaskId)
        assertEquals(null, (result as DomainResult.Success<Solution?>).value)
    }

    @Test
    fun `returns not found error when repository returns NotFound`() {
        val repo = FakeGetUserSolutionRepository()
        val useCase : GetUserSolution = GetUserSolutionUseCase(repo)
        val missingId = repo.missingTaskId

        val result = kotlinx.coroutines.runBlocking {
            useCase(missingId)
        }

        assertTrue(result is DomainResult.Failure)
        val error = (result as DomainResult.Failure).error
        assertEquals(DomainError.NotFound, error)
    }
}
