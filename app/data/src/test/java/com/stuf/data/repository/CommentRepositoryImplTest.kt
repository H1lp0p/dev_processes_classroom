package com.stuf.data.repository

import com.stuf.data.api.CommentApi
import com.stuf.data.model.AddCommentRequestDto
import com.stuf.data.model.ApiResponseType
import com.stuf.data.model.CommentAuthorDto
import com.stuf.data.model.CommentDto
import com.stuf.data.model.CommentDtoListApiResponse
import com.stuf.data.model.EditCommentRequestDto
import com.stuf.data.model.IdRequestDto
import com.stuf.data.model.IdRequestDtoApiResponse
import com.stuf.domain.common.DomainError
import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.Comment
import com.stuf.domain.model.PostId
import com.stuf.domain.model.SolutionId
import com.stuf.domain.model.UserId
import com.stuf.domain.repository.CommentRepository
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import java.util.UUID

private class FakeCommentApi : CommentApi {
    var lastPostIdForGet: UUID? = null
    var lastSolutionIdForGet: UUID? = null
    var lastPostIdForAdd: UUID? = null
    var lastSolutionIdForAdd: UUID? = null
    var lastAddedText: String? = null
    var lastRepliesCommentId: UUID? = null

    var postCommentsResponse: Response<CommentDtoListApiResponse>? = null
    var solutionCommentsResponse: Response<CommentDtoListApiResponse>? = null
    var addPostCommentResponse: Response<IdRequestDtoApiResponse>? = null
    var addSolutionCommentResponse: Response<IdRequestDtoApiResponse>? = null

    override suspend fun apiCommentIdDelete(id: UUID): Response<IdRequestDtoApiResponse> {
        error("Not needed in these tests")
    }

    override suspend fun apiCommentIdPut(
        id: UUID,
        editCommentRequestDto: EditCommentRequestDto?,
    ): Response<IdRequestDtoApiResponse> {
        error("Not needed in these tests")
    }

    override suspend fun apiCommentIdRepliesGet(id: UUID): Response<CommentDtoListApiResponse> {
        lastRepliesCommentId = id
        return requireNotNull(postCommentsResponse)
    }

    override suspend fun apiCommentIdReplyPost(
        id: UUID,
        addCommentRequestDto: AddCommentRequestDto?,
    ): Response<IdRequestDtoApiResponse> {
        lastRepliesCommentId = id
        lastAddedText = addCommentRequestDto?.text
        return requireNotNull(addPostCommentResponse)
    }

    override suspend fun apiPostIdCommentGet(id: UUID): Response<CommentDtoListApiResponse> {
        lastPostIdForGet = id
        return requireNotNull(postCommentsResponse)
    }

    override suspend fun apiPostIdCommentPost(
        id: UUID,
        addCommentRequestDto: AddCommentRequestDto?,
    ): Response<IdRequestDtoApiResponse> {
        lastPostIdForAdd = id
        lastAddedText = addCommentRequestDto?.text
        return requireNotNull(addPostCommentResponse)
    }

    override suspend fun apiSolutionIdCommentGet(id: UUID): Response<CommentDtoListApiResponse> {
        lastSolutionIdForGet = id
        return requireNotNull(solutionCommentsResponse)
    }

    override suspend fun apiSolutionIdCommentPost(
        id: UUID,
        addCommentRequestDto: AddCommentRequestDto?,
    ): Response<IdRequestDtoApiResponse> {
        lastSolutionIdForAdd = id
        lastAddedText = addCommentRequestDto?.text
        return requireNotNull(addSolutionCommentResponse)
    }
}

class CommentRepositoryImplTest {

