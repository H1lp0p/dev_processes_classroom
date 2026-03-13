package com.stuf.domain.usecase

import com.stuf.domain.common.DomainError
import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.Comment
import com.stuf.domain.model.CommentAuthor
import com.stuf.domain.model.CommentId
import com.stuf.domain.model.UserId
import com.stuf.domain.repository.CommentRepository
import com.stuf.domain.usecase.impl.GetCommentRepliesUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.OffsetDateTime
import java.util.UUID

private class FakeCommentRepliesRepository : CommentRepository {
    var lastCommentId: CommentId? = null
    var repliesResult: DomainResult<List<Comment>> = DomainResult.Success(emptyList())

    override suspend fun getPostComments(postId: com.stuf.domain.model.PostId): DomainResult<List<Comment>> {
        error("Not needed in this fake")
    }

    override suspend fun getSolutionComments(
        solutionId: com.stuf.domain.model.SolutionId,
    ): DomainResult<List<Comment>> {
        error("Not needed in this fake")
    }

    override suspend fun addPostComment(
        postId: com.stuf.domain.model.PostId,
        text: String,
    ): DomainResult<Comment> {
        error("Not needed in this fake")
    }

    override suspend fun addSolutionComment(
        solutionId: com.stuf.domain.model.SolutionId,
        text: String,
    ): DomainResult<Comment> {
        error("Not needed in this fake")
    }

    override suspend fun getCommentReplies(commentId: CommentId): DomainResult<List<Comment>> {
        lastCommentId = commentId
        return repliesResult
    }

    override suspend fun addCommentReply(
        commentId: CommentId,
        text: String,
    ): DomainResult<Comment> {
        error("Not needed in this fake")
    }
}

class GetCommentRepliesUseCaseTest {

    @Test
    fun `delegates_to_repository_and_returns_replies`() {
        val repository: CommentRepository = FakeCommentRepliesRepository().apply {
            val authorId: UserId = UserId(UUID.randomUUID())
            val parentId: CommentId = CommentId(UUID.randomUUID().toString())
            val reply: Comment = Comment(
                id = UUID.randomUUID().toString(),
                author = CommentAuthor(
                    id = authorId,
                    credentials = "Author",
                ),
                text = "Reply",
                createdAt = OffsetDateTime.now(),
                isPrivate = false,
            )
            repliesResult = DomainResult.Success(listOf(reply))
        }
        val useCase: GetCommentReplies = GetCommentRepliesUseCase(repository)
        val commentId: CommentId = CommentId(UUID.randomUUID().toString())

        val result: DomainResult<List<Comment>> = kotlinx.coroutines.runBlocking {
            useCase(commentId)
        }

        assertTrue(result is DomainResult.Success<*>)
        val success: DomainResult.Success<List<Comment>> = result as DomainResult.Success<List<Comment>>
        assertEquals(commentId, (repository as FakeCommentRepliesRepository).lastCommentId)
        assertEquals(1, success.value.size)
        assertEquals("Reply", success.value[0].text)
    }

    @Test
    fun `returns_failure_from_repository`() {
        val repository: CommentRepository = FakeCommentRepliesRepository().apply {
            repliesResult = DomainResult.Failure(DomainError.NotFound)
        }
        val useCase: GetCommentReplies = GetCommentRepliesUseCase(repository)
        val commentId: CommentId = CommentId(UUID.randomUUID().toString())

        val result: DomainResult<List<Comment>> = kotlinx.coroutines.runBlocking {
            useCase(commentId)
        }

        assertTrue(result is DomainResult.Failure)
        val error: DomainError = (result as DomainResult.Failure).error
        assertEquals(DomainError.NotFound, error)
    }
}

