package com.stuf.domain.bdd.support

import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.TaskId
import com.stuf.domain.model.TaskPost
import kotlinx.coroutines.runBlocking

object BddTestHelpers {

    fun completeIndividualReviews(studentName: String, count: Int) {
        BddWorld.backend.submitIndividualSolution(studentName)
        repeat(count) { i ->
            BddWorld.backend.submitIndividualSolution("Другой$i")
            runBlocking {
                BddWorld.backend.loginStudent(studentName)
                val task = BddWorld.backend.findCurrentTask() as TaskPost
                val next = BddWorld.harness.getNextPeerReview(TaskId(task.id.value))
                if (next is DomainResult.Success && next.value != null) {
                    BddWorld.harness.submitPeerReview(
                        next.value.reviewId,
                        com.stuf.grading.domain.model.SelfAssessmentDraft(),
                    )
                }
            }
        }
    }
}
