package com.example.nivelver20.ui.screens.grammar

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
fun GrammarScreen(
    nivel : String,
    onNavigateToTest: () -> Unit = {},
    onNavigateToPerfil: () -> Unit = {},
    onNavigateToResults: (String, Int, Int) -> Unit = { _, _, _ -> },
    viewModel: GrammarViewModel = viewModel()
) {
    val dimensions = rememberAdaptiveDimensions()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(nivel) {
        viewModel.setNivel(nivel)
        viewModel.setResultsCallback { n, correct, incorrect ->
            onNavigateToResults(n, correct, incorrect)
        }
    }

    BackHandler {
        viewModel.requestExit(onNavigateToTest)
    }

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
        if (uiState.isLoading) {
            CircularProgressIndicator(
                color = Color(0xFFa3b944),
                modifier = Modifier.size(48.dp)
            )
        }

        if (uiState.errorMessage != null) {
            Text(
                text = uiState.errorMessage ?: "",
                color = Color(0xFFC42D2C),
                fontSize = dimensions.vocabularioTitleFontSize.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(32.dp)
            )
        }

        if (!uiState.isLoading && uiState.errorMessage == null) {
            Image(
                painter = painterResource(id = R.drawable.espanol_logo),
                contentDescription = "Background",
                modifier = Modifier
                    .fillMaxWidth(0.8f)
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

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF0097B2),
                                Color(0xFF7ED957)
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

                    Text(
                        text = uiState.title,
                        fontSize = dimensions.vocabularioTitleFontSize.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFf2edd0),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(dimensions.vocabularioPadingH))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        when (uiState.currentQuestion?.tipo) {
                            "multiple_choice" -> MultipleChoiceView(
                                uiState = uiState,
                                onAnswerClick = { viewModel.onAnswerClick(it) },
                                dimensions = dimensions
                            )
                            "error_correction" -> ErrorCorrectionView(
                                uiState = uiState,
                                onAnswerClick = { viewModel.onAnswerClick(it) },
                                dimensions = dimensions
                            )
                            "drag_drop" -> DragDropView(
                                uiState = uiState,
                                onWordClick = { viewModel.onDragDropWordClick(it) },
                                onClear = { viewModel.onDragDropClear() },
                                onSubmit = { viewModel.onDragDropSubmit() },
                                dimensions = dimensions
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(dimensions.vocabularioPadding / 2))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
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

                    Spacer(modifier = Modifier.height(dimensions.vocabularioPadding / 2))
                }
            }

            Spacer(modifier = Modifier.height(dimensions.verticalPadding))

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
private fun MultipleChoiceView(
    uiState: GrammarUiState,
    onAnswerClick: (Int) -> Unit,
    dimensions: com.example.nivelver20.ui.theme.AdaptiveDimensions
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Text(
            text = uiState.currentQuestion?.pregunta ?: "",
            fontSize = dimensions.grammarQuestionFontSize.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFf2edd0),
            textAlign = TextAlign.Center,
            lineHeight = dimensions.lineHeightForAudAndLect,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(dimensions.grammarSpacingBetweenSections)
        ) {
            uiState.answers.forEachIndexed { index, answer ->
                AnswerItem(
                    answer = answer,
                    onClick = { onAnswerClick(index) },
                    dimensions = dimensions
                )
            }
        }
    }
}

@Composable
private fun ErrorCorrectionView(
    uiState: GrammarUiState,
    onAnswerClick: (Int) -> Unit,
    dimensions: com.example.nivelver20.ui.theme.AdaptiveDimensions
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimensions.grammarSpacingBetweenSections)
        ) {
            Text(
                text = uiState.currentQuestion?.pregunta ?: "",
                fontSize = dimensions.grammarQuestionFontSize.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFf2edd0),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            )

            Text(
                text = "\"${uiState.currentQuestion?.fraseIncorrecta ?: ""}\"",
                fontSize = dimensions.grammarAnswerFontSize.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFFf2edd0),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            )

            Text(
                text = "¿Cuál es la forma correcta?",
                fontSize = dimensions.grammarAnswerFontSize.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFf2edd0),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(dimensions.grammarSpacingBetweenSections)
        ) {
            uiState.answers.forEachIndexed { index, answer ->
                AnswerItem(
                    answer = answer,
                    onClick = { onAnswerClick(index) },
                    dimensions = dimensions
                )
            }
        }
    }
}

