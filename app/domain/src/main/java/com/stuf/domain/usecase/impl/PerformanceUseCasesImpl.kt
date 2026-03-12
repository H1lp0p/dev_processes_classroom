package com.stuf.domain.usecase.impl

import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.CourseId
import com.stuf.domain.model.GradeTable
import com.stuf.domain.repository.PerformanceRepository
import com.stuf.domain.usecase.GetPerformanceTable
import com.stuf.domain.usecase.PerformanceFilters

class GetPerformanceTableUseCase(
    private val repository: PerformanceRepository,
) : GetPerformanceTable {

    override suspend fun invoke(
        courseId: CourseId,
        filters: PerformanceFilters,
    ): DomainResult<GradeTable> {
        return repository.getPerformanceTable(
            courseId = courseId,
            from = filters.from,
            to = filters.to,
            onlyMandatory = filters.onlyMandatory,
        )
    }
}

