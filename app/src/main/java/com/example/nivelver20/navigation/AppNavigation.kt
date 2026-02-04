package com.example.nivelver20.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.nivelver20.ui.screens.main.MainScreen
import com.example.nivelver20.ui.screens.nivel.NivelSelectionScreen
import androidx.compose.ui.platform.LocalContext
import com.example.nivelver20.data.session.SessionManager
import com.example.nivelver20.ui.screens.audio.AudioScreen
import com.example.nivelver20.ui.screens.audio.AudioResultsScreen
import com.example.nivelver20.ui.screens.auth.LoginScreen
import com.example.nivelver20.ui.screens.auth.RegisterScreen
import com.example.nivelver20.ui.screens.lectura.LecturaScreen
import com.example.nivelver20.ui.screens.lectura.LecturaResultsScreen
import com.example.nivelver20.ui.screens.perfil.PerfilScreen
import com.example.nivelver20.ui.screens.vocabulario.VocabularioResultsScreen
import com.example.nivelver20.ui.screens.vocabulario.VocabularioScreen
import com.example.nivelver20.ui.screens.grammar.GrammarScreen
import com.example.nivelver20.ui.screens.grammar.GrammarResultsScreen

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val sessionManager = SessionManager.getInstance(context)
    val isLoggedIn by sessionManager.isLoggedIn.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Routes.Main.route
    ) {
        // Главный экран
        composable(Routes.Main.route) {
            MainScreen(
                onNavigateToVocabulario = {
                    navController.navigate(Routes.NivelSelection.route + "?destination=vocabulario")
                },
                onNavigateToGrammatica = {
                    navController.navigate(Routes.NivelSelection.route + "?destination=grammatica")
                },
                onNavigateToAudio = {
                    navController.navigate(Routes.NivelSelection.route + "?destination=audio")
                },
                onNavigateToLectura = {
                    navController.navigate(Routes.NivelSelection.route + "?destination=lectura")
                },
                onNavigateToTest = {
                    if (isLoggedIn) {
                        navController.navigate(Routes.Perfil.route)
                    } else {
                        navController.navigate(Routes.Login.route)
                    }
                },
                onNavigateToPerfil = {
                    if (isLoggedIn) {
                        navController.navigate(Routes.Perfil.route)
                    } else {
                        navController.navigate(Routes.Login.route)
                    }
                }
            )
        }

        // Экран выбора уровня
        composable(Routes.NivelSelection.route + "?destination={destination}") { backStackEntry ->
            val destination = backStackEntry.arguments?.getString("destination") ?: "vocabulario"
            NivelSelectionScreen(
                onNivelSelected = { nivelId ->
                    when (destination) {
                        "lectura" -> navController.navigate(Routes.Lectura.route + "/$nivelId")
                        "audio" -> navController.navigate(Routes.Audio.route + "/$nivelId")
                        "grammatica" -> navController.navigate(Routes.Grammatica.route + "/$nivelId")
                        else -> navController.navigate(Routes.Vocabulario.route + "/$nivelId")
                    }
                },
                onNavigateToTest = {
                    navController.popBackStack(Routes.Main.route, false)
                },
                onNavigateToPerfil = {
                    if (isLoggedIn) {
                        navController.navigate(Routes.Perfil.route)
                    } else {
                        navController.navigate(Routes.Login.route)
                    }
                }
            )
        }

        // Экран авторизации
        composable(Routes.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.Perfil.route) {
                        popUpTo(Routes.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Routes.Register.route)
                },
                onNavigateToTest = {
                    navController.navigate(Routes.Main.route) {
                        popUpTo(Routes.Main.route) { inclusive = true }
                    }
                },
                onNavigateToPerfil = {
                    if (isLoggedIn) {
                        navController.navigate(Routes.Perfil.route)
                    }
                }
            )
        }

        // Экран регистрации
        composable(Routes.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {},
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToTest = {
                    navController.navigate(Routes.Main.route) {
                        popUpTo(Routes.Main.route) { inclusive = true }
                    }
                },
                onNavigateToPerfil = {
                    if (isLoggedIn) {
                        navController.navigate(Routes.Perfil.route)
                    } else {
                        navController.navigate(Routes.Login.route)
                    }
                }
            )
        }

        // Экран профиля
        composable(Routes.Perfil.route) {
            LaunchedEffect(isLoggedIn) {
                if (!isLoggedIn) {
                    navController.navigate(Routes.Login.route) {
                        popUpTo(Routes.Perfil.route) { inclusive = true }
                    }
                }
            }

            if (isLoggedIn) {
                val vocabularioResult by sessionManager.vocabularioResult.collectAsState()
                val lecturaResult by sessionManager.lecturaResult.collectAsState()
                val audioResult by sessionManager.audioResult.collectAsState()
                val grammarResult by sessionManager.grammarResult.collectAsState()

                PerfilScreen(
                    onNavigateToNivel = {},
                    onNavigateToFlujo = {},
                    onNavigateToVocabulario = {
                        navController.navigate(
                            "${Routes.VocabularioResults.route}/${vocabularioResult.nivel}/${vocabularioResult.correctCount}/${vocabularioResult.incorrectCount}"
                        )
                    },
                    onNavigateToGrammatica = {
                        navController.navigate(
                            "${Routes.GrammaticaResults.route}/${grammarResult.nivel}/${grammarResult.correctCount}/${grammarResult.incorrectCount}"
                        )
                    },
                    onNavigateToAudio = {
                        navController.navigate(
                            "${Routes.AudioResults.route}/${audioResult.nivel}/${audioResult.correctCount}/${audioResult.incorrectCount}"
                        )
                    },
                    onNavigateToLectura = {
                        navController.navigate(
                            "${Routes.LecturaResults.route}/${lecturaResult.nivel}/${lecturaResult.correctCount}/${lecturaResult.incorrectCount}"
                        )
                    },
                    onNavigateToTest = {
                        navController.popBackStack(Routes.Main.route, false)
                    },
                    onNavigateToPerfil = {},
                    onLogout = {
                        navController.navigate(Routes.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }

        // Vocabulario
        composable(Routes.Vocabulario.route + "/{nivelId}") { backStackEntry ->
            val nivelId = backStackEntry.arguments?.getString("nivelId") ?: "A1"
            VocabularioScreen(
                nivel = nivelId,
                onNavigateToTest = {
                    navController.popBackStack(Routes.Main.route, false)
                },
                onNavigateToPerfil = {
                    if (isLoggedIn) {
                        navController.navigate(Routes.Perfil.route)
                    } else {
                        navController.navigate(Routes.Login.route)
                    }
                },
                onNavigateToResults = { nivel, correct, incorrect ->
                    navController.navigate(
                        "${Routes.VocabularioResults.route}/$nivel/$correct/$incorrect"
                    ) {
                        popUpTo(Routes.Vocabulario.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.VocabularioResults.route + "/{nivel}/{correctCount}/{incorrectCount}") { backStackEntry ->
            val nivel = backStackEntry.arguments?.getString("nivel") ?: "A1"
            val correctCount = backStackEntry.arguments?.getString("correctCount")?.toIntOrNull() ?: 0
            val incorrectCount = backStackEntry.arguments?.getString("incorrectCount")?.toIntOrNull() ?: 0

            VocabularioResultsScreen(
                nivel = nivel,
                userName = sessionManager.getCurrentUser() ?: "NOMBRE",
                correctCount = correctCount,
                incorrectCount = incorrectCount,
                onNavigateToMain = {
                    navController.navigate(Routes.Main.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToPerfil = {
                    if (isLoggedIn) {
                        navController.navigate(Routes.Perfil.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Routes.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }

        // Lectura
        composable(Routes.Lectura.route + "/{nivelId}") { backStackEntry ->
            val nivelId = backStackEntry.arguments?.getString("nivelId") ?: "A1"
            LecturaScreen(
                nivel = nivelId,
                onNavigateToTest = {
                    navController.popBackStack(Routes.Main.route, false)
                },
                onNavigateToPerfil = {
                    if (isLoggedIn) {
                        navController.navigate(Routes.Perfil.route)
                    } else {
                        navController.navigate(Routes.Login.route)
                    }
                },
                onNavigateToResults = { nivel, correct, incorrect ->
                    navController.navigate(
                        "${Routes.LecturaResults.route}/$nivel/$correct/$incorrect"
                    ) {
                        popUpTo(Routes.Lectura.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.LecturaResults.route + "/{nivel}/{correctCount}/{incorrectCount}") { backStackEntry ->
            val nivel = backStackEntry.arguments?.getString("nivel") ?: "A1"
            val correctCount = backStackEntry.arguments?.getString("correctCount")?.toIntOrNull() ?: 0
            val incorrectCount = backStackEntry.arguments?.getString("incorrectCount")?.toIntOrNull() ?: 0

            LecturaResultsScreen(
                nivel = nivel,
                userName = sessionManager.getCurrentUser() ?: "NOMBRE",
                correctCount = correctCount,
                incorrectCount = incorrectCount,
                onNavigateToMain = {
                    navController.navigate(Routes.Main.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToPerfil = {
                    if (isLoggedIn) {
                        navController.navigate(Routes.Perfil.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Routes.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }

        // Audio
        composable(Routes.Audio.route + "/{nivelId}") { backStackEntry ->
            val nivelId = backStackEntry.arguments?.getString("nivelId") ?: "A1"
            AudioScreen(
                nivel = nivelId,
                onNavigateToTest = {
                    navController.popBackStack(Routes.Main.route, false)
                },
                onNavigateToPerfil = {
                    if (isLoggedIn) {
                        navController.navigate(Routes.Perfil.route)
                    } else {
                        navController.navigate(Routes.Login.route)
                    }
                },
                onNavigateToResults = { nivel, correct, incorrect ->
                    navController.navigate(
                        "${Routes.AudioResults.route}/$nivel/$correct/$incorrect"
                    ) {
                        popUpTo(Routes.Audio.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.AudioResults.route + "/{nivel}/{correctCount}/{incorrectCount}") { backStackEntry ->
            val nivel = backStackEntry.arguments?.getString("nivel") ?: "A1"
            val correctCount = backStackEntry.arguments?.getString("correctCount")?.toIntOrNull() ?: 0
            val incorrectCount = backStackEntry.arguments?.getString("incorrectCount")?.toIntOrNull() ?: 0

            AudioResultsScreen(
                nivel = nivel,
                userName = sessionManager.getCurrentUser() ?: "NOMBRE",
                correctCount = correctCount,
                incorrectCount = incorrectCount,
                onNavigateToMain = {
                    navController.navigate(Routes.Main.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToPerfil = {
                    if (isLoggedIn) {
                        navController.navigate(Routes.Perfil.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Routes.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }

        // Грамматика
        composable(Routes.Grammatica.route + "/{nivelId}") { backStackEntry ->
            val nivelId = backStackEntry.arguments?.getString("nivelId") ?: "A1"
            GrammarScreen(
                nivel = nivelId,
                onNavigateToTest = {
                    navController.popBackStack(Routes.Main.route, false)
                },
                onNavigateToPerfil = {
                    if (isLoggedIn) {
                        navController.navigate(Routes.Perfil.route)
                    } else {
                        navController.navigate(Routes.Login.route)
                    }
                },
                onNavigateToResults = { nivel, correct, incorrect ->
                    navController.navigate(
                        "${Routes.GrammaticaResults.route}/$nivel/$correct/$incorrect"
                    ) {
                        popUpTo(Routes.Grammatica.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.GrammaticaResults.route + "/{nivel}/{correctCount}/{incorrectCount}") { backStackEntry ->
            val nivel = backStackEntry.arguments?.getString("nivel") ?: "A1"
            val correctCount = backStackEntry.arguments?.getString("correctCount")?.toIntOrNull() ?: 0
            val incorrectCount = backStackEntry.arguments?.getString("incorrectCount")?.toIntOrNull() ?: 0

            GrammarResultsScreen(
                nivel = nivel,
                userName = sessionManager.getCurrentUser() ?: "NOMBRE",
                correctCount = correctCount,
                incorrectCount = incorrectCount,
                onNavigateToMain = {
                    navController.navigate(Routes.Main.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToPerfil = {
                    if (isLoggedIn) {
                        navController.navigate(Routes.Perfil.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Routes.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }
    }
}