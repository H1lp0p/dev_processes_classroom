package com.stuf.domain.usecase

import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.Course

interface CreateCourse {
    suspend operator fun invoke(title: String): DomainResult<Course>
}
