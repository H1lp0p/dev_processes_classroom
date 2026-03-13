package com.stuf.classroom

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.stuf.classroom.auth.AuthManager
import com.stuf.classroom.auth.AuthState
import com.stuf.classroom.auth.HomeDebugScreen
import com.stuf.classroom.auth.LoadingScreen
import com.stuf.classroom.auth.LoginRoute
import com.stuf.classroom.auth.LoginViewModel
import com.stuf.classroom.auth.RegisterRoute
import com.stuf.classroom.auth.RegisterViewModel
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
                        HomeDebugScreen(
                            state = authState,
                            onLogout = {
                                scope.launch {
                                    authManager.logout()
                                    navController.navigate("login") {
                                        popUpTo("home") { inclusive = true }
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