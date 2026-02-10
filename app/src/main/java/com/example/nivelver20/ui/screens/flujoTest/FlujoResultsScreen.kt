package com.example.nivelver20.ui.screens.flujoTest

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.nivelver20.R
import com.example.nivelver20.data.session.SessionManager
import com.example.nivelver20.ui.theme.rememberAdaptiveDimensions

@Composable
fun FlujoResultsScreen(
    navController: NavController,
    viewModel: FlujoViewModel
) {
    val dimensions = rememberAdaptiveDimensions()
    val context = LocalContext.current
    val sessionManager = remember { SessionManager.getInstance(context) }
    val flujoResult = remember { sessionManager.getFlujoResult() }

    // Parse level results from JSON
    val levelResults = remember {
        parseLevelResults(flujoResult.levelResults)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF02214a))
    ) {
        Image(
            painter = painterResource(id = R.drawable.espanol_logo),
            contentDescription = "Background",
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .fillMaxHeight(0.7f),
            alpha = 0.15f,
            contentScale = ContentScale.Fit
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = dimensions.horizontalPadding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(dimensions.verticalPadding))

            // Trophy icon with final level
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = "Trophy",
                modifier = Modifier.size(dimensions.audioVolumeUp),
                tint = Color(0xFFFFD700)
            )

            Spacer(modifier = Modifier.height(dimensions.spaceBetweenButtons))

            Text(
                text = "Nivel alcanzado:",
                fontSize = dimensions.vocabularioWordFontSize.sp,
                color = Color(0xFFf2edd0),
                textAlign = TextAlign.Center
            )

            Text(
                text = flujoResult.finalLevel,
                fontSize = (dimensions.vocabularioTitleFontSize * 2).sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFa3b944),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(dimensions.verticalPadding))

            // Total stats card
            FlujoTotalStatsCard(
                totalQuestions = flujoResult.totalQuestions,
                totalCorrect = flujoResult.totalCorrect,
                dimensions = dimensions
            )

            Spacer(modifier = Modifier.height(dimensions.verticalPadding))

            // Level results
            Text(
                text = "Resultados por nivel:",
                fontSize = dimensions.vocabularioWordFontSize.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFf2edd0),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(dimensions.spaceBetweenButtons))

            levelResults.forEach { (nivel, result) ->
                FlujoLevelResultCard(
                    nivel = nivel,
                    correctas = result.first,
                    total = result.second,
                    aprobado = result.third,
                    dimensions = dimensions
                )
                Spacer(modifier = Modifier.height(dimensions.spaceBetweenButtons))
            }

            Spacer(modifier = Modifier.height(dimensions.verticalPadding))

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(dimensions.spaceBetweenButtons)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(dimensions.bottomButtonHeight)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFFA985F0),
                                    Color(0xFF85EDFF)
                                )
                            ),
                            shape = RoundedCornerShape(dimensions.buttonCornerRadius)
                        )
                ) {
                    Button(
                        onClick = {
                            navController.navigate(com.example.nivelver20.navigation.Routes.Main.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(2.dp),
                        shape = RoundedCornerShape(dimensions.buttonCornerRadius),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF003D5B)
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "VOLVER AL INICIO",
                            fontSize = dimensions.bottomButtonFontSize.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFa3b944),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(dimensions.spaceBetweenButtons))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dimensions.bottomButtonHeight)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF48C553),
                                Color(0xFF48C553)
                            )
                        ),
                        shape = RoundedCornerShape(dimensions.buttonCornerRadius)
                    )
            ) {
                Button(
                    onClick = {
                        // Restart test
                        navController.navigate(com.example.nivelver20.navigation.Routes.Flujo.route + "/A1") {
                            popUpTo(com.example.nivelver20.navigation.Routes.FlujoResults.route) { inclusive = true }
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(2.dp),
                    shape = RoundedCornerShape(dimensions.buttonCornerRadius),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF02214a)
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "REPETIR TEST",
                        fontSize = dimensions.bottomButtonFontSize.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFf2edd0),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(dimensions.verticalPadding))
        }
    }
}

