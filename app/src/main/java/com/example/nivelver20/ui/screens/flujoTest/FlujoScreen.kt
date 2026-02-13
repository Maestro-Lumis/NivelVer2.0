package com.example.nivelver20.ui.screens.flujoTest

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nivelver20.R
import com.example.nivelver20.ui.theme.rememberAdaptiveDimensions
import androidx.navigation.NavController

@Composable
fun FlujoScreen(
    navController: NavController,
    viewModel: FlujoViewModel,
    onNavigateToTest: () -> Unit = {},
    onNavigateToPerfil: () -> Unit = {},
    userName: String = "NOMBRE"
) {
    val dimensions = rememberAdaptiveDimensions()
    val uiState by viewModel.uiState.collectAsState()

    // Warning screen state
    var showWarningScreen by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
    }

    BackHandler {
        if (!showWarningScreen) {
            viewModel.requestExit { navController.popBackStack() }
        } else {
            navController.popBackStack()
        }
    }

    // Show warning screen first
    if (showWarningScreen) {
        FlujoWarningScreen(
            onStart = {
                showWarningScreen = false
                viewModel.startTest()
            },
            onNavigateToTest = onNavigateToTest,
            onNavigateToPerfil = onNavigateToPerfil,
            viewModel = viewModel
        )
        return
    }

    // Navigate to results when complete
    if (uiState.isTestComplete) {
        LaunchedEffect(Unit) {
            navController.navigate(com.example.nivelver20.navigation.Routes.FlujoResults.route) {
                popUpTo(com.example.nivelver20.navigation.Routes.Flujo.route) { inclusive = true }
            }
        }
        return
    }

    // Show dialogs
    if (uiState.showExitDialog) {
        ExitDialog(
            onConfirm = { viewModel.confirmExit() },
            onDismiss = { viewModel.cancelExit() },
            dimensions = dimensions
        )
    }

    if (uiState.showLevelCompleteDialog) {
        LevelCompleteDialog(
            level = uiState.currentLevel,
            correct = uiState.correctInLevel,
            total = uiState.answeredInLevel,
            onContinue = { viewModel.continueToNextLevel() },
            onFinish = { viewModel.stopTest() },
            dimensions = dimensions
        )
    }

    if (uiState.showStopDialog) {
        StopDialog(
            level = uiState.currentLevel,
            onContinue = { viewModel.continueDespiteFailure() },
            onStop = { viewModel.stopTest() },
            dimensions = dimensions
        )
    }

    // Main test screen
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

        if (uiState.error != null) {
            Text(
                text = uiState.error ?: "",
                color = Color(0xFFC42D2C),
                fontSize = dimensions.vocabularioTitleFontSize.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(32.dp)
            )
        }

        if (!uiState.isLoading && uiState.error == null) {
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

            // Top row: Level, Progress, Username
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Левый текст (уровень)
                Text(
                    text = uiState.currentLevel,
                    fontSize = dimensions.loginLabelFontSize.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFa3b944),
                    modifier = Modifier.align(Alignment.CenterStart)
                )

                // Центральный текст
                Text(
                    text = "${uiState.currentQuestionIndex + 1}/4",
                    fontSize = dimensions.loginLabelFontSize.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFa3b944),
                    modifier = Modifier.align(Alignment.Center)
                )

                // Правый текст (имя)
                Text(
                    text = userName,
                    fontSize = dimensions.loginLabelFontSize.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFa3b944),
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }

            Spacer(modifier = Modifier.height(dimensions.verticalPadding))

            // Main content box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFFFF6EC7),
                                Color(0xFFFFE97D)
                            )
                        ),
                        alpha = 0.55f,
                        shape = RoundedCornerShape(dimensions.buttonCornerRadius)
                    )
            ) {
                when (uiState.currentQuestion) {
                    is FlujoQuestion.Vocabulario -> VocabContent(
                        word = (uiState.currentQuestion as FlujoQuestion.Vocabulario).word,
                        spanishCards = uiState.spanishCards,
                        russianCards = uiState.russianCards,
                        onSpanishClick = { viewModel.onSpanishCardClick(it) },
                        onRussianClick = { viewModel.onRussianCardClick(it) },
                        correctCount = uiState.correctInLevel,
                        incorrectCount = uiState.incorrectInLevel,
                        dimensions = dimensions
                    )
                    is FlujoQuestion.Grammar -> GrammarContent(
                        question = (uiState.currentQuestion as FlujoQuestion.Grammar).question,
                        answers = uiState.answers,
                        dragDropWords = uiState.dragDropWords,
                        userDragDropAnswer = uiState.userDragDropAnswer,
                        onAnswerClick = { viewModel.onAnswerClick(it) },
                        onWordClick = { viewModel.onDragDropWordClick(it) },
                        onClear = { viewModel.onDragDropClear() },
                        onSubmit = { viewModel.onDragDropSubmit() },
                        correctCount = uiState.correctInLevel,
                        incorrectCount = uiState.incorrectInLevel,
                        dimensions = dimensions
                    )
                    is FlujoQuestion.Audio -> AudioContent(
                        question = (uiState.currentQuestion as FlujoQuestion.Audio).question,
                        answers = uiState.answers,
                        isPlaying = uiState.isPlaying,
                        currentPosition = uiState.currentPosition,
                        currentTimeText = uiState.currentTimeText,
                        durationText = uiState.durationText,
                        onAnswerClick = { viewModel.onAnswerClick(it) },
                        onPlayPause = { viewModel.togglePlayPause() },
                        onSliderChange = { viewModel.onSliderValueChange(it) },
                        onSliderFinished = { viewModel.onSliderValueChangeFinished() },
                        correctCount = uiState.correctInLevel,
                        incorrectCount = uiState.incorrectInLevel,
                        dimensions = dimensions
                    )
                    is FlujoQuestion.Lectura -> LecturaContent(
                        question = (uiState.currentQuestion as FlujoQuestion.Lectura).question,
                        answers = uiState.answers,
                        onAnswerClick = { viewModel.onAnswerClick(it) },
                        correctCount = uiState.correctInLevel,
                        incorrectCount = uiState.incorrectInLevel,
                        dimensions = dimensions
                    )
                    null -> {}
                }
            }

            Spacer(modifier = Modifier.height(dimensions.verticalPadding))

            // Bottom buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = dimensions.verticalPadding),
                horizontalArrangement = Arrangement.spacedBy(dimensions.spaceBetweenButtons)
            ) {
                BottomButton(
                    text = "TEST",
                    onClick = { viewModel.requestExit(onNavigateToTest) },
                    dimensions = dimensions,
                    modifier = Modifier.weight(1f)
                )

                BottomButton(
                    text = "PERFIL",
                    onClick = { viewModel.requestExit(onNavigateToPerfil) },
                    dimensions = dimensions,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// ========== VOCABULARIO CONTENT ==========
@Composable
private fun VocabContent(
    word: FlujoWord,
    spanishCards: List<FlujoWordCard>,
    russianCards: List<FlujoWordCard>,
    onSpanishClick: (Int) -> Unit,
    onRussianClick: (Int) -> Unit,
    correctCount: Int,
    incorrectCount: Int,
    dimensions: com.example.nivelver20.ui.theme.AdaptiveDimensions
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(dimensions.vocabularioPadingH))

        Text(
            text = "VOCABULARIO",
            fontSize = dimensions.vocabularioTitleFontSize.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFf2edd0),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(dimensions.vocabularioPadingH))

        // Spanish cards (4 rows × 2 columns)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(dimensions.vocabularioCardSpacing)
        ) {
            spanishCards.chunked(2).forEachIndexed { rowIndex, rowCards ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(dimensions.vocabularioCardSpacing)
                ) {
                    rowCards.forEachIndexed { colIndex, card ->
                        val index = rowIndex * 2 + colIndex
                        VocabCard(
                            card = card,
                            onClick = { onSpanishClick(index) },
                            dimensions = dimensions,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(dimensions.vocabularioBlockSpacing))

        // Russian cards (4 rows × 2 columns)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(dimensions.vocabularioCardSpacing)
        ) {
            russianCards.chunked(2).forEachIndexed { rowIndex, rowCards ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(dimensions.vocabularioCardSpacing)
                ) {
                    rowCards.forEachIndexed { colIndex, card ->
                        val index = rowIndex * 2 + colIndex
                        VocabCard(
                            card = card,
                            onClick = { onRussianClick(index) },
                            dimensions = dimensions,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(dimensions.vocabularioPadingH))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Text(
                text = incorrectCount.toString(),
                fontSize = dimensions.vocabularioCounterFontSize.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFC42D2C)
            )

            Text(
                text = correctCount.toString(),
                fontSize = dimensions.vocabularioCounterFontSize.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF48C553)
            )
        }

        Spacer(modifier = Modifier.height(dimensions.vocabularioPadingH))
    }
}

