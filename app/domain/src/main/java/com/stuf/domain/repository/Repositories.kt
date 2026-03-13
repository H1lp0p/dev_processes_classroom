package com.stuf.domain.repository

import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.*
import java.time.OffsetDateTime

interface AuthRepository {
    suspend fun login(email: String, password: String): DomainResult<AuthSession>
    suspend fun register(credentials: String, email: String, password: String): DomainResult<AuthSession>
    suspend fun refresh(): DomainResult<AuthSession>
}

data class AuthSession(
    val accessToken: String,
    val refreshToken: String,
)

interface CourseRepository {
    suspend fun getUserCourses(): DomainResult<List<UserCourse>>
    suspend fun createCourse(title: String): DomainResult<Course>
    suspend fun joinCourse(inviteCode: String): DomainResult<Course>
    suspend fun getCourseInfo(courseId: CourseId): DomainResult<Course>
    suspend fun getCourseMembers(
        courseId: CourseId,
        query: String? = null,
    ): DomainResult<List<CourseMember>>

    suspend fun changeMemberRole(courseId: CourseId, userId: UserId, newRole: CourseRole): DomainResult<Unit>
    suspend fun removeMember(courseId: CourseId, userId: UserId): DomainResult<Unit>
    suspend fun leaveCourse(courseId: CourseId): DomainResult<Unit>
}

interface PostRepository {
    suspend fun getCourseFeed(
        courseId: CourseId,
        skip: Int = 0,
        take: Int = 10,
    ): DomainResult<List<Post>>

    suspend fun getPost(postId: PostId): DomainResult<Post>
    suspend fun createPost(courseId: CourseId, post: Post): DomainResult<Post>
    suspend fun updatePost(postId: PostId, post: Post): DomainResult<Post>
    suspend fun deletePost(postId: PostId): DomainResult<Unit>
}

interface SolutionRepository {
    suspend fun submitSolution(taskId: TaskId, text: String?, fileIds: List<String>): DomainResult<Solution>
    suspend fun cancelSolution(taskId: TaskId): DomainResult<Unit>
    suspend fun getUserSolution(taskId: TaskId): DomainResult<Solution?>
    suspend fun getTaskSolutions(
        taskId: TaskId,
        status: SolutionStatus? = null,
        studentId: UserId? = null,
    ): DomainResult<List<Solution>>

    suspend fun reviewSolution(solutionId: SolutionId, review: Review): DomainResult<Unit>
}

interface CommentRepository {
    suspend fun getPostComments(postId: PostId): DomainResult<List<Comment>>
    suspend fun getSolutionComments(solutionId: SolutionId): DomainResult<List<Comment>>
    suspend fun addPostComment(postId: PostId, text: String): DomainResult<Comment>
    suspend fun addSolutionComment(solutionId: SolutionId, text: String): DomainResult<Comment>
    suspend fun getCommentReplies(commentId: CommentId): DomainResult<List<Comment>>
    suspend fun addCommentReply(commentId: CommentId, text: String): DomainResult<Comment>
}

interface FileRepository {
    suspend fun uploadFile(bytes: ByteArray, name: String): DomainResult<FileInfo>
}

interface PerformanceRepository {
    suspend fun getPerformanceTable(
        courseId: CourseId,
        from: OffsetDateTime? = null,
        to: OffsetDateTime? = null,
        onlyMandatory: Boolean = false,
    ): DomainResult<GradeTable>
}

