package com.stuf.data.demo

import com.stuf.domain.model.CourseId
import com.stuf.domain.model.PostId
import com.stuf.domain.model.SolutionId
import com.stuf.domain.model.UserId
import java.util.UUID

internal object DemoIds {
    val userStudent: UserId = UserId(UUID.fromString("00000000-0000-0000-0000-000000000001"))
    val userTeacher: UserId = UserId(UUID.fromString("00000000-0000-0000-0000-000000000002"))

    val courseAlgebra: CourseId = CourseId(UUID.fromString("11111111-1111-1111-1111-111111111101"))
    val courseWeb: CourseId = CourseId(UUID.fromString("22222222-2222-2222-2222-222222222202"))
    /** Курс, где демо-пользователь — преподаватель. */
    val courseTeacherClub: CourseId = CourseId(UUID.fromString("66666666-6666-6666-6666-666666666601"))

    val postWelcome: PostId = PostId(UUID.fromString("33333333-3333-3333-3333-333333333301"))
    val postHomework: PostId = PostId(UUID.fromString("33333333-3333-3333-3333-333333333302"))
    val postWebLab: PostId = PostId(UUID.fromString("44444444-4444-4444-4444-444444444403"))
    val postClubWelcome: PostId = PostId(UUID.fromString("77777777-7777-7777-7777-777777777701"))

    val solutionHomework: SolutionId = SolutionId(UUID.fromString("55555555-5555-5555-5555-555555555501"))
}