@Composable
private fun VocabCard(
    card: FlujoWordCard,
    onClick: () -> Unit,
    dimensions: com.example.nivelver20.ui.theme.AdaptiveDimensions,
    modifier: Modifier = Modifier
) {
    val borderColor = when (card.state) {
        FlujoCardState.NORMAL -> Color.Transparent
        FlujoCardState.SELECTED -> Color(0xCB02214A)
        FlujoCardState.SHOWING_SUCCESS -> Color(0xFF48C553)
        FlujoCardState.INCORRECT -> Color(0xFFC42D2C)
        FlujoCardState.MATCHED -> Color.Transparent
    }

    val backgroundColor = when (card.state) {
        FlujoCardState.SHOWING_SUCCESS -> Color(0xFFCCCCCC)
        FlujoCardState.MATCHED -> Color(0xFFCCCCCC)
        else -> Color(0xFFf2edd0)
    }

    val textColor = when (card.state) {
        FlujoCardState.SHOWING_SUCCESS -> Color.Gray
        FlujoCardState.MATCHED -> Color.Gray
        else -> Color(0xFF003D5B)
    }

    val isClickable = card.state != FlujoCardState.MATCHED && card.state != FlujoCardState.SHOWING_SUCCESS

    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .then(
                if (borderColor != Color.Transparent) {
                    Modifier.border(5.dp, borderColor, RoundedCornerShape(8.dp))
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

// ========== GRAMMAR CONTENT ==========
@Composable
private fun GrammarContent(
    question: FlujoGrammarQuestion,
    answers: List<FlujoAnswerItem>,
    dragDropWords: List<String>,
    userDragDropAnswer: String,
    onAnswerClick: (Int) -> Unit,
    onWordClick: (String) -> Unit,
    onClear: () -> Unit,
    onSubmit: () -> Unit,
    correctCount: Int,
    incorrectCount: Int,
    dimensions: com.example.nivelver20.ui.theme.AdaptiveDimensions
) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(dimensions.vocabularioPadingH))
        Text(
            text = "GRAMÁTICA",
            fontSize = dimensions.vocabularioTitleFontSize.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFf2edd0),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (question.tipo) {
                "drag_drop" -> DragDropView(
                    questionText = question.pregunta,
                    dragDropWords = dragDropWords,
                    userDragDropAnswer = userDragDropAnswer,
                    onWordClick = onWordClick,
                    onClear = onClear,
                    onSubmit = onSubmit,
                    dimensions = dimensions
                )
                "error_correction" -> ErrorCorrectionView(
                    questionText = question.pregunta,
                    incorrectPhrase = question.fraseIncorrecta ?: "",
                    answers = answers,
                    onAnswerClick = onAnswerClick,
                    dimensions = dimensions
                )
                else -> MultipleChoiceView(
                    questionText = question.pregunta,
                    answers = answers,
                    onAnswerClick = onAnswerClick,
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
                text = incorrectCount.toString(),
                fontSize = dimensions.vocabularioCounterFontSize.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFC42D2C)
            )

            Text(
                text = correctCount.toString(),
                fontSize = dimensions.vocabularioCounterFontSize.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF48C553)
            )
        }

        Spacer(modifier = Modifier.height(dimensions.vocabularioPadding / 2))
    }
}

