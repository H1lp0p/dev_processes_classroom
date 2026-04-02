package com.stuf.domain.usecase

import com.stuf.domain.common.DomainError
import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.Comment
import com.stuf.domain.model.CommentId
import com.stuf.domain.model.SolutionId
import com.stuf.domain.repository.CommentRepository
import com.stuf.domain.usecase.impl.GetSolutionCommentsUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

private class FakeGetSolutionCommentsRepository : CommentRepository {
    var lastGetSolutionCommentsSolutionId: SolutionId? = null
    val missingSolutionId: SolutionId = SolutionId(UUID.randomUUID())

    override suspend fun getPostComments(postId: com.stuf.domain.model.PostId): DomainResult<List<Comment>> =
        error("Not needed in this fake")

    override suspend fun getSolutionComments(solutionId: SolutionId): DomainResult<List<Comment>> {
        if (solutionId == missingSolutionId) {
            return DomainResult.Failure(DomainError.NotFound)
        }
        lastGetSolutionCommentsSolutionId = solutionId
        return DomainResult.Success(emptyList())
    }

    override suspend fun addPostComment(
        postId: com.stuf.domain.model.PostId,
        text: String,
    ): DomainResult<Comment> = error("Not needed in this fake")

    override suspend fun addSolutionComment(
        solutionId: SolutionId,
        text: String,
    ): DomainResult<Comment> = error("Not needed in this fake")

    override suspend fun getCommentReplies(commentId: CommentId): DomainResult<List<Comment>> =
        error("Not needed in this fake")

    override suspend fun addCommentReply(commentId: CommentId, text: String): DomainResult<Comment> =
        error("Not needed in this fake")
}

class GetSolutionCommentsUseCaseTest {

    @Test
    fun `delegates to repository and returns comments`() {
        val repo = FakeGetSolutionCommentsRepository()
        val useCase : GetSolutionComments = GetSolutionCommentsUseCase(repo)
        val solutionId = SolutionId(UUID.randomUUID())

        val result = kotlinx.coroutines.runBlocking {
            useCase(solutionId)
        }

        assertTrue(result is DomainResult.Success<*>)
        assertEquals(solutionId, repo.lastGetSolutionCommentsSolutionId)
        assertTrue((result as DomainResult.Success<List<Comment>>).value.isEmpty())
    }

    @Test
    fun `returns not found error when repository returns NotFound`() {
        val repo = FakeGetSolutionCommentsRepository()
        val useCase : GetSolutionComments = GetSolutionCommentsUseCase(repo)
        val missingId = repo.missingSolutionId

        val result = kotlinx.coroutines.runBlocking {
            useCase(missingId)
        }

        assertTrue(result is DomainResult.Failure)
        val error = (result as DomainResult.Failure).error
        assertEquals(DomainError.NotFound, error)
    }
}
