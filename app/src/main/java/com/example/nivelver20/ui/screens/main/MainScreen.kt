package com.example.nivelver20.ui.screens.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nivelver20.R
import com.example.nivelver20.ui.theme.rememberAdaptiveDimensions

@Composable
fun MainScreen(
    onNavigateToVocabulario: () -> Unit = {},
    onNavigateToGrammatica: () -> Unit = {},
    onNavigateToAudio: () -> Unit = {},
    onNavigateToLectura: () -> Unit = {},
    onNavigateToTest: () -> Unit = {},
    onNavigateToPerfil: () -> Unit = {},
    onNavigateToFlujo: () -> Unit = {},
    onNavigateToNivel: () -> Unit = {},
    viewModel: MainViewModel = viewModel()
) {
    val dimensions = rememberAdaptiveDimensions()
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF0e5f63),
                        Color(0xFF02214a)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = dimensions.horizontalPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            // Верхняя часть: NIVEL и FLUJO
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = dimensions.verticalPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(dimensions.spaceBetweenButtons)
            ) {
                // NIVEL Button
                MenuButton(
                    text = uiState.nivel,
                    gradient = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFFF6EC7),
                            Color(0xFFFFE97D)
                        )
                    ),
                    onClick = onNavigateToNivel,
                    dimensions = dimensions
                )

                // FLUJO Button
                MenuButton(
                    text = uiState.flujo,
                    gradient = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFFFE97D),
                            Color(0xFFFFB347)
                        )
                    ),
                    onClick = onNavigateToFlujo,
                    dimensions = dimensions
                )
            }

            // Центральная часть: Буква Ñ (ФОТО - изображение)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = dimensions.spaceBetweenButtons),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.espanol_logo),
                    contentDescription = "Letter Ñ",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }

            // Средняя часть: Основные кнопки меню
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(dimensions.spaceBetweenButtons)
            ) {
                MenuButton(
                    text = uiState.vocabulario,
                    gradient = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFA0D47B),
                            Color(0xFFE0CA71)
                        )
                    ),
                    onClick = onNavigateToVocabulario,
                    dimensions = dimensions
                )

                MenuButton(
                    text = uiState.grammatica,
                    gradient = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF0097B2),
                            Color(0xFF7ED957)
                        )
                    ),
                    onClick = onNavigateToGrammatica,
                    dimensions = dimensions
                )

                MenuButton(
                    text = uiState.audio,
                    gradient = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF5DE0E6),
                            Color(0xFF1C74CF)
                        )
                    ),
                    onClick = onNavigateToAudio,
                    dimensions = dimensions
                )

                MenuButton(
                    text = uiState.lectura,
                    gradient = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFA985F0),
                            Color(0xFF85EDFF)
                        )
                    ),
                    onClick = onNavigateToLectura,
                    dimensions = dimensions
                )
            }

            // Нижняя часть: TEST и PERFIL (50% на 50%)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = dimensions.bottomButtonsTopPadding,
                        bottom = dimensions.verticalPadding
                    ),
                horizontalArrangement = Arrangement.spacedBy(dimensions.spaceBetweenButtons)
            ) {
                BottomButton(
                    text = uiState.testButton,
                    onClick = onNavigateToTest,
                    dimensions = dimensions,
                    modifier = Modifier.weight(1f)
                )

                BottomButton(
                    text = uiState.perfilButton,
                    onClick = onNavigateToPerfil,
                    dimensions = dimensions,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// Универсальная кнопка меню

@Composable
private fun MenuButton(
    text: String,
    gradient: Brush,
    onClick: () -> Unit,
    dimensions: com.example.nivelver20.ui.theme.AdaptiveDimensions
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(dimensions.buttonHeight),
        shape = RoundedCornerShape(dimensions.buttonCornerRadius),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = dimensions.buttonFontSize.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C3E50),
                textAlign = TextAlign.Center
            )
        }
    }
}

// Нижние кнопки TEST и PERFIL (50% на 50%) с градиентной рамкой

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