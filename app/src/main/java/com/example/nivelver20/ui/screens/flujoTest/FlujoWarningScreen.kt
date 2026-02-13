package com.example.nivelver20.ui.screens.flujoTest

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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

@Composable
fun FlujoWarningScreen(
    onStart: () -> Unit,
    onNavigateToTest: () -> Unit,
    onNavigateToPerfil: () -> Unit,
    viewModel: FlujoViewModel
) {
    val dimensions = rememberAdaptiveDimensions()
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF02214a)),
        contentAlignment = Alignment.Center
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
                .padding(horizontal = dimensions.horizontalPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(dimensions.verticalPadding))

            Text(
                text = "TEST DE FLUJO",
                fontSize = dimensions.flujoWarningTitleFontSize.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFa3b944),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(dimensions.verticalPadding))

            // Main content box - takes all available space
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
                    .padding(dimensions.vocabularioPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(dimensions.spaceBetweenButtons / 2)
                ) {
                    uiState.warningInfoList.forEach { (icon, text) ->
                        InfoRow(
                            icon = icon,
                            text = text,
                            dimensions = dimensions
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(dimensions.verticalPadding))

            // COMENZAR button
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
                    onClick = onStart,
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
                        text = "COMENZAR",
                        fontSize = dimensions.bottomButtonFontSize.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFf2edd0),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(dimensions.spaceBetweenButtons))

            // TEST and PERFIL buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = dimensions.verticalPadding),
                horizontalArrangement = Arrangement.spacedBy(dimensions.spaceBetweenButtons)
            ) {
                BottomButton(
                    text = "TEST",
                    onClick = onNavigateToTest,
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
private fun InfoRow(
    icon: String,
    text: String,
    dimensions: com.example.nivelver20.ui.theme.AdaptiveDimensions
) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = icon,
            fontSize = dimensions.flujoWarningIconFontSize.sp,
            modifier = Modifier.padding(end = 6.dp)
        )
        Text(
            text = text,
            fontSize = dimensions.flujoWarningTextFontSize.sp,
            color = Color(0xFFf2edd0),
            lineHeight = (dimensions.flujoWarningTextFontSize * 1.3f).sp
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