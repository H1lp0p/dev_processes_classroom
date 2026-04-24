package com.stuf.data.demo

import com.stuf.domain.common.DomainError
import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.Comment
import com.stuf.domain.model.CommentId
import com.stuf.domain.model.Course
import com.stuf.domain.model.CourseId
import com.stuf.domain.model.CourseMember
import com.stuf.domain.model.CourseRole
import com.stuf.domain.model.FileInfo
import com.stuf.domain.model.GradeTable
import com.stuf.domain.model.Post
import com.stuf.domain.model.PostId
import com.stuf.domain.model.Review
import com.stuf.domain.model.Solution
import com.stuf.domain.model.SolutionId
import com.stuf.domain.model.SolutionStatus
import com.stuf.domain.model.TaskId
import com.stuf.domain.model.UserCourse
import com.stuf.domain.model.UserId
import com.stuf.domain.repository.CommentRepository
import com.stuf.domain.repository.CourseRepository
import com.stuf.domain.repository.FileRepository
import com.stuf.domain.repository.PerformanceRepository
import com.stuf.domain.repository.PostRepository
import com.stuf.domain.repository.SolutionRepository
import java.time.OffsetDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DemoCourseRepository @Inject constructor(
    private val store: DemoDataStore,
) : CourseRepository {

    override suspend fun getUserCourses(): DomainResult<List<UserCourse>> =
        DomainResult.Success(store.getUserCourses())

    override suspend fun createCourse(title: String): DomainResult<Course> =
        DomainResult.Success(store.createCourse(title, DemoIds.userStudent))

    override suspend fun joinCourse(inviteCode: String): DomainResult<Course> {
        val course = store.joinCourse(inviteCode)
            ?: return DomainResult.Failure(DomainError.Validation("Курс с таким кодом не найден"))
        return DomainResult.Success(course)
    }

    override suspend fun getCourseInfo(courseId: CourseId): DomainResult<Course> {
        val c = store.getCourseInfo(courseId)
            ?: return DomainResult.Failure(DomainError.NotFound)
        return DomainResult.Success(c)
    }

    override suspend fun getCourseMembers(
        courseId: CourseId,
        query: String?,
    ): DomainResult<List<CourseMember>> =
        DomainResult.Success(store.getCourseMembers(courseId, query))

    override suspend fun changeMemberRole(
        courseId: CourseId,
        userId: UserId,
        newRole: CourseRole,
    ): DomainResult<Unit> {
        if (!store.changeMemberRole(courseId, userId, newRole)) {
            return DomainResult.Failure(DomainError.NotFound)
        }
        return DomainResult.Success(Unit)
    }

    override suspend fun removeMember(courseId: CourseId, userId: UserId): DomainResult<Unit> {
        if (!store.removeMember(courseId, userId)) {
            return DomainResult.Failure(DomainError.NotFound)
        }
        return DomainResult.Success(Unit)
    }

    override suspend fun leaveCourse(courseId: CourseId): DomainResult<Unit> {
        store.leaveCourse(courseId)
        return DomainResult.Success(Unit)
    }
}

@Singleton
class DemoPostRepository @Inject constructor(
    private val store: DemoDataStore,
) : PostRepository {

    override suspend fun getCourseFeed(
        courseId: CourseId,
        skip: Int,
        take: Int,
    ): DomainResult<List<Post>> =
        DomainResult.Success(store.getCourseFeed(courseId, skip, take))

    override suspend fun getPost(postId: PostId): DomainResult<Post> {
        val p = store.getPost(postId) ?: return DomainResult.Failure(DomainError.NotFound)
        return DomainResult.Success(p)
    }

    override suspend fun createPost(courseId: CourseId, post: Post): DomainResult<Post> =
        DomainResult.Success(store.createPost(courseId, post))

    override suspend fun updatePost(postId: PostId, post: Post): DomainResult<Post> {
        val p = store.updatePost(postId, post) ?: return DomainResult.Failure(DomainError.NotFound)
        return DomainResult.Success(p)
    }

    override suspend fun deletePost(postId: PostId): DomainResult<Unit> {
        if (!store.deletePost(postId)) return DomainResult.Failure(DomainError.NotFound)
        return DomainResult.Success(Unit)
    }
}

