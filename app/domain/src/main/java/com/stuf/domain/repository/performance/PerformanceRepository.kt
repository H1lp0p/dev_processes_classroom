package com.stuf.domain.repository

import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.CourseId
import com.stuf.domain.model.GradeTable
import java.time.OffsetDateTime

interface PerformanceRepository {
    suspend fun getPerformanceTable(
        courseId: CourseId,
        from: OffsetDateTime? = null,
        to: OffsetDateTime? = null,
        onlyMandatory: Boolean = false,
    ): DomainResult<GradeTable>
}
