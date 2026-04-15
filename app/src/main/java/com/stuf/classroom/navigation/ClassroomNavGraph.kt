package com.stuf.classroom.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.stuf.classroom.auth.AuthManager
import com.stuf.classroom.auth.LoadingScreen
import com.stuf.classroom.auth.LoginRoute
import com.stuf.classroom.auth.LoginViewModel
import com.stuf.classroom.auth.RegisterRoute
import com.stuf.classroom.auth.RegisterViewModel
import com.stuf.classroom.course.CourseRoute
import com.stuf.classroom.courses.CreateCourseRoute
import com.stuf.classroom.courses.JoinCourseRoute
import com.stuf.classroom.courses.UserCoursesRoute
import com.stuf.classroom.courses.UserCoursesViewModel
import com.stuf.classroom.grade.GradeDistributionRoute
import com.stuf.classroom.post.PostRoute
import com.stuf.classroom.profile.ChangePasswordRoute
import com.stuf.classroom.profile.EditProfileRoute
import com.stuf.classroom.profile.ProfileRoute
import com.stuf.domain.model.CourseId
import com.stuf.domain.model.CourseRole
import com.stuf.domain.model.UserCourse
import kotlinx.coroutines.launch

@Composable
fun ClassroomNavHost(
    navController: NavHostController,
    authManager: AuthManager,
) {
    val scope = rememberCoroutineScope()

    NavHost(
        navController = navController,
        startDestination = ClassroomRoutes.LOADING,
    ) {
        composable(ClassroomRoutes.LOADING) {
            LoadingScreen()
        }
        composable(ClassroomRoutes.LOGIN) {
            val viewModel: LoginViewModel = hiltViewModel()
            LoginRoute(
                viewModel = viewModel,
                onNavigateToRegister = {
                    navController.navigate(ClassroomRoutes.REGISTER)
                },
                onAuthSuccess = {
                    navController.navigate(ClassroomRoutes.HOME) {
                        popUpTo(ClassroomRoutes.LOGIN) { inclusive = true }
                    }
                },
            )
        }
        composable(ClassroomRoutes.REGISTER) {
            val viewModel: RegisterViewModel = hiltViewModel()
            RegisterRoute(
                viewModel = viewModel,
                onNavigateToLogin = {
                    navController.popBackStack(ClassroomRoutes.LOGIN, inclusive = false)
                },
                onAuthSuccess = {
                    navController.navigate(ClassroomRoutes.HOME) {
                        popUpTo(ClassroomRoutes.LOGIN) { inclusive = true }
                    }
                },
            )
        }
        composable(ClassroomRoutes.HOME) {
            val viewModel: UserCoursesViewModel = hiltViewModel()
            UserCoursesRoute(
                viewModel = viewModel,
                onProfile = { navController.navigate(ClassroomRoutes.PROFILE) },
                onNewCourse = { navController.navigate(ClassroomRoutes.CREATE_COURSE) },
                onJoinCourse = { navController.navigate(ClassroomRoutes.JOIN_COURSE) },
                onCourseClick = { course: UserCourse ->
                    navController.navigateToCourse(course.id, course.role)
                },
            )
        }
        composable(ClassroomRoutes.COURSE) {
            CourseRoute(
                onPostClick = { postId ->
                    val roleArg = navController.currentBackStackEntry
                        ?.arguments
                        ?.getString("role")
                        ?: "student"
                    val roleNav = when (roleArg.lowercase()) {
                        "teacher" -> CourseRole.TEACHER
                        else -> CourseRole.STUDENT
                    }
                    navController.navigate(ClassroomRoutes.post(postId, roleNav))
                },
                onLeaveCourse = {
                    navController.popBackStack()
                },
            )
        }
        composable(ClassroomRoutes.POST) { backStackEntry ->
            PostRoute(
                navController = navController,
                backStackEntry = backStackEntry,
            )
        }
        composable(
            route = ClassroomRoutes.GRADE_DISTRIBUTION,
            arguments =
                listOf(
                    navArgument("teamId") { type = NavType.StringType },
                    navArgument("postId") { type = NavType.StringType },
                    navArgument("role") { type = NavType.StringType },
                ),
        ) { backStackEntry ->
            GradeDistributionRoute(
                navController = navController,
                backStackEntry = backStackEntry,
            )
        }
        composable(ClassroomRoutes.CREATE_COURSE) {
            CreateCourseRoute(
                onBack = {
                    navController.popBackStack()
                },
                onNavigateToCourse = { courseId: CourseId, role: CourseRole ->
                    navController.navigateToCourse(courseId, role) {
                        popUpTo(ClassroomRoutes.HOME) {
                            inclusive = false
                        }
                    }
                },
            )
        }
        composable(ClassroomRoutes.JOIN_COURSE) {
            JoinCourseRoute(
                onBack = {
                    navController.popBackStack()
                },
                onNavigateToCourse = { courseId: CourseId, role: CourseRole ->
                    navController.navigateToCourse(courseId, role) {
                        popUpTo(ClassroomRoutes.HOME) {
                            inclusive = false
                        }
                    }
                },
            )
        }
        composable(ClassroomRoutes.PROFILE) {
            ProfileRoute(
                onBack = { navController.popBackStack() },
                onLogout = {
                    scope.launch {
                        authManager.logout()
                        navController.navigate(ClassroomRoutes.LOGIN) {
                            popUpTo(ClassroomRoutes.HOME) { inclusive = true }
                        }
                    }
                },
                onEditProfile = { navController.navigate(ClassroomRoutes.EDIT_PROFILE) },
                onChangePassword = { navController.navigate(ClassroomRoutes.CHANGE_PASSWORD) },
            )
        }
        composable(ClassroomRoutes.EDIT_PROFILE) {
            EditProfileRoute(
                onBack = { navController.popBackStack() },
            )
        }
        composable(ClassroomRoutes.CHANGE_PASSWORD) {
            ChangePasswordRoute(
                onBack = { navController.popBackStack() },
            )
        }
    }
}