@Composable
private fun MultipleChoiceView(
    questionText: String,
    answers: List<FlujoAnswerItem>,
    onAnswerClick: (Int) -> Unit,
    dimensions: com.example.nivelver20.ui.theme.AdaptiveDimensions
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Text(
            text = questionText,
            fontSize = dimensions.grammarQuestionFontSize.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFf2edd0),
            textAlign = TextAlign.Center,
            lineHeight = dimensions.lineHeightForAudAndLect,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(dimensions.grammarSpacingBetweenSections)
        ) {
            answers.forEachIndexed { index, answer ->
                AnswerItem(answer, { onAnswerClick(index) }, dimensions)
            }
        }
    }
}

@Composable
private fun ErrorCorrectionView(
    questionText: String,
    incorrectPhrase: String,
    answers: List<FlujoAnswerItem>,
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
                text = questionText,
                fontSize = dimensions.grammarQuestionFontSize.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFf2edd0),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
            )

            Text(
                text = "\"$incorrectPhrase\"",
                fontSize = dimensions.grammarAnswerFontSize.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFFf2edd0),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
            )

            Text(
                text = "¿Cuál es la forma correcta?",
                fontSize = dimensions.grammarAnswerFontSize.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFf2edd0),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(dimensions.grammarSpacingBetweenSections)
        ) {
            answers.forEachIndexed { index, answer ->
                AnswerItem(answer, { onAnswerClick(index) }, dimensions)
            }
        }
    }
}