    @Test
    fun `getPostComments success maps list of comments`() {
        val postId = UUID.fromString("00000000-0000-0000-0000-000000000001")
        val commentId = UUID.randomUUID()

        val api = FakeCommentApi().apply {
            postCommentsResponse = Response.success(
                CommentDtoListApiResponse(
                    type = ApiResponseType.success,
                    message = null,
                    data = listOf(
                        CommentDto(
                            id = commentId,
                            text = "Nice post",
                            isDeleted = false,
                            author = CommentAuthorDto(
                                id = UUID.fromString("00000000-0000-0000-0000-000000000010"),
                                credentials = "User Name",
                            ),
                            nestedCount = 0,
                        ),
                    ),
                ),
            )
        }
        val repository: CommentRepository = CommentRepositoryImpl(api)

        val result = runBlocking {
            repository.getPostComments(PostId(postId))
        }

        assertTrue(result is DomainResult.Success<List<Comment>>)
        val comments = (result as DomainResult.Success<List<Comment>>).value
        assertEquals(1, comments.size)
        val comment = comments.first()
        assertEquals(commentId.toString(), comment.id)
        assertEquals("Nice post", comment.text)
        assertEquals(UserId(UUID.fromString("00000000-0000-0000-0000-000000000010")), comment.author.id)
        assertEquals("User Name", comment.author.credentials)
        assertEquals(postId, api.lastPostIdForGet)
    }

    @Test
    fun `getSolutionComments success maps list of comments`() {
        val solutionId = UUID.fromString("00000000-0000-0000-0000-000000000002")
        val commentId = UUID.randomUUID()

        val api = FakeCommentApi().apply {
            solutionCommentsResponse = Response.success(
                CommentDtoListApiResponse(
                    type = ApiResponseType.success,
                    message = null,
                    data = listOf(
                        CommentDto(
                            id = commentId,
                            text = "Looks good",
                            isDeleted = false,
                            author = CommentAuthorDto(
                                id = UUID.fromString("00000000-0000-0000-0000-000000000020"),
                                credentials = "Teacher",
                            ),
                            nestedCount = 0,
                        ),
                    ),
                ),
            )
        }
        val repository: CommentRepository = CommentRepositoryImpl(api)

        val result = runBlocking {
            repository.getSolutionComments(SolutionId(solutionId))
        }

        assertTrue(result is DomainResult.Success<List<Comment>>)
        val comments = (result as DomainResult.Success<List<Comment>>).value
        assertEquals(1, comments.size)
        val comment = comments.first()
        assertEquals(commentId.toString(), comment.id)
        assertEquals("Looks good", comment.text)
        assertEquals(UserId(UUID.fromString("00000000-0000-0000-0000-000000000020")), comment.author.id)
        assertEquals("Teacher", comment.author.credentials)
        assertEquals(solutionId, api.lastSolutionIdForGet)
    }

    @Test
    fun `addPostComment success returns created comment`() {
        val postId = UUID.fromString("00000000-0000-0000-0000-000000000003")
        val api = FakeCommentApi().apply {
            addPostCommentResponse = Response.success(
                IdRequestDtoApiResponse(
                    type = ApiResponseType.success,
                    message = null,
                    data = IdRequestDto(
                        id = UUID.fromString("00000000-0000-0000-0000-000000000030"),
                    ),
                ),
            )
        }
        val repository: CommentRepository = CommentRepositoryImpl(api)

        val result = runBlocking {
            repository.addPostComment(PostId(postId), text = "Comment text")
        }

        assertTrue(result is DomainResult.Success<Comment>)
        val comment = (result as DomainResult.Success<Comment>).value
        assertEquals("00000000-0000-0000-0000-000000000030", comment.id)
        assertEquals("Comment text", api.lastAddedText)
        assertEquals(postId, api.lastPostIdForAdd)
    }

