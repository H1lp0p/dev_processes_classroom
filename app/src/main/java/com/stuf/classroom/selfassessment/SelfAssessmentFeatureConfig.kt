package com.stuf.classroom.selfassessment

/**
 * Флаги функционала самооценки / командной рефлексии (временно выключает форму в UI).
 */
data class SelfAssessmentFeatureConfig(
    /** Когда true — показывается полноценная форма самооценки; иначе — заглушка. */
    val enableTeamReflexyScore: Boolean = false,
)
