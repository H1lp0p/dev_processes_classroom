package com.stuf.domain.model

import com.stuf.grading.domain.model.SelfAssessmentDraft

/** Самооценка одного участника команды. */
data class MemberSelfAssessment(
    val userId: UserId,
    val credentials: String?,
    val evaluation: SelfAssessmentDraft?,
)
