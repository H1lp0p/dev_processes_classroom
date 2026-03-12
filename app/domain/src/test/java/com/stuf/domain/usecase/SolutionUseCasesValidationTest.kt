package com.stuf.domain.usecase

import com.stuf.domain.common.DomainError
import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.Solution
import com.stuf.domain.model.TaskId
import com.stuf.domain.model.UserId
import com.stuf.domain.model.SolutionId
import com.stuf.domain.model.SolutionStatus
import com.stuf.domain.model.Review
import com.stuf.domain.repository.SolutionRepository
import com.stuf.domain.usecase.impl.SubmitSolutionUseCase
import com.stuf.domain.usecase.impl.UpdateSolutionUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.OffsetDateTime
import java.util.UUID
import kotlin.collections.emptyList

private class FakeSolutionRepository : SolutionRepository {
    var lastSubmitArgs: Triple<TaskId, String?, List<String>>? = null

    override suspend fun submitSolution(
        taskId: TaskId,
        text: String?,
        fileIds: List<String>,
    ): DomainResult<Solution> {
        lastSubmitArgs = Triple(taskId, text, fileIds)
        return DomainResult.Success(
            Solution(
                id = SolutionId(UUID.randomUUID()),
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

    override suspend fun cancelSolution(taskId: TaskId): DomainResult<Unit> {
        error("Not needed in this fake")
    }

    override suspend fun getUserSolution(taskId: TaskId): DomainResult<Solution?> {
        error("Not needed in this fake")
    }

    override suspend fun getTaskSolutions(
        taskId: TaskId,
        status: SolutionStatus?,
        studentId: UserId?,
    ): DomainResult<List<Solution>> {
        error("Not needed in this fake")
    }

    override suspend fun reviewSolution(solutionId: SolutionId, review: Review): DomainResult<Unit> {
        error("Not needed in this fake")
    }
}

class SubmitAndUpdateSolutionValidationTest {

    @Test
    fun `submit solution with non-blank text and empty files delegates to repository`() {
        val repo = FakeSolutionRepository()
        val useCase : SubmitSolution = SubmitSolutionUseCase(repo)
        val taskId = TaskId(UUID.randomUUID())

        val result = kotlinx.coroutines.runBlocking {
            useCase(taskId, "answer", emptyList())
        }

        assertTrue(result is DomainResult.Success<*>)
        assertEquals(taskId, repo.lastSubmitArgs?.first)
        assertEquals("answer", repo.lastSubmitArgs?.second)
        assertTrue(repo.lastSubmitArgs?.third?.isEmpty() == true)
    }

    @Test
    fun `submit solution with null text and non-empty files delegates to repository`() {
        val repo = FakeSolutionRepository()
        val useCase : SubmitSolution = SubmitSolutionUseCase(repo)
        val taskId = TaskId(UUID.randomUUID())

        val result = kotlinx.coroutines.runBlocking {
            useCase(taskId, null, listOf("file1"))
        }

        assertTrue(result is DomainResult.Success<*>)
        assertEquals(taskId, repo.lastSubmitArgs?.first)
        assertEquals(null, repo.lastSubmitArgs?.second)
        assertEquals(listOf("file1"), repo.lastSubmitArgs?.third)
    }

    @Test
    fun `submit solution with blank text and empty files returns validation error and does not delegate`() {
        val repo = FakeSolutionRepository()
        val useCase : SubmitSolution = SubmitSolutionUseCase(repo)
        val taskId = TaskId(UUID.randomUUID())

        val result = kotlinx.coroutines.runBlocking {
            useCase(taskId, "   ", emptyList())
        }

        assertTrue(result is DomainResult.Failure)
        val error = (result as DomainResult.Failure).error
        assertTrue(error is DomainError.Validation)
        assertEquals(null, repo.lastSubmitArgs)
    }

    @Test
    fun `update solution with non-blank text and empty files delegates to repository`() {
        val repo = FakeSolutionRepository()
        val useCase : UpdateSolution = UpdateSolutionUseCase(repo)
        val taskId = TaskId(UUID.randomUUID())

        val result = kotlinx.coroutines.runBlocking {
            useCase(taskId, "answer", emptyList())
        }

        assertTrue(result is DomainResult.Success<*>)
        assertEquals(taskId, repo.lastSubmitArgs?.first)
        assertEquals("answer", repo.lastSubmitArgs?.second)
        assertTrue(repo.lastSubmitArgs?.third?.isEmpty() == true)
    }

    @Test
    fun `update solution with null text and non-empty files delegates to repository`() {
        val repo = FakeSolutionRepository()
        val useCase : UpdateSolution = UpdateSolutionUseCase(repo)
        val taskId = TaskId(UUID.randomUUID())

        val result = kotlinx.coroutines.runBlocking {
            useCase(taskId, null, listOf("file1"))
        }

        assertTrue(result is DomainResult.Success<*>)
        assertEquals(taskId, repo.lastSubmitArgs?.first)
        assertEquals(null, repo.lastSubmitArgs?.second)
        assertEquals(listOf("file1"), repo.lastSubmitArgs?.third)
    }

    @Test
    fun `update solution with blank text and empty files returns validation error and does not delegate`() {
        val repo = FakeSolutionRepository()
        val useCase : UpdateSolution = UpdateSolutionUseCase(repo)
        val taskId = TaskId(UUID.randomUUID())

        val result = kotlinx.coroutines.runBlocking {
            useCase(taskId, "   ", emptyList())
        }

        assertTrue(result is DomainResult.Failure)
        val error = (result as DomainResult.Failure).error
        assertTrue(error is DomainError.Validation)
        assertEquals(null, repo.lastSubmitArgs)
    }
}

