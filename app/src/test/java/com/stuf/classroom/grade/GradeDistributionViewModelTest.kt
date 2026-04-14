package com.stuf.classroom.grade

import com.stuf.domain.model.UserId
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class GradeDistributionViewModelTest {

    private val userA = UserId(UUID.randomUUID())
    private val userB = UserId(UUID.randomUUID())

    @Test
    fun `remainder becomes negative when sum exceeds rraw`() {
        val st =
            GradeDistributionUiState(
                isLoading = false,
                teamRawScore = 10.0,
                members =
                    listOf(
                        GradeDistributionMemberRow(userA, "A"),
                        GradeDistributionMemberRow(userB, "B"),
                    ),
                draftPoints =
                    mapOf(
                        userA.value.toString() to "6",
                        userB.value.toString() to "5",
                    ),
                remainder = -1.0,
                remainderNegative = true,
                isCaptain = true,
            )
        assertTrue(st.remainderNegative)
    }

    @Test
    fun `remainder ok when sum equals rraw`() {
        val st =
            GradeDistributionUiState(
                isLoading = false,
                teamRawScore = 10.0,
                members =
                    listOf(
                        GradeDistributionMemberRow(userA, "A"),
                        GradeDistributionMemberRow(userB, "B"),
                    ),
                draftPoints =
                    mapOf(
                        userA.value.toString() to "6",
                        userB.value.toString() to "4",
                    ),
                remainder = 0.0,
                remainderNegative = false,
                isCaptain = true,
            )
        assertFalse(st.remainderNegative)
    }
}
