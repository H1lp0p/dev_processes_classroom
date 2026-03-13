package com.stuf.domain.usecase

import com.stuf.domain.common.DomainError
import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.Comment
import com.stuf.domain.model.CommentAuthor
import com.stuf.domain.model.CommentId
import com.stuf.domain.model.UserId
import com.stuf.domain.repository.CommentRepository
import com.stuf.domain.usecase.impl.AddCommentReplyUseCase
import java.time.OffsetDateTime
import java.util.UUID
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private class FakeAddCommentReplyRepository : CommentRepository {
    var lastReplyCommentId: CommentId? = null
    var lastReplyText: String? = null
    var result: DomainResult<Comment> = DomainResult.Success(
        Comment(
            id = UUID.randomUUID().toString(),
            author = CommentAuthor(
                id = UserId(UUID.randomUUID()),
                credentials = "Author",
            ),
            text = "Reply",
            createdAt = OffsetDateTime.now(),
            isPrivate = false,
        ),
    )

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
        error("Not needed in this fake")
    }

    override suspend fun addCommentReply(
        commentId: CommentId,
        text: String,
    ): DomainResult<Comment> {
        lastReplyCommentId = commentId
        lastReplyText = text
        return result
    }
}

class AddCommentReplyUseCaseTest {

    @Test
    fun `non_blank_text_is_trimmed_and_delegated`() {
        val repository: CommentRepository = FakeAddCommentReplyRepository()
        val useCase: AddCommentReply = AddCommentReplyUseCase(repository)
        val commentId: CommentId = CommentId(UUID.randomUUID().toString())

        val result: DomainResult<Comment> = kotlinx.coroutines.runBlocking {
            useCase(commentId, "  reply text  ")
        }

        assertTrue(result is DomainResult.Success<*>)
        assertEquals(commentId, (repository as FakeAddCommentReplyRepository).lastReplyCommentId)
        assertEquals("reply text", repository.lastReplyText)
    }

    @Test
    fun `blank_text_returns_validation_error_and_does_not_delegate`() {
        val repository: CommentRepository = FakeAddCommentReplyRepository()
        val useCase: AddCommentReply = AddCommentReplyUseCase(repository)
        val commentId: CommentId = CommentId(UUID.randomUUID().toString())

        val result: DomainResult<Comment> = kotlinx.coroutines.runBlocking {
            useCase(commentId, "   ")
        }

        assertTrue(result is DomainResult.Failure)
        val error: DomainError = (result as DomainResult.Failure).error
        assertTrue(error is DomainError.Validation)
        assertEquals(null, (repository as FakeAddCommentReplyRepository).lastReplyText)
    }
}