@Composable
private fun DragDropView(
    questionText: String,
    dragDropWords: List<String>,
    userDragDropAnswer: String,
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
        // Вопрос и поле для ответа
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimensions.grammarSpacingBetweenSections)
        ) {
            Text(
                text = questionText,
                fontSize = dimensions.grammarQuestionFontSize.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFf2edd0),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            )

            // Поле для ответа с адаптивной высотой
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
                    text = userDragDropAnswer.ifEmpty { "Toca las palabras..." },
                    fontSize = dimensions.grammarAnswerFontSize.sp,
                    color = if (userDragDropAnswer.isEmpty()) Color.Gray else Color(0xFF003D5B),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Слова для перетаскивания с прокруткой
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(dimensions.grammarSpacingBetweenSections)
        ) {
            val words = dragDropWords
            val wordCount = words.size

            // Адаптивное количество колонок
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

        // Кнопки BORRAR и ENVIAR
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


// ========== AUDIO CONTENT ==========
@Composable
private fun AudioContent(
    question: FlujoAudioQuestion,
    answers: List<FlujoAnswerItem>,
    isPlaying: Boolean,
    currentPosition: Float,
    currentTimeText: String,
    durationText: String,
    onAnswerClick: (Int) -> Unit,
    onPlayPause: () -> Unit,
    onSliderChange: (Float) -> Unit,
    onSliderFinished: () -> Unit,
    correctCount: Int,
    incorrectCount: Int,
    dimensions: com.example.nivelver20.ui.theme.AdaptiveDimensions
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(dimensions.vocabularioPadding))

        Text(
            text = "AUDIO",
            fontSize = dimensions.vocabularioTitleFontSize.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFf2edd0),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(dimensions.vocabularioPadding))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0x40FFFFFF), RoundedCornerShape(dimensions.vocabularioCardCornerRadius))
                .padding(dimensions.vocabularioPadding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Spacer(modifier = Modifier.weight(1f))

                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    modifier = Modifier.size(dimensions.audioVolumeUp).clickable { onPlayPause() },
                    tint = Color(0xFFf2edd0)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = currentTimeText,
                        fontSize = (dimensions.buttonFontSize * 0.9f).sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFa3b944)
                    )
                    Text(
                        text = " / ",
                        fontSize = (dimensions.buttonFontSize * 0.9f).sp,
                        color = Color(0xFFf2edd0).copy(alpha = 0.6f)
                    )
                    Text(
                        text = durationText,
                        fontSize = (dimensions.buttonFontSize * 0.9f).sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFf2edd0)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Slider(
                    value = currentPosition,
                    onValueChange = onSliderChange,
                    onValueChangeFinished = onSliderFinished,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFFa3b944),
                        activeTrackColor = Color(0xFFa3b944),
                        inactiveTrackColor = Color(0xFFf2edd0).copy(alpha = 0.3f)
                    )
                )

                Spacer(modifier = Modifier.weight(1f))
            }
        }

        Spacer(modifier = Modifier.height(dimensions.vocabularioCardSpacing))

        Text(
            text = question.pregunta,
            fontSize = dimensions.audioQuestion.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFf2edd0),
            textAlign = TextAlign.Center,
            lineHeight = dimensions.lineHeightForAudAndLect,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(dimensions.vocabularioCardSpacing))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(dimensions.vocabularioCardSpacing / 2)
        ) {
            answers.forEachIndexed { index, answer ->
                AnswerItem(answer, { onAnswerClick(index) }, dimensions)
            }
        }

        Spacer(modifier = Modifier.height(dimensions.vocabularioPadding / 2))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(
                text = incorrectCount.toString(),
                fontSize = dimensions.vocabularioCounterFontSize.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFC42D2C)
            )

            Text(
                text = correctCount.toString(),
                fontSize = dimensions.vocabularioCounterFontSize.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF48C553)
            )
        }

        Spacer(modifier = Modifier.height(dimensions.vocabularioPadding / 2))
    }
}

