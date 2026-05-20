package com.stuf.domain.usecase

import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.AnnouncementPost
import com.stuf.domain.model.CourseId
import com.stuf.domain.model.Post
import com.stuf.domain.model.PostId
import com.stuf.domain.repository.PostRepository
import com.stuf.domain.usecase.impl.UpdatePostUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.OffsetDateTime
import java.util.UUID

private class FakeUpdatePostRepository : PostRepository {
    var lastUpdatePostArgs: Pair<PostId, Post>? = null

    override suspend fun getCourseFeed(
        courseId: CourseId,
        skip: Int,
        take: Int,
    ): DomainResult<List<Post>> = error("Not needed in this fake")

    override suspend fun getPost(postId: PostId): DomainResult<Post> {
        error("Not needed in this fake")
    }

    override suspend fun createPost(courseId: CourseId, post: Post): DomainResult<Post> {
        error("Not needed in this fake")
    }

    override suspend fun updatePost(postId: PostId, post: Post): DomainResult<Post> {
        lastUpdatePostArgs = Pair(postId, post)
        return DomainResult.Success(post)
    }

    override suspend fun deletePost(postId: PostId): DomainResult<Unit> {
        error("Not needed in this fake")
    }
}

class UpdatePostUseCaseTest {

    @Test
    fun `delegates to repository and returns updated post`() {
        val repo = FakeUpdatePostRepository()
        val useCase : UpdatePost = UpdatePostUseCase(repo)
        val postId = PostId(UUID.randomUUID())
        val post =
            AnnouncementPost(
                id = postId,
                courseId = CourseId(UUID.randomUUID()),
                title = "Updated",
                text = "Body",
                createdAt = OffsetDateTime.now(),
            )

        val result = kotlinx.coroutines.runBlocking {
            useCase(postId, post)
        }

        assertTrue(result is DomainResult.Success<*>)
        assertEquals(postId, repo.lastUpdatePostArgs?.first)
        assertEquals(post, repo.lastUpdatePostArgs?.second)
    }
}
