package com.example.nivelver20.ui.screens.nivelTest

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nivelver20.data.session.SessionManager
import com.example.nivelver20.ui.theme.rememberAdaptiveDimensions

@Composable
fun NivelResultsScreen(
    nivel: String,
    correctCount: Int,
    incorrectCount: Int,
    onNavigateToTest: () -> Unit,
    onRetakeTest: () -> Unit
) {
    val dimensions = rememberAdaptiveDimensions()
    val context = LocalContext.current
    val sessionManager = remember { SessionManager.getInstance(context) }

    val totalQuestions = correctCount + incorrectCount
    val percentage = if (totalQuestions > 0) {
        (correctCount * 100) / totalQuestions
    } else 0

    // Сохраняем результаты
    LaunchedEffect(Unit) {
        sessionManager.saveNivelResult(nivel, correctCount, incorrectCount)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF02214a))
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(dimensions.horizontalPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(dimensions.verticalPadding * 2))

        // Основная карточка результата
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    percentage >= 80 -> Color(0xFF48C553)
                    percentage >= 60 -> Color(0xFFFF9800)
                    else -> Color(0xFFC42D2C)
                }
            ),
            elevation = CardDefaults.cardElevation(8.dp),
            shape = RoundedCornerShape(dimensions.buttonCornerRadius)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = when {
                        percentage >= 80 -> Icons.Default.CheckCircle
                        percentage >= 60 -> Icons.Default.Star
                        else -> Icons.Default.Info
                    },
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Color.White
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = when {
                        percentage >= 80 -> "¡EXCELENTE!"
                        percentage >= 60 -> "¡BIEN HECHO!"
                        else -> "SIGUE PRACTICANDO"
                    },
                    fontSize = dimensions.vocabularioTitleFontSize.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "NIVEL $nivel",
                    fontSize = dimensions.loginLabelFontSize.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "$percentage%",
                    fontSize = (dimensions.vocabularioTitleFontSize * 2).sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = "$correctCount de $totalQuestions correctas",
                    fontSize = dimensions.buttonFontSize.sp,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(dimensions.verticalPadding * 2))

        // Кнопки
        Button(
            onClick = onRetakeTest,
            modifier = Modifier.fillMaxWidth().height(dimensions.bottomButtonHeight),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFa3b944)
            ),
            shape = RoundedCornerShape(dimensions.buttonCornerRadius)
        ) {
            Icon(Icons.Default.Refresh, "Repetir")
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "REPETIR TEST",
                fontSize = dimensions.bottomButtonFontSize.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(dimensions.spaceBetweenButtons))

        OutlinedButton(
            onClick = onNavigateToTest,
            modifier = Modifier.fillMaxWidth().height(dimensions.bottomButtonHeight),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFFa3b944)
            ),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = Brush.horizontalGradient(
                    listOf(Color(0xFFa3b944), Color(0xFFa3b944))
                )
            ),
            shape = RoundedCornerShape(dimensions.buttonCornerRadius)
        ) {
            Icon(Icons.Default.Home, "Test")
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "VOLVER AL TEST",
                fontSize = dimensions.bottomButtonFontSize.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(dimensions.verticalPadding * 2))
    }
}