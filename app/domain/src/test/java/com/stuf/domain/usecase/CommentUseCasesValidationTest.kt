package com.stuf.domain.usecase

import com.stuf.domain.common.DomainError
import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.Comment
import com.stuf.domain.model.CommentAuthor
import com.stuf.domain.model.PostId
import com.stuf.domain.model.SolutionId
import com.stuf.domain.repository.CommentRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.OffsetDateTime
import java.util.UUID

private class FakeCommentRepository : CommentRepository {
    var lastPostCommentText: String? = null
    var lastSolutionCommentText: String? = null

    override suspend fun getPostComments(postId: PostId): DomainResult<List<Comment>> {
        error("Not needed in this fake")
    }

    override suspend fun getSolutionComments(solutionId: SolutionId): DomainResult<List<Comment>> {
        error("Not needed in this fake")
    }

    override suspend fun addPostComment(postId: PostId, text: String): DomainResult<Comment> {
        lastPostCommentText = text
        return DomainResult.Success(
            Comment(
                id = UUID.randomUUID().toString(),
                author = CommentAuthor(
                    id = com.stuf.domain.model.UserId(UUID.randomUUID()),
                    credentials = "Student",
                ),
                text = text,
                createdAt = OffsetDateTime.now(),
                isPrivate = false,
            ),
        )
    }

    override suspend fun addSolutionComment(solutionId: SolutionId, text: String): DomainResult<Comment> {
        lastSolutionCommentText = text
        return DomainResult.Success(
            Comment(
                id = UUID.randomUUID().toString(),
                author = CommentAuthor(
                    id = com.stuf.domain.model.UserId(UUID.randomUUID()),
                    credentials = "Student",
                ),
                text = text,
                createdAt = OffsetDateTime.now(),
                isPrivate = false,
            ),
        )
    }
}

class CommentUseCasesValidationTest {

    @Test
    fun `post comment with non-blank text is trimmed and delegated`() {
        val repo = FakeCommentRepository()
        val useCase : AddPostComment = AddPostCommentUseCase(repo)
        val postId = PostId(UUID.randomUUID())

        val result = kotlinx.coroutines.runBlocking {
            useCase(postId, "  hello  ")
        }

        assertTrue(result is DomainResult.Success<*>)
        assertEquals("hello", repo.lastPostCommentText)
        val comment = (result as DomainResult.Success<Comment>).value
        assertEquals("hello", comment.text)
    }

    @Test
    fun `post comment with blank text returns validation error and does not delegate`() {
        val repo = FakeCommentRepository()
        val useCase : AddPostComment = AddPostCommentUseCase(repo)
        val postId = PostId(UUID.randomUUID())

        val result = kotlinx.coroutines.runBlocking {
            useCase(postId, "   ")
        }

        assertTrue(result is DomainResult.Failure)
        val error = (result as DomainResult.Failure).error
        assertTrue(error is DomainError.Validation)
        assertEquals(null, repo.lastPostCommentText)
    }

    @Test
    fun `solution comment with non-blank text is trimmed and delegated`() {
        val repo = FakeCommentRepository()
        val useCase : AddSolutionComment = AddSolutionCommentUseCase(repo)
        val solutionId = SolutionId(UUID.randomUUID())

        val result = kotlinx.coroutines.runBlocking {
            useCase(solutionId, "  hello  ")
        }

        assertTrue(result is DomainResult.Success<*>)
        assertEquals("hello", repo.lastSolutionCommentText)
        val comment = (result as DomainResult.Success<Comment>).value
        assertEquals("hello", comment.text)
    }

    @Test
    fun `solution comment with blank text returns validation error and does not delegate`() {
        val repo = FakeCommentRepository()
        val useCase : AddSolutionComment = AddSolutionCommentUseCase(repo)
        val solutionId = SolutionId(UUID.randomUUID())

        val result = kotlinx.coroutines.runBlocking {
            useCase(solutionId, "   ")
        }

        assertTrue(result is DomainResult.Failure)
        val error = (result as DomainResult.Failure).error
        assertTrue(error is DomainError.Validation)
        assertEquals(null, repo.lastSolutionCommentText)
    }
}

