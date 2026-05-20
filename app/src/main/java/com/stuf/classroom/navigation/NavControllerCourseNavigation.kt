package com.stuf.classroom.navigation

import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import com.stuf.domain.model.CourseId
import com.stuf.domain.model.CourseRole

fun NavController.navigateToCourse(
    courseId: CourseId,
    role: CourseRole,
    builder: NavOptionsBuilder.() -> Unit = {},
) {
    navigate(ClassroomRoutes.course(courseId, role), builder)
}
