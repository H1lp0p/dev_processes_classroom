package com.stuf.data.infrastructure

import com.stuf.data.model.PostDetailsDtoApiResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class PostDetailsJsonParseTest {

    @Test
    fun `parses team task post with camelCase gradingMode from backend`() {
        val json =
            """
            {"type":"success","message":null,"data":{"id":"3b2d15b0-b71d-4c10-836c-b9138b1eddd8","type":"teaM_TASK","title":"54456456","text":"4545645656","deadline":"2026-05-15T10:04:00","maxScore":5,"taskType":null,"solvableAfterDeadline":false,"files":[],"userSolution":null,"minTeamSize":2,"maxTeamSize":5,"teamSolution":null,"captainMode":"firstMember","votingDurationHours":24,"predefinedTeamsCount":5,"allowJoinTeam":true,"allowLeaveTeam":true,"allowStudentTransferCaptain":true,"failThreshold":null,"successThreshold":null,"studentScoreWeight":0,"penaltyPerDay":null,"maxDays":0,"gradingMode":"teacherReview","minPeerReviewsRequired":null,"criteria":[{"id":"87529c9e-4b43-432c-af9a-3aa8feaea3dc","type":"weighted","title":"Функциональность","orderIndex":0,"maxScore":5,"weight":1,"threshold":null,"score":null,"direction":null,"maxAllowedScore":null}]}}
            """.trimIndent()

        val parsed =
            Serializer.moshi
                .adapter(PostDetailsDtoApiResponse::class.java)
                .fromJson(json)

        assertNotNull(parsed)
        assertEquals(com.stuf.data.model.GradingMode.TeacherReview, parsed?.data?.gradingMode)
        assertEquals(1, parsed?.data?.criteria?.size)
    }
}
