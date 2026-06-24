package com.stuf.classroom.peerreview

import android.content.Context
import com.stuf.grading.domain.model.CriterionId
import com.stuf.grading.domain.model.SelfAssessmentDraft
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import org.json.JSONObject

@Singleton
class PeerReviewDraftStorage @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun load(storageKey: String): SavedPeerReviewDraft? {
        val raw = prefs.getString(storageKey, null) ?: return null
        return runCatching {
            val json = JSONObject(raw)
            val weighted = mutableMapOf<CriterionId, Double>()
            json.optJSONObject(KEY_WEIGHTED)?.let { obj ->
                obj.keys().forEach { key ->
                    weighted[CriterionId(key)] = obj.getDouble(key)
                }
            }
            val toggled = mutableMapOf<CriterionId, Boolean>()
            json.optJSONObject(KEY_TOGGLED)?.let { obj ->
                obj.keys().forEach { key ->
                    toggled[CriterionId(key)] = obj.getBoolean(key)
                }
            }
            SavedPeerReviewDraft(
                draft = SelfAssessmentDraft(weightedScores = weighted, toggledEnabled = toggled),
                wizardStepIndex = json.optInt(KEY_STEP, 0),
            )
        }.getOrNull()
    }

    fun save(storageKey: String, draft: SelfAssessmentDraft, wizardStepIndex: Int) {
        val weightedJson = JSONObject()
        draft.weightedScores.forEach { (id, score) -> weightedJson.put(id.value, score) }
        val toggledJson = JSONObject()
        draft.toggledEnabled.forEach { (id, enabled) -> toggledJson.put(id.value, enabled) }
        val payload =
            JSONObject()
                .put(KEY_STEP, wizardStepIndex)
                .put(KEY_WEIGHTED, weightedJson)
                .put(KEY_TOGGLED, toggledJson)
        prefs.edit().putString(storageKey, payload.toString()).apply()
    }

    fun clear(storageKey: String) {
        prefs.edit().remove(storageKey).apply()
    }

    fun storageKey(taskId: String, assignmentKey: String): String = "${taskId}_$assignmentKey"

    data class SavedPeerReviewDraft(
        val draft: SelfAssessmentDraft,
        val wizardStepIndex: Int,
    )

    companion object {
        private const val PREFS_NAME = "peer_review_drafts"
        private const val KEY_STEP = "wizardStepIndex"
        private const val KEY_WEIGHTED = "weightedScores"
        private const val KEY_TOGGLED = "toggledEnabled"
    }
}
