package com.stuf.domain.usecase

import com.stuf.domain.common.DomainError
import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.Review
import com.stuf.domain.model.Score
import com.stuf.domain.model.SolutionId
import com.stuf.domain.model.SolutionStatus
import com.stuf.domain.repository.SolutionRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

private class FakeReviewSolutionRepository : SolutionRepository {
    var lastReviewArgs: Pair<SolutionId, Review>? = null
    val missingSolutionId: SolutionId = SolutionId(UUID.randomUUID())

    override suspend fun submitSolution(
        taskId: com.stuf.domain.model.TaskId,
        text: String?,
        fileIds: List<String>,
    ): DomainResult<com.stuf.domain.model.Solution> = error("Not needed in this fake")

    override suspend fun cancelSolution(taskId: com.stuf.domain.model.TaskId): DomainResult<Unit> {
        error("Not needed in this fake")
    }

    override suspend fun getUserSolution(
        taskId: com.stuf.domain.model.TaskId,
    ): DomainResult<com.stuf.domain.model.Solution?> = error("Not needed in this fake")

    override suspend fun getTaskSolutions(
        taskId: com.stuf.domain.model.TaskId,
        status: SolutionStatus?,
        studentId: com.stuf.domain.model.UserId?,
    ): DomainResult<List<com.stuf.domain.model.Solution>> = error("Not needed in this fake")

    override suspend fun reviewSolution(solutionId: SolutionId, review: Review): DomainResult<Unit> {
        if (solutionId == missingSolutionId) {
            return DomainResult.Failure(DomainError.NotFound)
        }
        lastReviewArgs = Pair(solutionId, review)
        return DomainResult.Success(Unit)
    }
}

class ReviewSolutionUseCaseTest {

    @Test
    fun `delegates to repository and returns success`() {
        val repo = FakeReviewSolutionRepository()
        val useCase : ReviewSolution = ReviewSolutionUseCase(repo)
        val solutionId = SolutionId(UUID.randomUUID())
        val review = Review(
            score = Score(4),
            status = SolutionStatus.CHECKED,
            comment = "Good",
        )

        val result = kotlinx.coroutines.runBlocking {
            useCase(solutionId, review)
        }

        assertTrue(result is DomainResult.Success<*>)
        assertEquals(solutionId, repo.lastReviewArgs?.first)
        assertEquals(review, repo.lastReviewArgs?.second)
    }

    @Test
    fun `returns not found error when repository returns NotFound`() {
        val repo = FakeReviewSolutionRepository()
        val useCase : ReviewSolution = ReviewSolutionUseCase(repo)
        val missingId = repo.missingSolutionId
        val review = Review(
            score = Score(4),
            status = SolutionStatus.CHECKED,
            comment = "Good",
        )

        val result = kotlinx.coroutines.runBlocking {
            useCase(missingId, review)
        }

        assertTrue(result is DomainResult.Failure)
        val error = (result as DomainResult.Failure).error
        assertEquals(DomainError.NotFound, error)
    }
}
