package com.stuf.data.repository

import com.stuf.data.api.CommentApi
import com.stuf.data.common.httpCodeToDomainError
import com.stuf.data.model.AddCommentRequestDto
import com.stuf.data.model.ApiResponseType
import com.stuf.data.model.CommentAuthorDto
import com.stuf.data.model.CommentDto
import com.stuf.data.model.CommentDtoListApiResponse
import com.stuf.data.model.EditCommentRequestDto
import com.stuf.data.model.IdRequestDtoApiResponse
import com.stuf.domain.common.DomainError
import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.Comment
import com.stuf.domain.model.CommentAuthor
import com.stuf.domain.model.CommentId
import com.stuf.domain.model.PostId
import com.stuf.domain.model.SolutionId
import com.stuf.domain.model.UserId
import com.stuf.domain.repository.CommentRepository
import android.util.Log
import javax.inject.Inject
import retrofit2.Response
import java.io.IOException
import java.util.UUID

class CommentRepositoryImpl @Inject constructor(
    private val api: CommentApi,
) : CommentRepository {

    override suspend fun getPostComments(postId: PostId): DomainResult<List<Comment>> {
        val response = safeCallList { api.apiPostIdCommentGet(postId.value) }
        return response
    }

    override suspend fun getSolutionComments(solutionId: SolutionId): DomainResult<List<Comment>> {
        Log.d("CommentRepository", "GET /api/solution/{id}/comment solutionId=${solutionId.value}")
        val response = safeCallList { api.apiSolutionIdCommentGet(solutionId.value) }
        when (response) {
            is DomainResult.Success ->
                Log.d(
                    "CommentRepository",
                    "GET /api/solution/{id}/comment success count=${response.value.size}",
                )
            is DomainResult.Failure ->
                Log.e(
                    "CommentRepository",
                    "GET /api/solution/{id}/comment failed error=${response.error}",
                )
        }
        return response
    }

    override suspend fun addPostComment(postId: PostId, text: String): DomainResult<Comment> {
        val dto = AddCommentRequestDto(text = text)
        val response = safeCall { api.apiPostIdCommentPost(postId.value, dto) }
        return when (response) {
            is DomainResult.Success -> {
                val data = response.value.data
                    ?: return DomainResult.Failure(DomainError.Unknown())
                val id = data.id
                DomainResult.Success(
                    Comment(
                        id = id.toString(),
                        author = CommentAuthor(
                            id = UserId(UUID.randomUUID()),
                            credentials = "",
                        ),
                        text = text,
                        createdAt = java.time.OffsetDateTime.now(),
                        isPrivate = false,
                    ),
                )
            }
            is DomainResult.Failure -> response
        }
    }

    override suspend fun addSolutionComment(solutionId: SolutionId, text: String): DomainResult<Comment> {
        val dto = AddCommentRequestDto(text = text)
        val response = safeCall { api.apiSolutionIdCommentPost(solutionId.value, dto) }
        return when (response) {
            is DomainResult.Success -> {
                val data = response.value.data
                    ?: return DomainResult.Failure(DomainError.Unknown())
                val id = data.id
                DomainResult.Success(
                    Comment(
                        id = id.toString(),
                        author = CommentAuthor(
                            id = UserId(UUID.randomUUID()),
                            credentials = "",
                        ),
                        text = text,
                        createdAt = java.time.OffsetDateTime.now(),
                        isPrivate = false,
                    ),
                )
            }
            is DomainResult.Failure -> response
        }
    }

    override suspend fun getCommentReplies(commentId: CommentId): DomainResult<List<Comment>> {
        return safeCallList {
            api.apiCommentIdRepliesGet(UUID.fromString(commentId.value))
        }
    }

    override suspend fun addCommentReply(commentId: CommentId, text: String): DomainResult<Comment> {
        val dto = AddCommentRequestDto(text = text)
        val response: DomainResult<IdRequestDtoApiResponse> =
            safeCall { api.apiCommentIdReplyPost(UUID.fromString(commentId.value), dto) }
        return when (response) {
            is DomainResult.Success -> {
                val data = response.value.data
                    ?: return DomainResult.Failure(DomainError.Unknown())
                val id = data.id
                DomainResult.Success(
                    Comment(
                        id = id.toString(),
                        author = CommentAuthor(
                            id = UserId(UUID.randomUUID()),
                            credentials = "",
                        ),
                        text = text,
                        createdAt = java.time.OffsetDateTime.now(),
                        isPrivate = false,
                    ),
                )
            }
            is DomainResult.Failure -> response
        }
    }

    override suspend fun editComment(commentId: CommentId, text: String): DomainResult<Unit> {
        val dto = EditCommentRequestDto(text = text)
        val result =
            safeCall {
                api.apiCommentIdPut(
                    id = UUID.fromString(commentId.value),
                    editCommentRequestDto = dto,
                )
            }
        return when (result) {
            is DomainResult.Success -> DomainResult.Success(Unit)
            is DomainResult.Failure -> DomainResult.Failure(result.error)
        }
    }

    override suspend fun deleteComment(commentId: CommentId): DomainResult<Unit> =
        when (val result = safeCall { api.apiCommentIdDelete(UUID.fromString(commentId.value)) }) {
            is DomainResult.Success -> DomainResult.Success(Unit)
            is DomainResult.Failure -> DomainResult.Failure(result.error)
        }

    private suspend fun safeCallList(
        block: suspend () -> Response<CommentDtoListApiResponse>,
    ): DomainResult<List<Comment>> {
        val response = try {
            block()
        } catch (e: IOException) {
            return DomainResult.Failure(DomainError.Network(e))
        } catch (e: Exception) {
            return DomainResult.Failure(DomainError.Unknown(e))
        }

        if (!response.isSuccessful) {
            return DomainResult.Failure(httpCodeToDomainError(response.code()))
        }

        val body = response.body()
            ?: return DomainResult.Failure(DomainError.Unknown())

        if (body.type != ApiResponseType.success) {
            return DomainResult.Failure(DomainError.Unknown())
        }

        val data = body.data ?: emptyList()
        return DomainResult.Success(data.map { it.toDomain() })
    }

    private suspend fun <T> safeCall(block: suspend () -> Response<T>): DomainResult<T> {
        val response = try {
            block()
        } catch (e: IOException) {
            return DomainResult.Failure(DomainError.Network(e))
        } catch (e: Exception) {
            return DomainResult.Failure(DomainError.Unknown(e))
        }

        if (!response.isSuccessful) {
            return DomainResult.Failure(httpCodeToDomainError(response.code()))
        }

        val body = response.body()
            ?: return DomainResult.Failure(DomainError.Unknown())

        return DomainResult.Success(body)
    }

    private fun CommentDto.toDomain(): Comment =
        Comment(
            id = id.toString(),
            author = author.toDomain(),
            text = text,
            createdAt = java.time.OffsetDateTime.now(),
            isPrivate = false,
        )

    private fun CommentAuthorDto.toDomain(): CommentAuthor =
        CommentAuthor(
            id = UserId(id),
            credentials = credentials,
        )
}