// ========== LECTURA CONTENT ==========
@Composable
private fun LecturaContent(
    question: FlujoLecturaQuestion,
    answers: List<FlujoAnswerItem>,
    onAnswerClick: (Int) -> Unit,
    correctCount: Int,
    incorrectCount: Int,
    dimensions: com.example.nivelver20.ui.theme.AdaptiveDimensions
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(dimensions.vocabularioPadingH))

        Text(
            text = "LECTURA",
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
                .background(Color(0x40FFFFFF), RoundedCornerShape(dimensions.vocabularioCardCornerRadius))
                .padding(dimensions.vocabularioPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = question.texto,
                fontSize = dimensions.vocabularioWordFontSize.sp,
                color = Color(0xFFf2edd0),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(dimensions.vocabularioCardSpacing))

        Text(
            text = question.pregunta,
            fontSize = dimensions.audioQuestion.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFf2edd0),
            textAlign = TextAlign.Center,
            lineHeight = dimensions.lineHeightForAudAndLect,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(dimensions.vocabularioCardSpacing))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(dimensions.vocabularioCardSpacing / 2)
        ) {
            answers.forEachIndexed { index, answer ->
                AnswerItem(answer, { onAnswerClick(index) }, dimensions)
            }
        }

        Spacer(modifier = Modifier.height(dimensions.vocabularioPadding / 2))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(
                text = incorrectCount.toString(),
                fontSize = dimensions.vocabularioCounterFontSize.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFC42D2C)
            )

            Text(
                text = correctCount.toString(),
                fontSize = dimensions.vocabularioCounterFontSize.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF48C553)
            )
        }

        Spacer(modifier = Modifier.height(dimensions.vocabularioPadding / 2))
    }
}

// ========== ANSWER ITEM ==========
@Composable
private fun AnswerItem(
    answer: FlujoAnswerItem,
    onClick: () -> Unit,
    dimensions: com.example.nivelver20.ui.theme.AdaptiveDimensions
) {
    val borderColor = when (answer.state) {
        FlujoCardState.NORMAL -> Color.Transparent
        FlujoCardState.SELECTED -> Color(0xCB02214A)
        FlujoCardState.SHOWING_SUCCESS -> Color(0xFF48C553)
        FlujoCardState.INCORRECT -> Color(0xFFC42D2C)
        FlujoCardState.MATCHED -> Color.Transparent
    }

    val backgroundColor = when (answer.state) {
        FlujoCardState.SHOWING_SUCCESS -> Color(0xFFCCCCCC)
        FlujoCardState.MATCHED -> Color(0xFFCCCCCC)
        else -> Color(0xFFF5F5DC)
    }

    val textColor = when (answer.state) {
        FlujoCardState.SHOWING_SUCCESS -> Color.Gray
        FlujoCardState.MATCHED -> Color.Gray
        else -> Color(0xFF003D5B)
    }

    val isClickable = answer.state != FlujoCardState.MATCHED && answer.state != FlujoCardState.SHOWING_SUCCESS

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()  // ← ИЗМЕНЕНО: было heightIn(dimensions.answerItemMinHeight)
            .background(backgroundColor, RoundedCornerShape(dimensions.vocabularioCardCornerRadius))
            .then(
                if (borderColor != Color.Transparent) {
                    Modifier.border(5.dp, borderColor, RoundedCornerShape(dimensions.vocabularioCardCornerRadius))
                } else Modifier
            )
            .clickable(enabled = isClickable) { onClick() }
            .padding(horizontal = dimensions.vocabularioPadding / 2, vertical = 8.dp),  // ← ИЗМЕНЕНО
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = answer.text,
            fontSize = dimensions.lecturaAnswerFontSize.sp,  // ← ИЗМЕНЕНО: был vocabularioWordFontSize
            fontWeight = FontWeight.Normal,
            color = textColor,
            textAlign = TextAlign.Center,
            style = androidx.compose.ui.text.TextStyle(  // ← ДОБАВЛЕНО: весь style блок
                fontSize = dimensions.audioWordFontSize.sp,
                lineHeight = (dimensions.lecturaAnswerFontSize * 1.2f).sp,
                platformStyle = androidx.compose.ui.text.PlatformTextStyle(
                    includeFontPadding = false
                ),
                lineHeightStyle = androidx.compose.ui.text.style.LineHeightStyle(
                    alignment = androidx.compose.ui.text.style.LineHeightStyle.Alignment.Center,
                    trim = androidx.compose.ui.text.style.LineHeightStyle.Trim.Both
                )
            ),
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()  // ← ДОБАВЛЕНО
        )
    }
}

