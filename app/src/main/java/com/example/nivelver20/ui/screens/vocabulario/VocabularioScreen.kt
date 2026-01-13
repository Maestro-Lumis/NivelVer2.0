package com.example.nivelver20.ui.screens.vocabulario

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.activity.compose.BackHandler

@Composable
fun VocabularioScreen(
    nivel : String,
    onNavigateToTest: () -> Unit = {},
    onNavigateToPerfil: () -> Unit = {},
    onNavigateToResults: (String, Int, Int) -> Unit = { _, _, _ -> },
    viewModel: VocabularioViewModel = viewModel()
) {
    val dimensions = rememberAdaptiveDimensions()
    val uiState by viewModel.uiState.collectAsState()

    // Загружаем слова при первом запуске
    LaunchedEffect(nivel) {
        viewModel.setNivel(nivel)
        viewModel.setResultsCallback { n, correct, incorrect ->
            onNavigateToResults(n, correct, incorrect)
        }
    }

    // Перехватываем системную кнопку "Назад"
    BackHandler {
        viewModel.requestExit(onNavigateToTest)
    }

    // Показываем диалог выхода
    if (uiState.showExitDialog) {
        ExitDialog(
            onConfirm = { viewModel.confirmExit() },
            onDismiss = { viewModel.cancelExit() },
            dimensions = dimensions
        )
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
                color = Color(0xFFC42D2C),
                fontSize = dimensions.vocabularioTitleFontSize.sp,
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
                    text = uiState.nivel,
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
                    onClick = { viewModel.requestExit(onNavigateToTest) },
                    dimensions = dimensions,
                    modifier = Modifier.weight(1f)
                )

                BottomButton(
                    text = uiState.perfilButton,
                    onClick = { viewModel.requestExit(onNavigateToPerfil) },
                    dimensions = dimensions,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ExitDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    dimensions: com.example.nivelver20.ui.theme.AdaptiveDimensions
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF003D5B),
        title = {
            Text(
                text = "¿SALIR DEL TEST?",
                fontSize = dimensions.loginLabelFontSize.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFa3b944),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Text(
                text = "¿Estás seguro de que quieres salir?\nTu progreso se perderá.",
                fontSize = (dimensions.exitDialog).sp,
                color = Color(0xFFf2edd0),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(dimensions.spaceBetweenButtons)
            ) {
                // Кнопка CONTINUAR (зеленая)
                Box(
                    modifier = Modifier
                        .weight(1f)
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
                        onClick = onDismiss,
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
                            text = "continuar",
                            fontSize = dimensions.exitDialog.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFf2edd0),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Кнопка SÍ, SALIR (красная)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(dimensions.bottomButtonHeight)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFFC42D2C),
                                    Color(0xFFC42D2C)
                                )
                            ),
                            shape = RoundedCornerShape(dimensions.buttonCornerRadius)
                        )
                ) {
                    Button(
                        onClick = onConfirm,
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
                            text = "si, salir",
                            fontSize = dimensions.exitDialog.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFf2edd0),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        },
        dismissButton = null
    )
}

@Composable
private fun WordCardItem(
    card: WordCard?,
    onClick: () -> Unit,
    dimensions: com.example.nivelver20.ui.theme.AdaptiveDimensions,
    modifier: Modifier = Modifier
) {
    if (card == null) return

    val borderColor = when (card.state) {
        CardState.NORMAL -> Color.Transparent
        CardState.SELECTED -> Color(0xCB02214A)
        CardState.SHOWING_SUCCESS -> Color(0xFF48C553)
        CardState.INCORRECT -> Color(0xFFC42D2C)
        CardState.MATCHED -> Color.Transparent
    }

    val backgroundColor = when (card.state) {
        CardState.SHOWING_SUCCESS -> Color(0xFFCCCCCC)
        CardState.MATCHED -> Color(0xFFCCCCCC)
        else -> Color(0xFFf2edd0)
    }

    val textColor = when (card.state) {
        CardState.SHOWING_SUCCESS -> Color.Gray
        CardState.MATCHED -> Color.Gray
        else -> Color(0xFF003D5B)
    }

    val isClickable = card.state != CardState.MATCHED && card.state != CardState.SHOWING_SUCCESS

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
            color = textColor,
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