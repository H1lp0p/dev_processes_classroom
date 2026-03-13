package com.stuf.classroom

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.stuf.classroom.auth.AuthManager
import com.stuf.classroom.auth.AuthState
import com.stuf.classroom.auth.LoadingScreen
import com.stuf.classroom.auth.LoginRoute
import com.stuf.classroom.auth.LoginViewModel
import com.stuf.classroom.auth.RegisterRoute
import com.stuf.classroom.auth.RegisterViewModel
import com.stuf.classroom.course.CourseRoute
import com.stuf.classroom.post.PostRoute
import com.stuf.classroom.courses.UserCoursesRoute
import com.stuf.classroom.courses.UserCoursesViewModel
import com.stuf.classroom.courses.CreateCourseRoute
import com.stuf.classroom.courses.JoinCourseRoute
import com.stuf.domain.model.CourseId
import com.stuf.domain.model.CourseRole
import com.stuf.domain.model.UserCourse
import com.stuf.classroom.ui.theme.ClassroomTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ClassroomTheme {
                val navController = rememberNavController()
                val authState by authManager.authState.collectAsState()
                val scope = rememberCoroutineScope()

                LaunchedEffect(Unit) {
                    authManager.initialize()
                }

                LaunchedEffect(authState) {
                    val current = navController.currentBackStackEntry?.destination?.route
                    when (authState) {
                        is AuthState.Initial, is AuthState.Loading -> { /* остаёмся на loading */ }
                        is AuthState.Unauthenticated -> {
                            if (current == "loading") {
                                navController.navigate("login") {
                                    popUpTo("loading") { inclusive = true }
                                }
                            }
                        }
                        is AuthState.Authenticated -> {
                            if (current == "loading") {
                                navController.navigate("home") {
                                    popUpTo("loading") { inclusive = true }
                                }
                            } else if (current == "login" || current == "register") {
                                navController.navigate("home") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        }
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = "loading",
                ) {
                    composable("loading") {
                        LoadingScreen()
                    }
                    composable("login") {
                        val viewModel: LoginViewModel = hiltViewModel()
                        LoginRoute(
                            viewModel = viewModel,
                            onNavigateToRegister = {
                                navController.navigate("register")
                            },
                            onAuthSuccess = {
                                navController.navigate("home") {
                                    popUpTo("login") { inclusive = true }
                                }
                            },
                        )
                    }
                    composable("register") {
                        val viewModel: RegisterViewModel = hiltViewModel()
                        RegisterRoute(
                            viewModel = viewModel,
                            onNavigateToLogin = {
                                navController.popBackStack("login", inclusive = false)
                            },
                            onAuthSuccess = {
                                navController.navigate("home") {
                                    popUpTo("login") { inclusive = true }
                                }
                            },
                        )
                    }
                    composable("home") {
                        val viewModel: UserCoursesViewModel = hiltViewModel()
                        UserCoursesRoute(
                            viewModel = viewModel,
                            onLogout = {
                                scope.launch {
                                    authManager.logout()
                                    navController.navigate("login") {
                                        popUpTo("home") { inclusive = true }
                                    }
                                }
                            },
                            onNewCourse = { navController.navigate("createCourse") },
                            onJoinCourse = { navController.navigate("joinCourse") },
                            onCourseClick = { course: UserCourse ->
                                val roleSegment = when (course.role) {
                                    com.stuf.domain.model.CourseRole.TEACHER -> "teacher"
                                    com.stuf.domain.model.CourseRole.STUDENT -> "student"
                                }
                                navController.navigate("course/${course.id.value}/$roleSegment")
                            },
                        )
                    }
                    composable("course/{courseId}/{role}") {
                        CourseRoute(
                            onPostClick = { postId ->
                                val roleArg = navController.currentBackStackEntry
                                    ?.arguments
                                    ?.getString("role")
                                    ?: "student"
                                navController.navigate("post/${postId.value}/$roleArg")
                            },
                            onCreatePostClick = {
                                // Навигация к созданию поста будет добавлена позже
                            },
                            onLeaveCourse = {
                                navController.popBackStack()
                            },
                        )
                    }
                    composable("post/{postId}/{role}") { backStackEntry ->
                        PostRoute(
                            navController = navController,
                            backStackEntry = backStackEntry,
                        )
                    }
                    composable("createCourse") {
                        CreateCourseRoute(
                            onBack = {
                                navController.popBackStack()
                            },
                            onNavigateToCourse = { courseId: CourseId, role: CourseRole ->
                                val roleSegment = when (role) {
                                    CourseRole.TEACHER -> "teacher"
                                    CourseRole.STUDENT -> "student"
                                }
                                navController.navigate("course/${courseId.value}/$roleSegment") {
                                    // Убираем экран создания из back stack, чтобы back вёл на home.
                                    popUpTo("home") {
                                        inclusive = false
                                    }
                                }
                            },
                        )
                    }
                    composable("joinCourse") {
                        JoinCourseRoute(
                            onBack = {
                                navController.popBackStack()
                            },
                            onNavigateToCourse = { courseId: CourseId, role: CourseRole ->
                                val roleSegment = when (role) {
                                    CourseRole.TEACHER -> "teacher"
                                    CourseRole.STUDENT -> "student"
                                }
                                navController.navigate("course/${courseId.value}/$roleSegment") {
                                    // Убираем экран присоединения из back stack, чтобы back вёл на home.
                                    popUpTo("home") {
                                        inclusive = false
                                    }
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}