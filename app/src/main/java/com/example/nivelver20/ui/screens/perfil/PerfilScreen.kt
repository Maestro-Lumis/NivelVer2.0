package com.example.nivelver20.ui.screens.perfil

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.nivelver20.data.session.SessionManager
import com.example.nivelver20.ui.theme.rememberAdaptiveDimensions

@Composable
fun PerfilScreen(
    nivel: String,
    onNavigateToNivel: () -> Unit = {},
    onNavigateToFlujo: () -> Unit = {},
    onNavigateToVocabulario: () -> Unit = {},
    onNavigateToGrammatica: () -> Unit = {},
    onNavigateToAudio: () -> Unit = {},
    onNavigateToLectura: () -> Unit = {},
    onNavigateToTest: () -> Unit = {},
    onNavigateToPerfil: () -> Unit = {},
    onLogout: () -> Unit = {},
    viewModel: PerfilViewModel = viewModel()
) {
    val dimensions = rememberAdaptiveDimensions()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val sessionManager = SessionManager.getInstance(context)
    val currentUsername by sessionManager.currentUsername.collectAsState()

    // Получаем картинку для уровня
    val nivelImageRes = when (nivel) {
        "A1" -> R.drawable.a1
        "A2" -> R.drawable.a2
        "B1" -> R.drawable.b1
        "B2" -> R.drawable.b2
        else -> R.drawable.a0
    }

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
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.espanol_logo),
            contentDescription = "Letter Ñ",
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

            Spacer(modifier = Modifier.height(dimensions.perfilTopPadding))

            // Иконка выхода в правом верхнем углу
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopEnd
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = "Cerrar sesión",
                    tint = Color(0xFFa3b944),
                    modifier = Modifier
                        .size(dimensions.perfilTitleFontSize.sp.value.dp)
                        .clickable {
                            sessionManager.logout()
                            onLogout()
                        }
                )
            }

            // Заголовок
            Text(
                text = uiState.title,
                fontSize = dimensions.perfilTitleFontSize.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFa3b944),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            // NOMBRE + NIVEL (картинка)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(dimensions.perfilSpacingBetweenButtons)
            ) {
                Text(
                    text = currentUsername ?: "Usuario",
                    fontSize = dimensions.perfilUsernameFontSize.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFa3b944),
                    textAlign = TextAlign.Center
                )

                // NIVEL как картинка
                Image(
                    painter = painterResource(id = nivelImageRes),
                    contentDescription = "Nivel $nivel",
                    modifier = Modifier
                        .size(dimensions.nivelImageSize)
                        .padding(vertical = 4.dp),
                    contentScale = ContentScale.Fit
                )
            }

            // Кнопки категорий
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(dimensions.perfilSpacingBetweenButtons)
            ) {
                // NIVEL
                PerfilButton(
                    text = uiState.nivelButton,
                    onClick = onNavigateToNivel,
                    gradient = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFFF6EC7),
                            Color(0xFFFFE97D)
                        )
                    ),
                    dimensions = dimensions
                )

                // FLUJO
                PerfilButton(
                    text = uiState.flujoButton,
                    onClick = onNavigateToFlujo,
                    gradient = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFFFE97D),
                            Color(0xFFFFB347)
                        )
                    ),
                    dimensions = dimensions
                )

                // VOCABULARIO
                PerfilButton(
                    text = uiState.vocabularioButton,
                    onClick = onNavigateToVocabulario,
                    gradient = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFA0D47B),
                            Color(0xFFE0CA71)
                        )
                    ),
                    dimensions = dimensions
                )

                // GRAMMATICA
                PerfilButton(
                    text = uiState.grammaticaButton,
                    onClick = onNavigateToGrammatica,
                    gradient = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF0097B2),
                            Color(0xFF7ED957)
                        )
                    ),
                    dimensions = dimensions
                )

                // AUDIO
                PerfilButton(
                    text = uiState.audioButton,
                    onClick = onNavigateToAudio,
                    gradient = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF5DE0E6),
                            Color(0xFF1C74CF)
                        )
                    ),
                    dimensions = dimensions
                )

                // LECTURA
                PerfilButton(
                    text = uiState.lecturaButton,
                    onClick = onNavigateToLectura,
                    gradient = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFA985F0),
                            Color(0xFF85EDFF)
                        )
                    ),
                    dimensions = dimensions
                )
            }

            Spacer(modifier = Modifier.height(dimensions.perfilBottomPadding))

            // Нижняя часть: TEST и PERFIL
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = dimensions.perfilBottomPadding),
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
private fun PerfilButton(
    text: String,
    gradient: Brush,
    onClick: () -> Unit,
    dimensions: com.example.nivelver20.ui.theme.AdaptiveDimensions
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(dimensions.perfilButtonHeight),
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
                fontSize = dimensions.perfilButtonFontSize.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C3E50),
                textAlign = TextAlign.Center
            )
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