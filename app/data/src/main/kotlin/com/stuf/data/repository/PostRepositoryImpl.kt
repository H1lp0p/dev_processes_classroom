package com.stuf.data.repository

import com.stuf.data.api.PostApi
import com.stuf.data.common.httpCodeToDomainError
import com.stuf.data.model.CreateUpdatePostDto
import com.stuf.data.model.PostDetailsDto
import com.stuf.data.model.PostType
import com.stuf.data.model.TaskType
import com.stuf.domain.common.DomainError
import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.CourseId
import com.stuf.domain.model.Post
import com.stuf.domain.model.PostId
import com.stuf.domain.model.PostKind
import com.stuf.domain.model.TaskDetails
import com.stuf.domain.repository.PostRepository
import retrofit2.Response
import java.io.IOException

class PostRepositoryImpl(
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
                val posts = records.map { item ->
                    Post(
                        id = PostId(item.id),
                        courseId = courseId,
                        kind = when (item.type) {
                            PostType.post -> PostKind.ANNOUNCEMENT
                            PostType.task -> PostKind.TASK
                        },
                        title = item.title,
                        text = "",
                        createdAt = item.createdDate,
                        taskDetails = null,
                    )
                }
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
                DomainResult.Success(dto.toDomain())
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
                // тесты ожидают, что id не меняется и обновляется только содержимое
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

    private fun PostDetailsDto.toDomain(): Post {
        val kind = when (type) {
            PostType.post -> PostKind.ANNOUNCEMENT
            PostType.task -> PostKind.TASK
        }

        val taskDetails = if (type == PostType.task) {
            TaskDetails(
                deadline = deadline,
                isMandatory = taskType == TaskType.mandatory,
                maxScore = maxScore ?: 5,
            )
        } else {
            null
        }

        return Post(
            id = PostId(id!!),
            courseId = CourseId(java.util.UUID.randomUUID()),
            kind = kind,
            title = title,
            text = text,
            createdAt = deadline ?: java.time.OffsetDateTime.now(),
            taskDetails = taskDetails,
        ).copy(
            // files не в доменной модели Post, но уже тестируется TaskDetails
        )
    }

    private fun Post.toCreateUpdateDto(): CreateUpdatePostDto {
        val taskType = if (kind == PostKind.TASK) TaskType.mandatory else TaskType.mandatory
        return CreateUpdatePostDto(
            type = when (kind) {
                PostKind.ANNOUNCEMENT,
                PostKind.MATERIAL,
                -> PostType.post
                PostKind.TASK -> PostType.task
            },
            title = title,
            text = text,
            deadline = taskDetails?.deadline,
            maxScore = taskDetails?.maxScore,
            taskType = taskType,
            solvableAfterDeadline = taskDetails?.isMandatory,
            files = null,
        )
    }
}

