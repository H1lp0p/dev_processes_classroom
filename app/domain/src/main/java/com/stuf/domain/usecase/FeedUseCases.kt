package com.stuf.domain.usecase

import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.CourseId
import com.stuf.domain.model.Post
import com.stuf.domain.model.PostId

interface GetCourseFeed {
    suspend operator fun invoke(courseId: CourseId, skip: Int = 0, take: Int = 10): DomainResult<List<Post>>
}

interface GetPost {
    suspend operator fun invoke(postId: PostId): DomainResult<Post>
}

interface CreatePost {
    suspend operator fun invoke(courseId: CourseId, post: Post): DomainResult<Post>
}

interface UpdatePost {
    suspend operator fun invoke(postId: PostId, post: Post): DomainResult<Post>
}

interface DeletePost {
    suspend operator fun invoke(postId: PostId): DomainResult<Unit>
}
