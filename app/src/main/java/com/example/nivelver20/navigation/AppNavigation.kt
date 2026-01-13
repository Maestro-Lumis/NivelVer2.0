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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nivelver20.data.session.SessionManager
import com.example.nivelver20.ui.screens.audio.AudioScreen
import com.example.nivelver20.ui.screens.auth.LoginScreen
import com.example.nivelver20.ui.screens.auth.RegisterScreen
import com.example.nivelver20.ui.screens.lectura.LecturaScreen
import com.example.nivelver20.ui.screens.perfil.PerfilScreen
import com.example.nivelver20.ui.screens.vocabulario.VocabularioResultsScreen
import com.example.nivelver20.ui.screens.vocabulario.VocabularioScreen

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
                    } else {
                        // Уже на экране логина
                    }
                }
            )
        }

        // Экран регистрации
        composable(Routes.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    // Больше не используется, оставляем пустым
                },
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

        // Экран профиля - только для авторизованных
        composable(Routes.Perfil.route) {
            LaunchedEffect(isLoggedIn) {
                if (!isLoggedIn) {
                    navController.navigate(Routes.Login.route) {
                        popUpTo(Routes.Perfil.route) { inclusive = true }
                    }
                }
            }

            if (isLoggedIn) {
                PerfilScreen(
                    onNavigateToNivel = {
                        navController.navigate(Routes.NivelSelection.route + "?destination=vocabulario")
                    },
                    onNavigateToFlujo = {
                        // Пока пусто
                    },
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
                        navController.popBackStack(Routes.Main.route, false)
                    },
                    onNavigateToPerfil = {
                        // Уже на профиле
                    },
                    onLogout = {
                        navController.navigate(Routes.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }

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
                onNavigateToResults = { nivel, correct, incorrect ->  // НОВЫЙ CALLBACK
                    navController.navigate(
                        "${Routes.VocabularioResults.route}/$nivel/$correct/$incorrect"
                    ) {
                        popUpTo(Routes.Vocabulario.route) { inclusive = true }
                    }
                }
            )
        }

        // маршрут для результатов vocabulario
        composable(
            route = Routes.VocabularioResults.route + "/{nivel}/{correctCount}/{incorrectCount}"
        ) { backStackEntry ->
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
                onNavigateToPerfil = {  // ДОБАВЛЕНО
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

        // Экран Lectura
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
                }
            )
        }

        // Экран Audio
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
                }
            )
        }

        // Экран Grammatica
        composable(Routes.Grammatica.route + "/{nivelId}") { backStackEntry ->
            val nivelId = backStackEntry.arguments?.getString("nivelId") ?: "A1"
            PlaceholderScreen(
                text = "GRAMÁTICA\nNivel: $nivelId",
                onBack = {
                    navController.popBackStack(Routes.Main.route, false)
                }
            )
        }
    }
}

@Composable
private fun PlaceholderScreen(
    text: String,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF02214a)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = text,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFa3b944)
                )
            ) {
                Text(
                    text = "НАЗАД",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}