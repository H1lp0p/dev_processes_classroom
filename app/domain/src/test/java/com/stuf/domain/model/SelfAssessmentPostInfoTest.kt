package com.stuf.domain.model

import com.stuf.grading.domain.model.TaskGradingRubric
import java.time.OffsetDateTime
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SelfAssessmentPostInfoTest {

    private val rubric =
        TaskGradingRubric(
            taskId = "t1",
            title = "Rubric",
            assignmentMaxScore = 5.0,
            studentScoreWeight = 0.2,
            criteria = emptyList(),
        )

    @Test
    fun open_whenDeadlineInFuture() {
        val now = OffsetDateTime.parse("2026-05-20T12:00:00Z")
        val deadline = now.plusDays(1)
        assertTrue(
            isSelfAssessmentOpen(deadline, solvableAfterDeadline = false, now),
        )
    }

    @Test
    fun closed_afterDeadlineUnlessSolvableAfter() {
        val now = OffsetDateTime.parse("2026-05-20T12:00:00Z")
        val deadline = now.minusHours(1)
        assertFalse(
            isSelfAssessmentOpen(deadline, solvableAfterDeadline = false, now),
        )
        assertTrue(
            isSelfAssessmentOpen(deadline, solvableAfterDeadline = true, now),
        )
    }

    @Test
    fun notConfigured_whenNoRubric() {
        val info =
            buildSelfAssessmentPostInfo(
                gradingRubric = null,
                deadline = OffsetDateTime.now().plusDays(1),
                solvableAfterDeadline = false,
                now = OffsetDateTime.now(),
            )
        assertFalse(info.isConfigured)
    }

    @Test
    fun configured_whenRubricPresent() {
        val info =
            buildSelfAssessmentPostInfo(
                gradingRubric = rubric,
                deadline = OffsetDateTime.now().plusDays(1),
                solvableAfterDeadline = false,
                now = OffsetDateTime.now(),
            )
        assertTrue(info.isConfigured)
        assertTrue(info.isCurrentlyOpen)
    }
}
