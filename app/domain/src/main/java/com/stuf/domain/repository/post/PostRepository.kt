package com.stuf.domain.repository

import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.CourseId
import com.stuf.domain.model.Post
import com.stuf.domain.model.PostId

interface PostRepository {
    suspend fun getCourseFeed(
        courseId: CourseId,
        skip: Int = 0,
        take: Int = 10,
    ): DomainResult<List<Post>>

    suspend fun getPost(postId: PostId): DomainResult<Post>
    suspend fun createPost(courseId: CourseId, post: Post): DomainResult<Post>
    suspend fun updatePost(postId: PostId, post: Post): DomainResult<Post>
    suspend fun deletePost(postId: PostId): DomainResult<Unit>
}
