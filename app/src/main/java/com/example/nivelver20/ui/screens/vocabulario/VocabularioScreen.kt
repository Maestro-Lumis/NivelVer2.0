package com.example.nivelver20.ui.screens.vocabulario

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
fun VocabularioScreen(
    nivel: String = "A1",
    onNavigateToTest: () -> Unit = {},
    onNavigateToPerfil: () -> Unit = {},
    viewModel: VocabularioViewModel = viewModel()
) {
    val dimensions = rememberAdaptiveDimensions()
    val uiState by viewModel.uiState.collectAsState()

    // Загружаем слова при первом запуске
    LaunchedEffect(Unit) {
        if (uiState.spanishWords.isEmpty()) {
            viewModel.loadWords(nivel)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF02214a)),
        contentAlignment = Alignment.Center
    ) {
        // Показываем индикатор загрузки
        if (uiState.isLoading) {
            CircularProgressIndicator(
                color = Color(0xFFa3b944),
                modifier = Modifier.size(48.dp)
            )
        }

        // Показываем ошибку
        if (uiState.errorMessage != null) {
            Text(
                text = uiState.errorMessage ?: "",
                color = Color.Red,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(32.dp)
            )
        }

        // Основной контент (только если слова загружены)
        if (!uiState.isLoading && uiState.errorMessage == null) {
            Image(
                painter = painterResource(id = R.drawable.espanol_logo),
                contentDescription = "Letter Ñ",
                modifier = Modifier.fillMaxWidth(0.8f)
                    .fillMaxHeight(0.7f),
                alpha = 0.15f,
                contentScale = ContentScale.Fit
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = dimensions.horizontalPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(dimensions.verticalPadding))

            // Верхняя строка: NIVEL и NOMBRE
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = uiState.nivelLabel,
                    fontSize = dimensions.loginLabelFontSize.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFa3b944)
                )

                Text(
                    text = uiState.userName,
                    fontSize = dimensions.loginLabelFontSize.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFa3b944)
                )
            }

            Spacer(modifier = Modifier.height(dimensions.verticalPadding))

            // Карточка с заголовком и словами
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFFa0d47b),
                                Color(0xFFe0ca71)
                            )
                        ),
                        alpha = 0.55f,
                        shape = RoundedCornerShape(dimensions.buttonCornerRadius)
                    )
            ) {

                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Spacer(modifier = Modifier.height(dimensions.vocabularioPadingH))
                    // Заголовок VOCABULARIO
                    Text(
                        text = uiState.title,
                        fontSize = dimensions.vocabularioTitleFontSize.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFf2edd0),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(dimensions.vocabularioPadingH))

                    // БЛОК ИСПАНСКИХ СЛОВ (4 ряда × 2 колонки = 8 слов)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(dimensions.vocabularioCardSpacing)
                    ) {
                        // Ряд 1
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(dimensions.vocabularioCardSpacing)
                        ) {
                            WordCardItem(
                                card = uiState.spanishWords.getOrNull(0),
                                onClick = { viewModel.onSpanishCardClick(0) },
                                dimensions = dimensions,
                                modifier = Modifier.weight(1f)
                            )
                            WordCardItem(
                                card = uiState.spanishWords.getOrNull(1),
                                onClick = { viewModel.onSpanishCardClick(1) },
                                dimensions = dimensions,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Ряд 2
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(dimensions.vocabularioCardSpacing)
                        ) {
                            WordCardItem(
                                card = uiState.spanishWords.getOrNull(2),
                                onClick = { viewModel.onSpanishCardClick(2) },
                                dimensions = dimensions,
                                modifier = Modifier.weight(1f)
                            )
                            WordCardItem(
                                card = uiState.spanishWords.getOrNull(3),
                                onClick = { viewModel.onSpanishCardClick(3) },
                                dimensions = dimensions,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Ряд 3
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(dimensions.vocabularioCardSpacing)
                        ) {
                            WordCardItem(
                                card = uiState.spanishWords.getOrNull(4),
                                onClick = { viewModel.onSpanishCardClick(4) },
                                dimensions = dimensions,
                                modifier = Modifier.weight(1f)
                            )
                            WordCardItem(
                                card = uiState.spanishWords.getOrNull(5),
                                onClick = { viewModel.onSpanishCardClick(5) },
                                dimensions = dimensions,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Ряд 4
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(dimensions.vocabularioCardSpacing)
                        ) {
                            WordCardItem(
                                card = uiState.spanishWords.getOrNull(6),
                                onClick = { viewModel.onSpanishCardClick(6) },
                                dimensions = dimensions,
                                modifier = Modifier.weight(1f)
                            )
                            WordCardItem(
                                card = uiState.spanishWords.getOrNull(7),
                                onClick = { viewModel.onSpanishCardClick(7) },
                                dimensions = dimensions,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(dimensions.vocabularioBlockSpacing))

                    // БЛОК РУССКИХ СЛОВ (4 ряда × 2 колонки = 8 слов)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(dimensions.vocabularioCardSpacing)
                    ) {
                        // Ряд 1
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(dimensions.vocabularioCardSpacing)
                        ) {
                            WordCardItem(
                                card = uiState.russianWords.getOrNull(0),
                                onClick = { viewModel.onRussianCardClick(0) },
                                dimensions = dimensions,
                                modifier = Modifier.weight(1f)
                            )
                            WordCardItem(
                                card = uiState.russianWords.getOrNull(1),
                                onClick = { viewModel.onRussianCardClick(1) },
                                dimensions = dimensions,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Ряд 2
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(dimensions.vocabularioCardSpacing)
                        ) {
                            WordCardItem(
                                card = uiState.russianWords.getOrNull(2),
                                onClick = { viewModel.onRussianCardClick(2) },
                                dimensions = dimensions,
                                modifier = Modifier.weight(1f)
                            )
                            WordCardItem(
                                card = uiState.russianWords.getOrNull(3),
                                onClick = { viewModel.onRussianCardClick(3) },
                                dimensions = dimensions,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Ряд 3
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(dimensions.vocabularioCardSpacing)
                        ) {
                            WordCardItem(
                                card = uiState.russianWords.getOrNull(4),
                                onClick = { viewModel.onRussianCardClick(4) },
                                dimensions = dimensions,
                                modifier = Modifier.weight(1f)
                            )
                            WordCardItem(
                                card = uiState.russianWords.getOrNull(5),
                                onClick = { viewModel.onRussianCardClick(5) },
                                dimensions = dimensions,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Ряд 4
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(dimensions.vocabularioCardSpacing)
                        ) {
                            WordCardItem(
                                card = uiState.russianWords.getOrNull(6),
                                onClick = { viewModel.onRussianCardClick(6) },
                                dimensions = dimensions,
                                modifier = Modifier.weight(1f)
                            )
                            WordCardItem(
                                card = uiState.russianWords.getOrNull(7),
                                onClick = { viewModel.onRussianCardClick(7) },
                                dimensions = dimensions,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(dimensions.vocabularioPadingH))

                    // Счетчики внизу
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Text(
                            text = uiState.incorrectCount.toString(),
                            fontSize = dimensions.vocabularioCounterFontSize.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFC42D2C)
                        )

                        Text(
                            text = uiState.correctCount.toString(),
                            fontSize = dimensions.vocabularioCounterFontSize.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF48C553)
                        )
                    }
                    Spacer(modifier = Modifier.height(dimensions.vocabularioPadingH))
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

@Composable
private fun WordCardItem(
    card: WordCard?,
    onClick: () -> Unit,
    dimensions: com.example.nivelver20.ui.theme.AdaptiveDimensions,
    modifier: Modifier = Modifier
) {
    if (card == null) return

    // Определяем цвет границы в зависимости от состояния
    val borderColor = when (card.state) {
        CardState.NORMAL -> Color.Transparent
        CardState.SELECTED -> Color(0xCB02214A) // Голубой - выбрана
        CardState.CORRECT -> Color(0xFF48C553)  // Зеленый - правильно
        CardState.INCORRECT -> Color(0xFFC42D2C) // Красный - неправильно
        CardState.MATCHED -> Color.Gray          // Серый - уже сопоставлена
    }

    // Определяем цвет фона
    val backgroundColor = when (card.state) {
        CardState.MATCHED -> Color(0xFFCCCCCC) // Серый фон для сопоставленных
        else -> Color(0xFFf2edd0)
    }

    // Определяем, можно ли нажимать
    val isClickable = card.state != CardState.MATCHED

    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(8.dp)
            )
            .then(
                if (borderColor != Color.Transparent) {
                    Modifier.border(
                        width = 5.dp,
                        color = borderColor,
                        shape = RoundedCornerShape(8.dp)
                    )
                } else Modifier
            )
            .clickable(enabled = isClickable) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (card.isSpanish) card.spanish else card.russian,
            fontSize = dimensions.vocabularioWordFontSize.sp,
            fontWeight = FontWeight.Bold,
            color = if (card.state == CardState.MATCHED) Color.Gray else Color(0xFF003D5B),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
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