package com.stuf.classroom.navigation

import com.stuf.domain.model.CourseRole

fun CourseRole.toNavSegment(): String =
    when (this) {
        CourseRole.TEACHER -> "teacher"
        CourseRole.STUDENT -> "student"
    }
