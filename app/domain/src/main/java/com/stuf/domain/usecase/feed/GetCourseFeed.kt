package com.stuf.domain.usecase

import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.CourseId
import com.stuf.domain.model.Post

interface GetCourseFeed {
    suspend operator fun invoke(courseId: CourseId, skip: Int = 0, take: Int = 10): DomainResult<List<Post>>
}
