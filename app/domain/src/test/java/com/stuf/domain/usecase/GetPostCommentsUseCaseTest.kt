package com.stuf.domain.usecase

import com.stuf.domain.common.DomainError
import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.Comment
import com.stuf.domain.model.PostId
import com.stuf.domain.repository.CommentRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

private class FakeGetPostCommentsRepository : CommentRepository {
    var lastGetPostCommentsPostId: PostId? = null
    val missingPostId: PostId = PostId(UUID.randomUUID())

    override suspend fun getPostComments(postId: PostId): DomainResult<List<Comment>> {
        if (postId == missingPostId) {
            return DomainResult.Failure(DomainError.NotFound)
        }
        lastGetPostCommentsPostId = postId
        return DomainResult.Success(emptyList())
    }

    override suspend fun getSolutionComments(
        solutionId: com.stuf.domain.model.SolutionId,
    ): DomainResult<List<Comment>> = error("Not needed in this fake")

    override suspend fun addPostComment(postId: PostId, text: String): DomainResult<Comment> {
        error("Not needed in this fake")
    }

    override suspend fun addSolutionComment(
        solutionId: com.stuf.domain.model.SolutionId,
        text: String,
    ): DomainResult<Comment> = error("Not needed in this fake")
}

class GetPostCommentsUseCaseTest {

    @Test
    fun `delegates to repository and returns comments`() {
        val repo = FakeGetPostCommentsRepository()
        val useCase : GetPostComments = GetPostCommentsUseCase(repo)
        val postId = PostId(UUID.randomUUID())

        val result = kotlinx.coroutines.runBlocking {
            useCase(postId)
        }

        assertTrue(result is DomainResult.Success<*>)
        assertEquals(postId, repo.lastGetPostCommentsPostId)
        assertTrue((result as DomainResult.Success<List<Comment>>).value.isEmpty())
    }

    @Test
    fun `returns not found error when repository returns NotFound`() {
        val repo = FakeGetPostCommentsRepository()
        val useCase : GetPostComments = GetPostCommentsUseCase(repo)
        val missingId = repo.missingPostId

        val result = kotlinx.coroutines.runBlocking {
            useCase(missingId)
        }

        assertTrue(result is DomainResult.Failure)
        val error = (result as DomainResult.Failure).error
        assertEquals(DomainError.NotFound, error)
    }
}