@Composable
private fun FlujoTotalStatsCard(
    totalQuestions: Int,
    totalCorrect: Int,
    dimensions: com.example.nivelver20.ui.theme.AdaptiveDimensions
) {
    val percentage = if (totalQuestions > 0) {
        (totalCorrect.toFloat() / totalQuestions.toFloat() * 100).toInt()
    } else 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(dimensions.buttonCornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF003D5B)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensions.horizontalPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Resumen total",
                fontSize = dimensions.vocabularioWordFontSize.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFa3b944)
            )

            Spacer(modifier = Modifier.height(dimensions.spaceBetweenButtons))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FlujoStatItem(
                    label = "Preguntas",
                    value = totalQuestions.toString(),
                    dimensions = dimensions
                )
                FlujoStatItem(
                    label = "Correctas",
                    value = totalCorrect.toString(),
                    dimensions = dimensions
                )
                FlujoStatItem(
                    label = "Porcentaje",
                    value = "$percentage%",
                    dimensions = dimensions
                )
            }

            Spacer(modifier = Modifier.height(dimensions.spaceBetweenButtons))

            LinearProgressIndicator(
                progress = { percentage / 100f },
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFa3b944),
                trackColor = Color(0xFF02214a)
            )

            Spacer(modifier = Modifier.height(dimensions.spaceBetweenButtons / 2))
        }
    }
}

@Composable
private fun FlujoStatItem(
    label: String,
    value: String,
    dimensions: com.example.nivelver20.ui.theme.AdaptiveDimensions
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = (dimensions.vocabularioTitleFontSize * 1.2f).sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFf2edd0)
        )
        Text(
            text = label,
            fontSize = (dimensions.vocabularioWordFontSize * 0.8f).sp,
            color = Color(0xFFf2edd0).copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun FlujoLevelResultCard(
    nivel: String,
    correctas: Int,
    total: Int,
    aprobado: Boolean,
    dimensions: com.example.nivelver20.ui.theme.AdaptiveDimensions
) {
    val backgroundColor = if (aprobado) Color(0xFF48C553) else Color(0xFFC42D2C)
    val icon = if (aprobado) Icons.Default.CheckCircle else Icons.Default.Close

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(dimensions.buttonCornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensions.horizontalPadding / 2),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Level badge
            Box(
                modifier = Modifier
                    .size(dimensions.bottomButtonHeight)
                    .background(
                        color = backgroundColor,
                        shape = RoundedCornerShape(dimensions.buttonCornerRadius / 2)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = nivel,
                    fontSize = dimensions.vocabularioWordFontSize.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Score
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "$correctas / $total",
                    fontSize = dimensions.vocabularioWordFontSize.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFf2edd0)
                )
                Text(
                    text = if (aprobado) "Aprobado" else "No aprobado",
                    fontSize = (dimensions.vocabularioWordFontSize * 0.8f).sp,
                    color = Color(0xFFf2edd0).copy(alpha = 0.7f)
                )
            }

            // Icon
            Icon(
                imageVector = icon,
                contentDescription = if (aprobado) "Passed" else "Failed",
                modifier = Modifier.size(dimensions.bottomButtonHeight / 2),
                tint = backgroundColor
            )
        }
    }
}

// Helper function to parse JSON string manually
private fun parseLevelResults(json: String): Map<String, Triple<Int, Int, Boolean>> {
    val results = mutableMapOf<String, Triple<Int, Int, Boolean>>()

    try {
        // Simple JSON parsing without Gson
        // Format: {"A1":{"correctas":3,"total":4,"aprobado":true},...}

        val levelRegex = """"(\w+)"\s*:\s*\{[^}]+\}""".toRegex()
        val matches = levelRegex.findAll(json)

        matches.forEach { match ->
            val levelName = match.groupValues[1]
            val levelData = match.value

            val correctasRegex = """"correctas"\s*:\s*(\d+)""".toRegex()
            val totalRegex = """"total"\s*:\s*(\d+)""".toRegex()
            val aprobadoRegex = """"aprobado"\s*:\s*(true|false)""".toRegex()

            val correctas = correctasRegex.find(levelData)?.groupValues?.get(1)?.toIntOrNull() ?: 0
            val total = totalRegex.find(levelData)?.groupValues?.get(1)?.toIntOrNull() ?: 0
            val aprobado = aprobadoRegex.find(levelData)?.groupValues?.get(1) == "true"

            results[levelName] = Triple(correctas, total, aprobado)
        }
    } catch (e: Exception) {
        // If parsing fails, return empty map
    }

    return results
}