@Singleton
class DemoSolutionRepository @Inject constructor(
    private val store: DemoDataStore,
) : SolutionRepository {

    override suspend fun submitSolution(
        taskId: TaskId,
        text: String?,
        fileIds: List<String>,
    ): DomainResult<Solution> =
        DomainResult.Success(store.submitSolution(taskId, text, fileIds))

    override suspend fun cancelSolution(taskId: TaskId): DomainResult<Unit> {
        if (!store.cancelSolution(taskId)) return DomainResult.Failure(DomainError.NotFound)
        return DomainResult.Success(Unit)
    }

    override suspend fun getUserSolution(taskId: TaskId): DomainResult<Solution?> =
        DomainResult.Success(store.getUserSolution(taskId))

    override suspend fun getTaskSolutions(
        taskId: TaskId,
        status: SolutionStatus?,
        studentId: UserId?,
    ): DomainResult<List<Solution>> =
        DomainResult.Success(store.getTaskSolutions(taskId, status, studentId))

    override suspend fun reviewSolution(solutionId: SolutionId, review: Review): DomainResult<Unit> {
        if (!store.reviewSolution(solutionId, review)) {
            return DomainResult.Failure(DomainError.NotFound)
        }
        return DomainResult.Success(Unit)
    }
}

@Singleton
class DemoCommentRepository @Inject constructor(
    private val store: DemoDataStore,
) : CommentRepository {

    override suspend fun getPostComments(postId: PostId): DomainResult<List<Comment>> =
        DomainResult.Success(store.getPostComments(postId))

    override suspend fun getSolutionComments(solutionId: SolutionId): DomainResult<List<Comment>> =
        DomainResult.Success(store.getSolutionComments(solutionId))

    override suspend fun addPostComment(postId: PostId, text: String): DomainResult<Comment> =
        DomainResult.Success(store.addPostComment(postId, text))

    override suspend fun addSolutionComment(solutionId: SolutionId, text: String): DomainResult<Comment> =
        DomainResult.Success(store.addSolutionComment(solutionId, text))

    override suspend fun getCommentReplies(commentId: CommentId): DomainResult<List<Comment>> =
        DomainResult.Success(store.getCommentReplies(commentId))

    override suspend fun addCommentReply(commentId: CommentId, text: String): DomainResult<Comment> =
        DomainResult.Success(store.addCommentReply(commentId, text))

    override suspend fun editComment(commentId: CommentId, text: String): DomainResult<Unit> =
        if (store.editComment(commentId, text)) {
            DomainResult.Success(Unit)
        } else {
            DomainResult.Failure(DomainError.NotFound)
        }

    override suspend fun deleteComment(commentId: CommentId): DomainResult<Unit> =
        if (store.deleteComment(commentId)) {
            DomainResult.Success(Unit)
        } else {
            DomainResult.Failure(DomainError.NotFound)
        }
}

@Singleton
class DemoFileRepository @Inject constructor(
    private val store: DemoDataStore,
) : FileRepository {
    override suspend fun uploadFile(bytes: ByteArray, name: String): DomainResult<FileInfo> =
        DomainResult.Success(store.uploadFile(bytes, name))
}

@Singleton
class DemoPerformanceRepository @Inject constructor(
    private val store: DemoDataStore,
) : PerformanceRepository {
    override suspend fun getPerformanceTable(
        courseId: CourseId,
        from: OffsetDateTime?,
        to: OffsetDateTime?,
        onlyMandatory: Boolean,
    ): DomainResult<GradeTable> {
        if (store.getCourseInfo(courseId) == null) {
            return DomainResult.Failure(DomainError.NotFound)
        }
        return DomainResult.Success(store.getPerformanceTable(courseId))
    }
}
