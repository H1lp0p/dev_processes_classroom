package com.stuf.domain.usecase

import com.stuf.domain.common.DomainError
import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.Solution
import com.stuf.domain.model.TaskId
import com.stuf.domain.repository.SolutionRepository
import com.stuf.domain.usecase.impl.CancelSolutionUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

private class FakeCancelSolutionRepository : SolutionRepository {
    var lastCancelSolutionTaskId: TaskId? = null

    val incorrectSolutionTaskId: TaskId = TaskId(UUID.randomUUID())

    override suspend fun submitSolution(
        taskId: TaskId,
        text: String?,
        fileIds: List<String>,
    ): DomainResult<Solution> = error("Not needed in this fake")

    override suspend fun cancelSolution(taskId: TaskId): DomainResult<Unit> {
        if (taskId == incorrectSolutionTaskId) {
            return DomainResult.Failure(DomainError.NotFound)
        }
        lastCancelSolutionTaskId = taskId
        return DomainResult.Success(Unit)
    }

    override suspend fun getUserSolution(taskId: TaskId): DomainResult<Solution?> {
        error("Not needed in this fake")
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

class CancelSolutionUseCaseTest {

    @Test
    fun `delegates to repository and returns success`() {
        val repo = FakeCancelSolutionRepository()
        val useCase : CancelSolution = CancelSolutionUseCase(repo)
        val taskId = TaskId(UUID.randomUUID())

        val result = kotlinx.coroutines.runBlocking {
            useCase(taskId)
        }

        assertTrue(result is DomainResult.Success<*>)
        assertEquals(taskId, repo.lastCancelSolutionTaskId)
    }

    @Test
    fun `returns not found error when repository returns NotFound`() {
        val repo = FakeCancelSolutionRepository()
        val incorrectTaskId = repo.incorrectSolutionTaskId
        val useCase : CancelSolution = CancelSolutionUseCase(repo)

        val result = kotlinx.coroutines.runBlocking {
            useCase(incorrectTaskId)
        }

        assertTrue(result is DomainResult.Failure)
        val error = (result as DomainResult.Failure).error
        assertEquals(DomainError.NotFound, error)
    }
}
