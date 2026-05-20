package com.stuf.classroom

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.rememberNavController
import com.stuf.classroom.auth.AuthManager
import com.stuf.classroom.auth.AuthState
import com.stuf.classroom.navigation.ClassroomNavHost
import com.stuf.classroom.navigation.ClassroomRoutes
import com.stuf.classroom.ui.theme.ClassroomTheme
import dagger.hilt.android.AndroidEntryPoint
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

                LaunchedEffect(Unit) {
                    authManager.initialize()
                }

                LaunchedEffect(authState) {
                    val current = navController.currentBackStackEntry?.destination?.route
                    when (authState) {
                        is AuthState.Initial, is AuthState.Loading -> { }
                        is AuthState.Unauthenticated -> {
                            if (current == ClassroomRoutes.LOADING) {
                                navController.navigate(ClassroomRoutes.LOGIN) {
                                    popUpTo(ClassroomRoutes.LOADING) { inclusive = true }
                                }
                            }
                        }
                        is AuthState.Authenticated -> {
                            if (current == ClassroomRoutes.LOADING) {
                                navController.navigate(ClassroomRoutes.HOME) {
                                    popUpTo(ClassroomRoutes.LOADING) { inclusive = true }
                                }
                            } else if (current == ClassroomRoutes.LOGIN || current == ClassroomRoutes.REGISTER) {
                                navController.navigate(ClassroomRoutes.HOME) {
                                    popUpTo(ClassroomRoutes.LOGIN) { inclusive = true }
                                }
                            }
                        }
                    }
                }

                ClassroomNavHost(
                    navController = navController,
                    authManager = authManager,
                )
            }
        }
    }
}
