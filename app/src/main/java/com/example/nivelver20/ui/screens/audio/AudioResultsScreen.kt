package com.example.nivelver20.ui.screens.audio

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.nivelver20.R
import com.example.nivelver20.data.session.SessionManager
import com.example.nivelver20.ui.theme.rememberAdaptiveDimensions
import kotlin.math.roundToInt

@Composable
fun AudioResultsScreen(
    nivel: String,
    userName: String,
    correctCount: Int,
    incorrectCount: Int,
    onNavigateToMain: () -> Unit,
    onNavigateToPerfil: () -> Unit = {}
) {
    val dimensions = rememberAdaptiveDimensions()

    val context = LocalContext.current
    val sessionManager = SessionManager.getInstance(context)

    LaunchedEffect(Unit) {
        sessionManager.saveAudioResult(nivel, correctCount, incorrectCount)
    }

    // Подсчет процента правильных ответов
    val totalAnswers = correctCount + incorrectCount
    val successPercentage = if (totalAnswers > 0) {
        ((correctCount.toFloat() / totalAnswers.toFloat()) * 100).roundToInt()
    } else {
        0
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF02214a)),
        contentAlignment = Alignment.Center
    ) {
        // Фоновое изображение
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
                .padding(horizontal = dimensions.horizontalPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(dimensions.verticalPadding))

            // Верхняя строка: NOMBRE
            Text(
                text = userName,
                fontSize = dimensions.loginLabelFontSize.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFa3b944),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // RESULTADOS
            Text(
                text = "RESULTADOS",
                fontSize = dimensions.vocabularioTitleFontSize.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFa3b944),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(30.dp))

            // Карточка с результатами
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.7f)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFFA985F0),
                                Color(0xFF85edff)
                            )
                        ),
                        alpha = 0.55f,
                        shape = RoundedCornerShape(dimensions.buttonCornerRadius)
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 30.dp, horizontal = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // TEST DE AUDIO - внутри карточки вверху
                    Text(
                        text = "TEST DE AUDIO",
                        fontSize = dimensions.vocabularioTitleFontSize.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFf2edd0),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Контент посередине - элементы ближе друг к другу
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Процент успеха
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "$successPercentage%",
                                fontSize = (dimensions.vocabularioCounterFontSize * 2f).sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFf2edd0)
                            )
                            Text(
                                text = "Éxito",
                                fontSize = dimensions.loginLabelFontSize.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF02214a)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Счетчики
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            // Неправильные (красные)
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = incorrectCount.toString(),
                                    fontSize = (dimensions.vocabularioCounterFontSize * 1.5f).sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFC42D2C)
                                )
                                Text(
                                    text = "Errores",
                                    fontSize = dimensions.loginLabelFontSize.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF02214a)
                                )
                            }

                            // Правильные (зеленые)
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = correctCount.toString(),
                                    fontSize = (dimensions.vocabularioCounterFontSize * 1.5f).sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF48C553)
                                )
                                Text(
                                    text = "Correctas",
                                    fontSize = dimensions.loginLabelFontSize.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF02214a)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // NIVEL
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "NIVEL",
                                fontSize = dimensions.vocabularioTitleFontSize.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFf2edd0),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = nivel,
                                fontSize = (dimensions.vocabularioTitleFontSize * 1.5f).sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF02214a),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(dimensions.verticalPadding))

            // Нижняя часть: TEST и PERFIL
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = dimensions.verticalPadding),
                horizontalArrangement = Arrangement.spacedBy(dimensions.spaceBetweenButtons)
            ) {
                BottomButton(
                    text = "TEST",
                    onClick = onNavigateToMain,
                    dimensions = dimensions,
                    modifier = Modifier.weight(1f)
                )

                BottomButton(
                    text = "PERFIL",
                    onClick = onNavigateToPerfil,
                    dimensions = dimensions,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun BottomButton(
    text: String,
    onClick: () -> Unit,
    dimensions: com.example.nivelver20.ui.theme.AdaptiveDimensions,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
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
            onClick = onClick,
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
                text = text,
                fontSize = dimensions.bottomButtonFontSize.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFa3b944),
                textAlign = TextAlign.Center
            )
        }
    }
}