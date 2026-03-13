package com.stuf.data.repository

import com.stuf.data.api.PostApi
import com.stuf.data.model.ApiResponseType
import com.stuf.data.model.CreateUpdatePostDto
import com.stuf.data.model.FeedResponseDto
import com.stuf.data.model.FeedResponseDtoApiResponse
import com.stuf.data.model.FileDto
import com.stuf.data.model.IdRequestDto
import com.stuf.data.model.IdRequestDtoApiResponse
import com.stuf.data.model.PostDetailsDto
import com.stuf.data.model.PostDetailsDtoApiResponse
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
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import java.time.OffsetDateTime
import java.util.UUID

private class FakePostApi : PostApi {
    var lastFeedCourseId: UUID? = null
    var lastFeedSkip: Int? = null
    var lastFeedTake: Int? = null

    var lastCreatedCourseId: UUID? = null
    var lastCreatedDto: CreateUpdatePostDto? = null

    var lastUpdatedPostId: UUID? = null
    var lastUpdatedDto: CreateUpdatePostDto? = null

    var lastDeletedPostId: UUID? = null
    var lastGetPostId: UUID? = null

    var feedResponse: Response<FeedResponseDtoApiResponse>? = null
    var createPostResponse: Response<IdRequestDtoApiResponse>? = null
    var getPostResponse: Response<PostDetailsDtoApiResponse>? = null
    var updatePostResponse: Response<IdRequestDtoApiResponse>? = null
    var deletePostResponse: Response<IdRequestDtoApiResponse>? = null

    override suspend fun apiCourseCourseIdFeedGet(
        courseId: UUID,
        skip: Int?,
        take: Int?,
    ): Response<FeedResponseDtoApiResponse> {
        lastFeedCourseId = courseId
        lastFeedSkip = skip
        lastFeedTake = take
        return requireNotNull(feedResponse)
    }

    override suspend fun apiCourseCourseIdTaskPost(
        courseId: UUID,
        createUpdatePostDto: CreateUpdatePostDto?,
    ): Response<IdRequestDtoApiResponse> {
        lastCreatedCourseId = courseId
        lastCreatedDto = createUpdatePostDto
        return requireNotNull(createPostResponse)
    }

    override suspend fun apiPostIdDelete(id: UUID): Response<IdRequestDtoApiResponse> {
        lastDeletedPostId = id
        return requireNotNull(deletePostResponse)
    }

    override suspend fun apiPostIdGet(id: UUID): Response<PostDetailsDtoApiResponse> {
        lastGetPostId = id
        return requireNotNull(getPostResponse)
    }

    override suspend fun apiPostIdPut(
        id: UUID,
        createUpdatePostDto: CreateUpdatePostDto?,
    ): Response<IdRequestDtoApiResponse> {
        lastUpdatedPostId = id
        lastUpdatedDto = createUpdatePostDto
        return requireNotNull(updatePostResponse)
    }
}

class PostRepositoryImplTest {

    @Test
    fun `getCourseFeed success maps posts and passes pagination`() {
        val courseId = UUID.fromString("00000000-0000-0000-0000-000000000001")
        val api = FakePostApi().apply {
            feedResponse = Response.success(
                FeedResponseDtoApiResponse(
                    type = ApiResponseType.success,
                    message = null,
                    data = FeedResponseDto(
                        records = listOf(
                            com.stuf.data.model.CourseFeedItemDto(
                                id = UUID.fromString("00000000-0000-0000-0000-000000000010"),
                                type = PostType.post,
                                title = "Announcement",
                                createdDate = OffsetDateTime.parse("2024-01-01T10:00:00Z"),
                            ),
                        ),
                        totalRecords = 1,
                    ),
                ),
            )
        }
        val repository: PostRepository = PostRepositoryImpl(api)

        val result = runBlocking {
            repository.getCourseFeed(
                courseId = CourseId(courseId),
                skip = 5,
                take = 10,
            )
        }

        assertTrue(result is DomainResult.Success<List<Post>>)
        val posts = (result as DomainResult.Success<List<Post>>).value
        assertEquals(1, posts.size)
        val post = posts.first()
        assertEquals(PostId(UUID.fromString("00000000-0000-0000-0000-000000000010")), post.id)
        assertEquals(CourseId(courseId), post.courseId)
        assertEquals(PostKind.ANNOUNCEMENT, post.kind)
        assertEquals("Announcement", post.title)
        assertEquals(5, api.lastFeedSkip)
        assertEquals(10, api.lastFeedTake)
        assertEquals(courseId, api.lastFeedCourseId)
    }

    @Test
    fun `getCourseFeed http 401 returns Unauthorized`() {
        val courseId = UUID.randomUUID()
        val api = FakePostApi().apply {
            val body = ResponseBody.create("application/json".toMediaType(), "{}")
            feedResponse = Response.error(401, body)
        }
        val repository: PostRepository = PostRepositoryImpl(api)

        val result = runBlocking {
            repository.getCourseFeed(CourseId(courseId))
        }

        assertTrue(result is DomainResult.Failure)
        assertEquals(DomainError.Unauthorized, (result as DomainResult.Failure).error)
    }

