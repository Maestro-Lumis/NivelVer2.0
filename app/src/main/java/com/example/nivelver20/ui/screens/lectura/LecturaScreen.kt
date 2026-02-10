package com.example.nivelver20.ui.screens.lectura

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
import androidx.compose.ui.draw.alpha
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
fun LecturaScreen(
    nivel : String,
    onNavigateToTest: () -> Unit = {},
    onNavigateToPerfil: () -> Unit = {},
    onNavigateToResults: (String, Int, Int) -> Unit = { _, _, _ -> },
    viewModel: LecturaViewModel = viewModel()
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
                contentDescription = "Letter Ñ",
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
                                Color(0xFFA985F0),
                                Color(0xFF85edff)
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
                            .background(
                                color = Color(0x40FFFFFF),
                                shape = RoundedCornerShape(dimensions.vocabularioCardCornerRadius)
                            )
                            .padding(dimensions.vocabularioPadding)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = uiState.text,
                                fontSize = dimensions.vocabularioWordFontSize.sp,
                                color = Color(0xFFf2edd0),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(dimensions.vocabularioCardSpacing))

                    Text(
                        text = uiState.question,
                        fontSize = dimensions.vocabularioWordFontSize.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFf2edd0),
                        textAlign = TextAlign.Center,
                        lineHeight = dimensions.lineHeightForAudAndLect,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(dimensions.vocabularioCardSpacing))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(dimensions.vocabularioCardSpacing/2)
                    ) {
                        uiState.answers.forEachIndexed { index, answer ->
                            AnswerItem(
                                answer = answer,
                                onClick = { viewModel.onAnswerClick(index) },
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
private fun AnswerItem(
    answer: AnswerItem?,
    onClick: () -> Unit,
    dimensions: com.example.nivelver20.ui.theme.AdaptiveDimensions
) {
    if (answer == null) return

    val borderColor = when (answer.state) {
        AnswerState.NORMAL -> Color.Transparent
        AnswerState.SELECTED -> Color(0xCB02214A)
        AnswerState.SHOWING_SUCCESS -> Color(0xFF48C553)
        AnswerState.INCORRECT -> Color(0xFFC42D2C)
        AnswerState.MATCHED -> Color.Transparent
    }

    val backgroundColor = when (answer.state) {
        AnswerState.SHOWING_SUCCESS -> Color(0xFFCCCCCC)
        AnswerState.MATCHED -> Color(0xFFCCCCCC)
        else -> Color(0xFFF5F5DC)
    }

    val textColor = when (answer.state) {
        AnswerState.SHOWING_SUCCESS -> Color.Gray
        AnswerState.MATCHED -> Color.Gray
        else -> Color(0xFF003D5B)
    }

    val isClickable = answer.state != AnswerState.MATCHED && answer.state != AnswerState.SHOWING_SUCCESS

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
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
            .padding(horizontal = dimensions.vocabularioPadding / 2, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = answer.text,
            fontSize = dimensions.lecturaAnswerFontSize.sp,
            fontWeight = FontWeight.Normal,
            color = textColor,
            textAlign = TextAlign.Center,
            style = androidx.compose.ui.text.TextStyle(
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
                .wrapContentHeight()
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