    @Test
    fun `addSolutionComment success returns created comment`() {
        val solutionId = UUID.fromString("00000000-0000-0000-0000-000000000004")
        val api = FakeCommentApi().apply {
            addSolutionCommentResponse = Response.success(
                IdRequestDtoApiResponse(
                    type = ApiResponseType.success,
                    message = null,
                    data = IdRequestDto(
                        id = UUID.fromString("00000000-0000-0000-0000-000000000040"),
                    ),
                ),
            )
        }
        val repository: CommentRepository = CommentRepositoryImpl(api)

        val result = runBlocking {
            repository.addSolutionComment(SolutionId(solutionId), text = "Solution comment")
        }

        assertTrue(result is DomainResult.Success<Comment>)
        val comment = (result as DomainResult.Success<Comment>).value
        assertEquals("00000000-0000-0000-0000-000000000040", comment.id)
        assertEquals("Solution comment", api.lastAddedText)
        assertEquals(solutionId, api.lastSolutionIdForAdd)
    }

    @Test
    fun `getPostComments http 401 returns Unauthorized`() {
        val postId = UUID.randomUUID()
        val api = FakeCommentApi().apply {
            val body = ResponseBody.create("application/json".toMediaType(), "{}")
            postCommentsResponse = Response.error(401, body)
        }
        val repository: CommentRepository = CommentRepositoryImpl(api)

        val result = runBlocking {
            repository.getPostComments(PostId(postId))
        }

        assertTrue(result is DomainResult.Failure)
        assertEquals(DomainError.Unauthorized, (result as DomainResult.Failure).error)
    }

    @Test
    fun `getCommentReplies success maps list of replies`() {
        val commentId: UUID = UUID.fromString("00000000-0000-0000-0000-000000000050")
        val replyId: UUID = UUID.randomUUID()

        val api: FakeCommentApi = FakeCommentApi().apply {
            postCommentsResponse = Response.success(
                CommentDtoListApiResponse(
                    type = ApiResponseType.success,
                    message = null,
                    data = listOf(
                        CommentDto(
                            id = replyId,
                            text = "Nested reply",
                            isDeleted = false,
                            author = CommentAuthorDto(
                                id = UUID.fromString("00000000-0000-0000-0000-000000000051"),
                                credentials = "Nested Author",
                            ),
                            nestedCount = 0,
                        ),
                    ),
                ),
            )
        }
        val repository: CommentRepository = CommentRepositoryImpl(api)

        val result: DomainResult<List<Comment>> = runBlocking {
            repository.getCommentReplies(com.stuf.domain.model.CommentId(commentId.toString()))
        }

        assertTrue(result is DomainResult.Success<List<Comment>>)
        val replies: List<Comment> = (result as DomainResult.Success<List<Comment>>).value
        assertEquals(1, replies.size)
        val reply: Comment = replies.first()
        assertEquals(replyId.toString(), reply.id)
        assertEquals("Nested reply", reply.text)
        assertEquals(
            UserId(UUID.fromString("00000000-0000-0000-0000-000000000051")),
            reply.author.id,
        )
        assertEquals("Nested Author", reply.author.credentials)
        assertEquals(commentId, api.lastRepliesCommentId)
    }

    @Test
    fun `addCommentReply success returns created reply`() {
        val commentId: UUID = UUID.fromString("00000000-0000-0000-0000-000000000060")
        val api: FakeCommentApi = FakeCommentApi().apply {
            addPostCommentResponse = Response.success(
                IdRequestDtoApiResponse(
                    type = ApiResponseType.success,
                    message = null,
                    data = IdRequestDto(
                        id = UUID.fromString("00000000-0000-0000-0000-000000000061"),
                    ),
                ),
            )
        }
        val repository: CommentRepository = CommentRepositoryImpl(api)

        val result: DomainResult<Comment> = runBlocking {
            repository.addCommentReply(
                commentId = com.stuf.domain.model.CommentId(commentId.toString()),
                text = "Reply text",
            )
        }

        assertTrue(result is DomainResult.Success<Comment>)
        val reply: Comment = (result as DomainResult.Success<Comment>).value
        assertEquals("00000000-0000-0000-0000-000000000061", reply.id)
        assertEquals("Reply text", api.lastAddedText)
        assertEquals(commentId, api.lastRepliesCommentId)
    }
}

