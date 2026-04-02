package com.stuf.data.repository

import com.stuf.data.api.PostApi
import com.stuf.data.common.httpCodeToDomainError
import com.stuf.domain.common.DomainError
import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.CourseId
import com.stuf.domain.model.Post
import com.stuf.domain.model.PostId
import com.stuf.domain.repository.PostRepository
import javax.inject.Inject
import retrofit2.Response
import java.io.IOException

class PostRepositoryImpl @Inject constructor(
    private val api: PostApi,
) : PostRepository {

    override suspend fun getCourseFeed(
        courseId: CourseId,
        skip: Int,
        take: Int,
    ): DomainResult<List<Post>> {
        val response = safeCall { api.apiCourseCourseIdFeedGet(courseId.value, skip, take) }
        return when (response) {
            is DomainResult.Success -> {
                val records = response.value.data?.records.orEmpty()
                val posts = records.map { mapCourseFeedItemToPost(it, courseId) }
                DomainResult.Success(posts)
            }
            is DomainResult.Failure -> response
        }
    }

    override suspend fun getPost(postId: PostId): DomainResult<Post> {
        val response = safeCall { api.apiPostIdGet(postId.value) }
        return when (response) {
            is DomainResult.Success -> {
                val dto = response.value.data
                    ?: return DomainResult.Failure(DomainError.Unknown())
                DomainResult.Success(mapPostDetailsDtoToPost(dto))
            }
            is DomainResult.Failure -> response
        }
    }

    override suspend fun createPost(courseId: CourseId, post: Post): DomainResult<Post> {
        val dto = post.toCreateUpdateDto()
        val response = safeCall { api.apiCourseCourseIdTaskPost(courseId.value, dto) }
        return when (response) {
            is DomainResult.Success -> {
                val data = response.value.data
                    ?: return DomainResult.Failure(DomainError.Unknown())
                val createdId = PostId(data.id)
                DomainResult.Success(
                    post.copy(id = createdId),
                )
            }
            is DomainResult.Failure -> response
        }
    }

    override suspend fun updatePost(postId: PostId, post: Post): DomainResult<Post> {
        val dto = post.toCreateUpdateDto()
        val response = safeCall { api.apiPostIdPut(postId.value, dto) }
        return when (response) {
            is DomainResult.Success -> {
                DomainResult.Success(post)
            }
            is DomainResult.Failure -> response
        }
    }

    override suspend fun deletePost(postId: PostId): DomainResult<Unit> {
        val response = safeCall { api.apiPostIdDelete(postId.value) }
        return when (response) {
            is DomainResult.Success -> DomainResult.Success(Unit)
            is DomainResult.Failure -> response
        }
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
}