    @Test
    fun `getPost success maps details including taskDetails and files`() {
        val postId = UUID.fromString("00000000-0000-0000-0000-000000000020")
        val api = FakePostApi().apply {
            getPostResponse = Response.success(
                PostDetailsDtoApiResponse(
                    type = ApiResponseType.success,
                    message = null,
                    data = PostDetailsDto(
                        id = postId,
                        type = PostType.task,
                        title = "Task title",
                        text = "Solve this",
                        deadline = OffsetDateTime.parse("2024-01-10T12:00:00Z"),
                        maxScore = 10,
                        taskType = TaskType.mandatory,
                        solvableAfterDeadline = true,
                        files = listOf(
                            FileDto(
                                id = "file-id-1",
                                name = "file.txt",
                            ),
                        ),
                        userSolution = null,
                    ),
                ),
            )
        }
        val repository: PostRepository = PostRepositoryImpl(api)

        val result = runBlocking {
            repository.getPost(PostId(postId))
        }

        assertTrue(result is DomainResult.Success<Post>)
        val post = (result as DomainResult.Success<Post>).value
        assertEquals(PostId(postId), post.id)
        assertEquals(PostKind.TASK, post.kind)
        assertEquals("Task title", post.title)
        assertEquals("Solve this", post.text)
        val taskDetails: TaskDetails? = post.taskDetails
        assertEquals(OffsetDateTime.parse("2024-01-10T12:00:00Z"), taskDetails?.deadline)
        assertEquals(true, taskDetails?.isMandatory)
        assertEquals(10, taskDetails?.maxScore)
    }

    @Test
    fun `createPost success returns created post with id`() {
        val courseId = UUID.fromString("00000000-0000-0000-0000-000000000030")
        val api = FakePostApi().apply {
            createPostResponse = Response.success(
                IdRequestDtoApiResponse(
                    type = ApiResponseType.success,
                    message = null,
                    data = IdRequestDto(
                        id = UUID.fromString("00000000-0000-0000-0000-000000000031"),
                    ),
                ),
            )
        }
        val repository: PostRepository = PostRepositoryImpl(api)

        val domainPost = Post(
            id = PostId(UUID.fromString("00000000-0000-0000-0000-000000000000")),
            courseId = CourseId(courseId),
            kind = PostKind.TASK,
            title = "New Task",
            text = "Do it",
            createdAt = OffsetDateTime.parse("2024-01-01T00:00:00Z"),
            taskDetails = TaskDetails(
                deadline = OffsetDateTime.parse("2024-01-05T00:00:00Z"),
                isMandatory = true,
                maxScore = 5,
            ),
        )

        val result = runBlocking {
            repository.createPost(CourseId(courseId), domainPost)
        }

        assertTrue(result is DomainResult.Success<Post>)
        val created = (result as DomainResult.Success<Post>).value
        assertEquals(PostId(UUID.fromString("00000000-0000-0000-0000-000000000031")), created.id)

        assertEquals(courseId, api.lastCreatedCourseId)
        assertEquals("New Task", api.lastCreatedDto?.title)
        assertEquals(PostType.task, api.lastCreatedDto?.type)
        assertEquals("Do it", api.lastCreatedDto?.text)
        assertEquals(5, api.lastCreatedDto?.maxScore)
        assertEquals(TaskType.mandatory, api.lastCreatedDto?.taskType)
    }

    @Test
    fun `updatePost success returns updated post`() {
        val postId = UUID.fromString("00000000-0000-0000-0000-000000000040")
        val api = FakePostApi().apply {
            updatePostResponse = Response.success(
                IdRequestDtoApiResponse(
                    type = ApiResponseType.success,
                    message = null,
                    data = IdRequestDto(
                        id = postId,
                    ),
                ),
            )
        }
        val repository: PostRepository = PostRepositoryImpl(api)

        val originalPost = Post(
            id = PostId(postId),
            courseId = CourseId(UUID.randomUUID()),
            kind = PostKind.ANNOUNCEMENT,
            title = "Old title",
            text = "Old text",
            createdAt = OffsetDateTime.parse("2024-01-01T00:00:00Z"),
            taskDetails = null,
        )

        val result = runBlocking {
            repository.updatePost(PostId(postId), originalPost.copy(title = "New title"))
        }

        assertTrue(result is DomainResult.Success<Post>)
        val updated = (result as DomainResult.Success<Post>).value
        assertEquals(PostId(postId), updated.id)
        assertEquals("New title", updated.title)

        assertEquals(postId, api.lastUpdatedPostId)
        assertEquals("New title", api.lastUpdatedDto?.title)
    }

    @Test
    fun `deletePost success returns Unit`() {
        val postId = UUID.fromString("00000000-0000-0000-0000-000000000050")
        val api = FakePostApi().apply {
            deletePostResponse = Response.success(
                IdRequestDtoApiResponse(
                    type = ApiResponseType.success,
                    message = null,
                    data = IdRequestDto(id = postId),
                ),
            )
        }
        val repository: PostRepository = PostRepositoryImpl(api)

        val result = runBlocking {
            repository.deletePost(PostId(postId))
        }

        assertTrue(result is DomainResult.Success<Unit>)
        assertEquals(postId, api.lastDeletedPostId)
    }
}

