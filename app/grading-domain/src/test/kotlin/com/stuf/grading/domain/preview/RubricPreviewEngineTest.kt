package com.stuf.grading.domain.preview

import com.stuf.grading.domain.model.CriterionDefinition
import com.stuf.grading.domain.model.CriterionDirection
import com.stuf.grading.domain.model.CriterionId
import com.stuf.grading.domain.model.SelfAssessmentDraft
import com.stuf.grading.domain.model.TaskGradingRubric
import kotlin.test.Test
import kotlin.test.assertEquals

class RubricPreviewEngineTest {

    @Test
    fun weightedOnly_sumOfScoreTimesWeight() {
        val w1 = CriterionId("w1")
        val w2 = CriterionId("w2")
        val rubric =
            TaskGradingRubric(
                taskId = "t1",
                title = "Test",
                assignmentMaxScore = 10.0,
                criteria =
                    listOf(
                        CriterionDefinition.Weighted(w1, "A", maxScore = 5.0, weight = 0.5),
                        CriterionDefinition.Weighted(w2, "B", maxScore = 5.0, weight = 0.5),
                    ),
            )
        val draft =
            SelfAssessmentDraft(
                weightedScores = mapOf(w1 to 4.0, w2 to 2.0),
            )
        val r = RubricPreviewEngine.preview(rubric, draft)
        assertEquals(4.0 * 0.5 + 2.0 * 0.5, r.finalScore, absoluteTolerance = 1e-9)
    }
}
