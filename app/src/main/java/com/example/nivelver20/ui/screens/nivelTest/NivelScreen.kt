package com.example.nivelver20.ui.screens.nivelTest

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nivelver20.R
import com.example.nivelver20.data.repository.FirestoreRepository
import com.example.nivelver20.data.session.SessionManager
import com.example.nivelver20.ui.theme.rememberAdaptiveDimensions

@Composable
fun NivelScreen(
    nivel: String,
    onNavigateToTest: () -> Unit = {},
    onNavigateToPerfil: () -> Unit = {},
    onNavigateToResults: (String, Int, Int) -> Unit = { _, _, _ -> },
    viewModel: NivelViewModel = viewModel()
) {
    val dimensions = rememberAdaptiveDimensions()
    val context = LocalContext.current
    val sessionManager = remember { SessionManager.getInstance(context) }
    val repository = remember { FirestoreRepository.getInstance() }

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(nivel) {
        viewModel.loadNivelTest(nivel)
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

            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Левый текст
                Text(
                    text = uiState.nivel,
                    fontSize = dimensions.loginLabelFontSize.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFa3b944),
                    modifier = Modifier.align(Alignment.CenterStart)
                )

                // Центральный текст
                Text(
                    text = "${uiState.currentQuestionIndex + 1}/${uiState.totalQuestions}",
                    fontSize = dimensions.loginLabelFontSize.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFa3b944),
                    modifier = Modifier.align(Alignment.Center)
                )

                // Правый текст
                Text(
                    text = uiState.userName,
                    fontSize = dimensions.loginLabelFontSize.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFa3b944),
                    modifier = Modifier.align(Alignment.CenterEnd)
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
                                Color(0xFFFF6EC7),
                                Color(0xFFFFE97D)
                            )
                        ),
                        alpha = 0.55f,
                        shape = RoundedCornerShape(dimensions.buttonCornerRadius)
                    )
            ) {
                when (uiState.currentQuestion?.type) {
                    NivelQuestionType.VOCABULARIO -> VocabContent(
                        uiState = uiState,
                        onSpanishClick = { viewModel.onSpanishCardClick(it) },
                        onRussianClick = { viewModel.onRussianCardClick(it) },
                        dimensions = dimensions
                    )
                    NivelQuestionType.GRAMMAR -> GrammarContent(
                        uiState = uiState,
                        onAnswerClick = { viewModel.onAnswerClick(it) },
                        onWordClick = { viewModel.onDragDropWordClick(it) },
                        onClear = { viewModel.onDragDropClear() },
                        onSubmit = { viewModel.onDragDropSubmit() },
                        dimensions = dimensions
                    )
                    NivelQuestionType.AUDIO -> AudioContent(
                        uiState = uiState,
                        onAnswerClick = { viewModel.onAnswerClick(it) },
                        onPlayPause = { viewModel.togglePlayPause() },
                        onSliderChange = { viewModel.onSliderValueChange(it) },
                        onSliderFinished = { viewModel.onSliderValueChangeFinished() },
                        dimensions = dimensions
                    )
                    NivelQuestionType.LECTURA -> LecturaContent(
                        uiState = uiState,
                        onAnswerClick = { viewModel.onAnswerClick(it) },
                        dimensions = dimensions
                    )
                    null -> {}
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

@Composable
private fun VocabContent(
    uiState: NivelUiState,
    onSpanishClick: (Int) -> Unit,
    onRussianClick: (Int) -> Unit,
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

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(dimensions.vocabularioCardSpacing)
        ) {
            // Spanish cards (4 rows × 2 columns)
            uiState.spanishCards.chunked(2).forEachIndexed { rowIndex, rowCards ->
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

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(dimensions.vocabularioCardSpacing)
        ) {
            // Russian cards (4 rows × 2 columns)
            uiState.russianCards.chunked(2).forEachIndexed { rowIndex, rowCards ->
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

@Composable
private fun VocabCard(
    card: NivelWordCard,
    onClick: () -> Unit,
    dimensions: com.example.nivelver20.ui.theme.AdaptiveDimensions,
    modifier: Modifier = Modifier
) {
    val borderColor = when (card.state) {
        NivelCardState.NORMAL -> Color.Transparent
        NivelCardState.SELECTED -> Color(0xCB02214A)
        NivelCardState.SHOWING_SUCCESS -> Color(0xFF48C553)
        NivelCardState.INCORRECT -> Color(0xFFC42D2C)
        NivelCardState.MATCHED -> Color.Transparent
    }

    val backgroundColor = when (card.state) {
        NivelCardState.SHOWING_SUCCESS -> Color(0xFFCCCCCC)
        NivelCardState.MATCHED -> Color(0xFFCCCCCC)
        else -> Color(0xFFf2edd0)
    }

    val textColor = when (card.state) {
        NivelCardState.SHOWING_SUCCESS -> Color.Gray
        NivelCardState.MATCHED -> Color.Gray
        else -> Color(0xFF003D5B)
    }

    val isClickable = card.state != NivelCardState.MATCHED && card.state != NivelCardState.SHOWING_SUCCESS

    val wordText = if (card.isSpanish) card.spanish else card.russian
    val adaptiveFontSize = when {
        wordText.length > 12 -> dimensions.vocabularioWordFontSize * 0.65f
        wordText.length > 8  -> dimensions.vocabularioWordFontSize * 0.80f
        else                 -> dimensions.vocabularioWordFontSize
    }
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
            text = wordText,
            fontSize = adaptiveFontSize.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

@Composable
private fun GrammarContent(
    uiState: NivelUiState,
    onAnswerClick: (Int) -> Unit,
    onWordClick: (String) -> Unit,
    onClear: () -> Unit,
    onSubmit: () -> Unit,
    dimensions: com.example.nivelver20.ui.theme.AdaptiveDimensions
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(dimensions.vocabularioPadding),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
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
            when (uiState.currentQuestion?.grammarType) {
                "drag_drop" -> DragDropView(
                    uiState = uiState,
                    onWordClick = onWordClick,
                    onClear = onClear,
                    onSubmit = onSubmit,
                    dimensions = dimensions
                )
                "error_correction" -> ErrorCorrectionView(
                    uiState = uiState,
                    onAnswerClick = onAnswerClick,
                    dimensions = dimensions
                )
                else -> MultipleChoiceView(
                    uiState = uiState,
                    onAnswerClick = onAnswerClick,
                    dimensions = dimensions
                )
            }
        }

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
    }
}

@Composable
private fun MultipleChoiceView(
    uiState: NivelUiState,
    onAnswerClick: (Int) -> Unit,
    dimensions: com.example.nivelver20.ui.theme.AdaptiveDimensions
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Text(
            text = uiState.currentQuestion?.questionText ?: "",
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
            uiState.answers.forEachIndexed { index, answer ->
                AnswerItem(answer, { onAnswerClick(index) }, dimensions)
            }
        }
    }
}

@Composable
private fun ErrorCorrectionView(
    uiState: NivelUiState,
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
                text = uiState.currentQuestion?.questionText ?: "",
                fontSize = dimensions.grammarQuestionFontSize.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFf2edd0),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
            )

            Text(
                text = "\"${uiState.currentQuestion?.grammarIncorrectPhrase ?: ""}\"",
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
            uiState.answers.forEachIndexed { index, answer ->
                AnswerItem(answer, { onAnswerClick(index) }, dimensions)
            }
        }
    }
}

