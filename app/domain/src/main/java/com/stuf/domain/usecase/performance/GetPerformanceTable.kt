package com.stuf.domain.usecase

import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.CourseId
import com.stuf.domain.model.GradeTable

interface GetPerformanceTable {
    suspend operator fun invoke(
        courseId: CourseId,
        filters: PerformanceFilters,
    ): DomainResult<GradeTable>
}
