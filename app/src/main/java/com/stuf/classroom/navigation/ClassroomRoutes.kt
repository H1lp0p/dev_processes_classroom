package com.stuf.classroom.navigation

import com.stuf.domain.model.CourseId
import com.stuf.domain.model.CourseRole
import com.stuf.domain.model.PostId
import com.stuf.domain.model.TeamId

object ClassroomRoutes {
    const val LOADING = "loading"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val CREATE_COURSE = "createCourse"
    const val JOIN_COURSE = "joinCourse"

    const val COURSE = "course/{courseId}/{role}"
    const val POST = "post/{postId}/{role}"
    const val GRADE_DISTRIBUTION = "gradeDistribution/{teamId}/{postId}/{role}"

    fun course(courseId: CourseId, role: CourseRole): String =
        "course/${courseId.value}/${role.toNavSegment()}"

    fun post(postId: PostId, role: CourseRole): String =
        "post/${postId.value}/${role.toNavSegment()}"

    fun gradeDistribution(teamId: TeamId, postId: PostId, role: CourseRole): String =
        "gradeDistribution/${teamId.value}/${postId.value}/${role.toNavSegment()}"
}