@Composable
private fun DragDropView(
    uiState: NivelUiState,
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
                text = uiState.currentQuestion?.questionText ?: "",
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
                    text = uiState.userDragDropAnswer.ifEmpty { "Toca las palabras..." },
                    fontSize = dimensions.grammarAnswerFontSize.sp,
                    color = if (uiState.userDragDropAnswer.isEmpty()) Color.Gray else Color(0xFF003D5B),
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
            val words = uiState.dragDropWords
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

@Composable
private fun AudioContent(
    uiState: NivelUiState,
    onAnswerClick: (Int) -> Unit,
    onPlayPause: () -> Unit,
    onSliderChange: (Float) -> Unit,
    onSliderFinished: () -> Unit,
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
                    imageVector = if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (uiState.isPlaying) "Pause" else "Play",
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
                        text = uiState.currentTimeText,
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
                        text = uiState.durationText,
                        fontSize = (dimensions.buttonFontSize * 0.9f).sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFf2edd0)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Slider(
                    value = uiState.currentPosition,
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
            text = uiState.currentQuestion?.questionText ?: "",
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
            uiState.answers.forEachIndexed { index, answer ->
                AnswerItem(answer, { onAnswerClick(index) }, dimensions)
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

@Composable
private fun LecturaContent(
    uiState: NivelUiState,
    onAnswerClick: (Int) -> Unit,
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
                text = uiState.currentQuestion?.lecturaText ?: "",
                fontSize = dimensions.vocabularioWordFontSize.sp,
                color = Color(0xFFf2edd0),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(dimensions.vocabularioCardSpacing))

        Text(
            text = uiState.currentQuestion?.questionText ?: "",
            fontSize = dimensions.lecturaAnswerFontSize.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFf2edd0),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(dimensions.vocabularioCardSpacing))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(dimensions.vocabularioCardSpacing / 2)
        ) {
            uiState.answers.forEachIndexed { index, answer ->
                AnswerItem(answer, { onAnswerClick(index) }, dimensions)
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
@Composable
private fun AnswerItem(
    answer: NivelAnswerItem,
    onClick: () -> Unit,
    dimensions: com.example.nivelver20.ui.theme.AdaptiveDimensions
) {
    val borderColor = when (answer.state) {
        NivelCardState.NORMAL -> Color.Transparent  // ← ПРАВИЛЬНЫЙ ENUM!
        NivelCardState.SELECTED -> Color(0xCB02214A)
        NivelCardState.SHOWING_SUCCESS -> Color(0xFF48C553)
        NivelCardState.INCORRECT -> Color(0xFFC42D2C)
        NivelCardState.MATCHED -> Color.Transparent
    }

    val backgroundColor = when (answer.state) {
        NivelCardState.SHOWING_SUCCESS -> Color(0xFFCCCCCC)
        NivelCardState.MATCHED -> Color(0xFFCCCCCC)
        else -> Color(0xFFF5F5DC)
    }

    val textColor = when (answer.state) {
        NivelCardState.SHOWING_SUCCESS -> Color.Gray
        NivelCardState.MATCHED -> Color.Gray
        else -> Color(0xFF003D5B)
    }

    val isClickable = answer.state != NivelCardState.MATCHED && answer.state != NivelCardState.SHOWING_SUCCESS

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(backgroundColor, RoundedCornerShape(dimensions.vocabularioCardCornerRadius))
            .then(
                if (borderColor != Color.Transparent) {
                    Modifier.border(5.dp, borderColor, RoundedCornerShape(dimensions.vocabularioCardCornerRadius))
                } else Modifier
            )
            .clickable(enabled = isClickable) { onClick() }
            .padding(horizontal = dimensions.vocabularioPadding / 2, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = answer.text,
            fontSize = dimensions.lecturaAnswerFontSize.sp,
            fontWeight = FontWeight.Normal,
            color = textColor,
            textAlign = TextAlign.Center,
            style = androidx.compose.ui.text.TextStyle(
                fontSize = dimensions.lecturaAnswerFontSize.sp,
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
                .wrapContentHeight()
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