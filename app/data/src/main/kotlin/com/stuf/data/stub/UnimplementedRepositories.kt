package com.stuf.data.stub

import com.stuf.domain.common.DomainError
import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.CourseId
import com.stuf.domain.model.FileInfo
import com.stuf.domain.model.GradeTable
import com.stuf.domain.repository.FileRepository
import com.stuf.domain.repository.PerformanceRepository
import java.time.OffsetDateTime
import javax.inject.Inject

/**
 * Плейсхолдеры для api-сборки, пока нет сетевых реализаций File / Performance.
 */
class UnimplementedFileRepository @Inject constructor() : FileRepository {
    override suspend fun uploadFile(bytes: ByteArray, name: String): DomainResult<FileInfo> =
        DomainResult.Failure(DomainError.Unknown())
}

class UnimplementedPerformanceRepository @Inject constructor() : PerformanceRepository {
    override suspend fun getPerformanceTable(
        courseId: CourseId,
        from: OffsetDateTime?,
        to: OffsetDateTime?,
        onlyMandatory: Boolean,
    ): DomainResult<GradeTable> =
        DomainResult.Failure(DomainError.Unknown())
}