@Composable
private fun DragDropView(
    uiState: GrammarUiState,
    onWordClick: (String) -> Unit,
    onClear: () -> Unit,
    onSubmit: () -> Unit,
    dimensions: com.example.nivelver20.ui.theme.AdaptiveDimensions
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimensions.grammarSpacingBetweenSections)
        ) {
            Text(
                text = uiState.currentQuestion?.pregunta ?: "",
                fontSize = dimensions.grammarQuestionFontSize.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFf2edd0),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = dimensions.grammarDragDropBoxHeight)
                    .wrapContentHeight()
                    .background(
                        color = Color(0xFFF5F5DC),
                        shape = RoundedCornerShape(dimensions.vocabularioCardCornerRadius)
                    )
                    .padding(dimensions.grammarAnswerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = uiState.userDragDropAnswer.ifEmpty { "Toca las palabras..." },
                    fontSize = dimensions.grammarAnswerFontSize.sp,
                    color = if (uiState.userDragDropAnswer.isEmpty()) Color.Gray else Color(0xFF003D5B),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(dimensions.grammarSpacingBetweenSections)
        ) {
            val words = uiState.dragDropWords
            val wordCount = words.size

            val columnsCount = when {
                dimensions.screenType == com.example.nivelver20.ui.theme.ScreenType.SMALL_PHONE ||
                        dimensions.screenType == com.example.nivelver20.ui.theme.ScreenType.MEDIUM_PHONE -> 2
                dimensions.screenType == com.example.nivelver20.ui.theme.ScreenType.LARGE_PHONE ||
                        dimensions.screenType == com.example.nivelver20.ui.theme.ScreenType.XLARGE_PHONE -> {
                    when {
                        wordCount <= 6 -> 2
                        else -> 3
                    }
                }
                else -> 3
            }

            words.chunked(columnsCount).forEach { rowWords ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(dimensions.grammarSpacingBetweenSections)
                ) {
                    rowWords.forEach { word ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(dimensions.grammarWordButtonHeight)
                                .background(
                                    color = Color(0xFFa3b944),
                                    shape = RoundedCornerShape(dimensions.vocabularioCardCornerRadius)
                                )
                                .clickable { onWordClick(word) }
                                .padding(1.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = word,
                                fontSize = dimensions.grammarWordButtonFontSize.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF02214a),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    repeat(columnsCount - rowWords.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(dimensions.spaceBetweenButtons)
        ) {
            Button(
                onClick = onClear,
                modifier = Modifier
                    .weight(1f)
                    .height(dimensions.grammarWordButtonHeight),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFC42D2C)
                ),
                shape = RoundedCornerShape(dimensions.buttonCornerRadius)
            ) {
                Text(
                    "BORRAR",
                    color = Color.White,
                    fontSize = dimensions.grammarWordButtonFontSize.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = onSubmit,
                modifier = Modifier
                    .weight(1f)
                    .height(dimensions.grammarWordButtonHeight),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF48C553)
                ),
                shape = RoundedCornerShape(dimensions.buttonCornerRadius)
            ) {
                Text(
                    "ENVIAR",
                    color = Color.White,
                    fontSize = dimensions.grammarWordButtonFontSize.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun AnswerItem(
    answer: GrammarAnswerItem?,
    onClick: () -> Unit,
    dimensions: com.example.nivelver20.ui.theme.AdaptiveDimensions
) {
    if (answer == null) return

    val borderColor = when (answer.state) {
        GrammarAnswerState.NORMAL -> Color.Transparent
        GrammarAnswerState.SELECTED -> Color(0xCB02214A)
        GrammarAnswerState.SHOWING_SUCCESS -> Color(0xFF48C553)
        GrammarAnswerState.INCORRECT -> Color(0xFFC42D2C)
        GrammarAnswerState.MATCHED -> Color.Transparent
    }

    val backgroundColor = when (answer.state) {
        GrammarAnswerState.SHOWING_SUCCESS -> Color(0xFFCCCCCC)
        GrammarAnswerState.MATCHED -> Color(0xFFCCCCCC)
        else -> Color(0xFFF5F5DC)
    }

    val textColor = when (answer.state) {
        GrammarAnswerState.SHOWING_SUCCESS -> Color.Gray
        GrammarAnswerState.MATCHED -> Color.Gray
        else -> Color(0xFF003D5B)
    }

    val isClickable = answer.state != GrammarAnswerState.MATCHED && answer.state != GrammarAnswerState.SHOWING_SUCCESS

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = dimensions.grammarAnswerMinHeight)
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(dimensions.vocabularioCardCornerRadius)
            )
            .then(
                if (borderColor != Color.Transparent) {
                    Modifier.border(
                        width = 5.dp,
                        color = borderColor,
                        shape = RoundedCornerShape(dimensions.vocabularioCardCornerRadius)
                    )
                } else Modifier
            )
            .clickable(enabled = isClickable) { onClick() }
            .padding(vertical = dimensions.grammarAnswerPadding, horizontal = dimensions.grammarAnswerPadding * 2),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = answer.text,
            fontSize = dimensions.grammarAnswerFontSize.sp,
            fontWeight = FontWeight.Normal,
            color = textColor,
            textAlign = TextAlign.Center
        )
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