// ========== DIALOGS ==========
@Composable
private fun LevelCompleteDialog(
    level: String,
    correct: Int,
    total: Int,
    onContinue: () -> Unit,
    onFinish: () -> Unit,
    dimensions: com.example.nivelver20.ui.theme.AdaptiveDimensions
) {
    AlertDialog(
        onDismissRequest = {},
        containerColor = Color(0xFF003D5B),
        title = {
            Text(
                text = "¡Nivel $level completado!",
                fontSize = dimensions.loginLabelFontSize.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFa3b944),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Resultado: $correct/$total correctas",
                    fontSize = dimensions.exitDialog.sp,
                    color = Color(0xFFf2edd0),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "¿Continuar al siguiente nivel?",
                    fontSize = dimensions.exitDialog.sp,
                    color = Color(0xFFf2edd0),
                    textAlign = TextAlign.Center
                )
            }
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
                            brush = Brush.horizontalGradient(listOf(Color(0xFF48C553), Color(0xFF48C553))),
                            shape = RoundedCornerShape(dimensions.buttonCornerRadius)
                        )
                ) {
                    Button(
                        onClick = onContinue,
                        modifier = Modifier.fillMaxSize().padding(2.dp),
                        shape = RoundedCornerShape(dimensions.buttonCornerRadius),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF02214a)),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "Continuar",
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
                            brush = Brush.horizontalGradient(listOf(Color(0xFFA985F0), Color(0xFF85EDFF))),
                            shape = RoundedCornerShape(dimensions.buttonCornerRadius)
                        )
                ) {
                    Button(
                        onClick = onFinish,
                        modifier = Modifier.fillMaxSize().padding(2.dp),
                        shape = RoundedCornerShape(dimensions.buttonCornerRadius),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF003D5B)),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "Ver resultados",
                            fontSize = dimensions.exitDialog.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFa3b944),
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
private fun StopDialog(
    level: String,
    onContinue: () -> Unit,
    onStop: () -> Unit,
    dimensions: com.example.nivelver20.ui.theme.AdaptiveDimensions
) {
    AlertDialog(
        onDismissRequest = {},
        containerColor = Color(0xFF003D5B),
        title = {
            Text(
                text = "Tu nivel actual: $level",
                fontSize = dimensions.loginLabelFontSize.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFa3b944),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "No alcanzaste el puntaje necesario (3/4).",
                    fontSize = dimensions.exitDialog.sp,
                    color = Color(0xFFf2edd0),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "¿Quieres terminar o intentar el siguiente nivel?",
                    fontSize = dimensions.exitDialog.sp,
                    color = Color(0xFFf2edd0),
                    textAlign = TextAlign.Center
                )
            }
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
                            brush = Brush.horizontalGradient(listOf(Color(0xFF48C553), Color(0xFF48C553))),
                            shape = RoundedCornerShape(dimensions.buttonCornerRadius)
                        )
                ) {
                    Button(
                        onClick = onContinue,
                        modifier = Modifier.fillMaxSize().padding(2.dp),
                        shape = RoundedCornerShape(dimensions.buttonCornerRadius),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF02214a)),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "Continuar",
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
                            brush = Brush.horizontalGradient(listOf(Color(0xFFC42D2C), Color(0xFFC42D2C))),
                            shape = RoundedCornerShape(dimensions.buttonCornerRadius)
                        )
                ) {
                    Button(
                        onClick = onStop,
                        modifier = Modifier.fillMaxSize().padding(2.dp),
                        shape = RoundedCornerShape(dimensions.buttonCornerRadius),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF02214a)),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "Terminar",
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
                fontSize = dimensions.exitDialog.sp,
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
                            brush = Brush.horizontalGradient(listOf(Color(0xFF48C553), Color(0xFF48C553))),
                            shape = RoundedCornerShape(dimensions.buttonCornerRadius)
                        )
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxSize().padding(2.dp),
                        shape = RoundedCornerShape(dimensions.buttonCornerRadius),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF02214a)),
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
                            brush = Brush.horizontalGradient(listOf(Color(0xFFC42D2C), Color(0xFFC42D2C))),
                            shape = RoundedCornerShape(dimensions.buttonCornerRadius)
                        )
                ) {
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.fillMaxSize().padding(2.dp),
                        shape = RoundedCornerShape(dimensions.buttonCornerRadius),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF02214a)),
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
                brush = Brush.horizontalGradient(listOf(Color(0xFFA985F0), Color(0xFF85EDFF))),
                shape = RoundedCornerShape(dimensions.buttonCornerRadius)
            )
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier.fillMaxSize().padding(2.dp),
            shape = RoundedCornerShape(dimensions.buttonCornerRadius),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF003D5B)),
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