package com.stuf.domain.usecase

import com.stuf.domain.common.DomainError
import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.*
import com.stuf.domain.repository.PerformanceRepository
import com.stuf.domain.usecase.impl.GetPerformanceTableUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

private class FakePerformanceRepository(
    private val table: GradeTable?,
    private val error: DomainError? = null,
) : PerformanceRepository {
    override suspend fun getPerformanceTable(
        courseId: CourseId,
        from: java.time.OffsetDateTime?,
        to: java.time.OffsetDateTime?,
        onlyMandatory: Boolean,
    ): DomainResult<GradeTable> {
        return if (error != null || table == null) {
            DomainResult.Failure(error ?: DomainError.Unknown(null))
        } else {
            DomainResult.Success(table)
        }
    }
}

private class RecordingPerformanceRepository(
    private val table: GradeTable,
) : PerformanceRepository {
    var lastFrom: java.time.OffsetDateTime? = null
    var lastTo: java.time.OffsetDateTime? = null
    var lastOnlyMandatory: Boolean? = null

    override suspend fun getPerformanceTable(
        courseId: CourseId,
        from: java.time.OffsetDateTime?,
        to: java.time.OffsetDateTime?,
        onlyMandatory: Boolean,
    ): DomainResult<GradeTable> {
        lastFrom = from
        lastTo = to
        lastOnlyMandatory = onlyMandatory
        return DomainResult.Success(table)
    }
}

class GetPerformanceTableUseCaseTest {

    @Test
    fun `returns table from repository unchanged on success`() {
        val student = CourseMember(
            id = UserId(UUID.randomUUID()),
            credentials = "Student",
            email = "s@example.com",
            role = CourseRole.STUDENT,
        )
        val taskId = TaskId(UUID.randomUUID())
        val gradeCell = GradeCell(GradeStatus.GRADED, Score(5))
        val table = GradeTable(
            tasks = listOf(taskId),
            rows = listOf(
                GradeRow(
                    student = student,
                    cells = mapOf(taskId to gradeCell),
                    averageScore = 5.0,
                )
            ),
        )
        val repo = FakePerformanceRepository(table = table)
        val useCase : GetPerformanceTable = GetPerformanceTableUseCase(repo)

        val result = kotlinx.coroutines.runBlocking {
            useCase(CourseId(UUID.randomUUID()), PerformanceFilters())
        }

        assertTrue(result is DomainResult.Success<*>)
        val value = (result as DomainResult.Success<GradeTable>).value
        assertEquals(table, value)
    }

    @Test
    fun `returns not found error when repository returns NotFound`() {
        val repo = FakePerformanceRepository(
            table = null,
            error = DomainError.NotFound,
        )
        val useCase : GetPerformanceTable = GetPerformanceTableUseCase(repo)

        val result = kotlinx.coroutines.runBlocking {
            useCase(CourseId(UUID.randomUUID()), PerformanceFilters())
        }

        assertTrue(result is DomainResult.Failure)
        val error = (result as DomainResult.Failure).error
        assertEquals(DomainError.NotFound, error)
    }

    @Test
    fun `passes PerformanceFilters from to and onlyMandatory to repository`() {
        val table = GradeTable(
            tasks = emptyList(),
            rows = emptyList(),
        )
        val repo = RecordingPerformanceRepository(table)
        val useCase : GetPerformanceTable = GetPerformanceTableUseCase(repo)
        val from = java.time.OffsetDateTime.now().minusDays(7)
        val to = java.time.OffsetDateTime.now()
        val filters = PerformanceFilters(
            query = "ignored",
            from = from,
            to = to,
            onlyMandatory = true,
        )

        kotlinx.coroutines.runBlocking {
            useCase(CourseId(UUID.randomUUID()), filters)
        }

        assertEquals(from, repo.lastFrom)
        assertEquals(to, repo.lastTo)
        assertEquals(true, repo.lastOnlyMandatory)
    }
}

