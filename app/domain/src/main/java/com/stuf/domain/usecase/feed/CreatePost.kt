package com.stuf.domain.usecase

import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.CourseId
import com.stuf.domain.model.Post

interface CreatePost {
    suspend operator fun invoke(courseId: CourseId, post: Post): DomainResult<Post>
}
