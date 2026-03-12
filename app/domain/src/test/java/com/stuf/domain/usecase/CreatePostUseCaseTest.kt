package com.stuf.domain.usecase

import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.CourseId
import com.stuf.domain.model.Post
import com.stuf.domain.model.PostId
import com.stuf.domain.model.PostKind
import com.stuf.domain.repository.PostRepository
import com.stuf.domain.usecase.impl.CreatePostUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.OffsetDateTime
import java.util.UUID

private class FakeCreatePostRepository : PostRepository {
    var lastCreatePostArgs: Pair<CourseId, Post>? = null

    override suspend fun getCourseFeed(
        courseId: CourseId,
        skip: Int,
        take: Int,
    ): DomainResult<List<Post>> = error("Not needed in this fake")

    override suspend fun getPost(postId: PostId): DomainResult<Post> {
        error("Not needed in this fake")
    }

    override suspend fun createPost(courseId: CourseId, post: Post): DomainResult<Post> {
        lastCreatePostArgs = Pair(courseId, post)
        return DomainResult.Success(
            post.copy(id = PostId(UUID.randomUUID())),
        )
    }

    override suspend fun updatePost(postId: PostId, post: Post): DomainResult<Post> {
        error("Not needed in this fake")
    }

    override suspend fun deletePost(postId: PostId): DomainResult<Unit> {
        error("Not needed in this fake")
    }
}

class CreatePostUseCaseTest {

    @Test
    fun `delegates to repository and returns created post`() {
        val repo = FakeCreatePostRepository()
        val useCase : CreatePost = CreatePostUseCase(repo)
        val courseId = CourseId(UUID.randomUUID())
        val post = Post(
            id = PostId(UUID.randomUUID()),
            courseId = courseId,
            kind = PostKind.ANNOUNCEMENT,
            title = "Title",
            text = "Text",
            createdAt = OffsetDateTime.now(),
            taskDetails = null,
        )

        val result = kotlinx.coroutines.runBlocking {
            useCase(courseId, post)
        }

        assertTrue(result is DomainResult.Success<*>)
        assertEquals(courseId, repo.lastCreatePostArgs?.first)
        assertEquals(post, repo.lastCreatePostArgs?.second)
    }
}
