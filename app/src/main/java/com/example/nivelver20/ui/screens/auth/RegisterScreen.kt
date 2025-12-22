package com.example.nivelver20.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nivelver20.R
import com.example.nivelver20.ui.theme.rememberAdaptiveDimensions

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    onNavigateToTest: () -> Unit = {},
    onNavigateToPerfil: () -> Unit = {},
    viewModel: RegisterViewModel = viewModel()
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
        Image(
            painter = painterResource(id = R.drawable.espanol_logo),
            contentDescription = "Background",
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 40.dp),
            contentScale = ContentScale.Fit,
            alpha = 0.1f
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

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = uiState.title,
                fontSize = dimensions.loginTitleFontSize.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFa3b944),
                textAlign = TextAlign.Center
            )

            // Форма регистрации
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(dimensions.loginSpaceBetweenInputs)
            ) {
                // Уникальное имя
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = uiState.nameLabel,
                        fontSize = dimensions.loginLabelFontSize.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFa3b944),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    TextField(
                        value = uiState.nameUn,
                        onValueChange = { viewModel.onNameChange(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(dimensions.loginInputHeight),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF7FD4A8),
                            unfocusedContainerColor = Color(0xFF7FD4A8),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(dimensions.loginInputHeight / 2),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        singleLine = true
                    )
                }

                // Contraseña
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = uiState.passwordLabel,
                        fontSize = dimensions.loginLabelFontSize.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFa3b944),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    TextField(
                        value = uiState.password,
                        onValueChange = { viewModel.onPasswordChange(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(dimensions.loginInputHeight),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF7FD4A8),
                            unfocusedContainerColor = Color(0xFF7FD4A8),
                            focusedIndicatorColor = Color(0xFF000000),
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(dimensions.loginInputHeight / 2),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true
                    )
                }
            }

            // Кнопки Guardar
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(dimensions.loginSpaceBetweenButtons)
            ) {
                // GUARDAR
                Box(
                    modifier = Modifier
                        .width(dimensions.loginButtonWidth)
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
                        onClick = { viewModel.registerUser() },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(2.dp),
                        shape = RoundedCornerShape(dimensions.buttonCornerRadius),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF003D5B)
                        ),
                        contentPadding = PaddingValues(0.dp),
                        enabled = !uiState.isLoading  //блокировка при загрузке
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color(0xFFa3b944)
                            )
                        } else {
                            Text(
                                text = uiState.registerButton,
                                fontSize = dimensions.bottomButtonFontSize.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFa3b944)
                            )
                        }
                    }
                }

                // Показать ошибку
                uiState.errorMessage?.let { error ->
                    Text(
                        text = error,
                        color = Color(0xFFC42D2C),
                        fontSize = dimensions.loginLabelFontSize.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                }

                // Показать успех - ВАЖНО!
                uiState.successMessage?.let { success ->
                    Text(
                        text = success,
                        color = Color(0xFF48C553),
                        fontSize = dimensions.loginLabelFontSize.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                }

                // ENTRADA
                Box(
                    modifier = Modifier
                        .width(dimensions.loginButtonWidth)
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
                        onClick = onNavigateBack,
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
                            text = uiState.backButton,
                            fontSize = dimensions.bottomButtonFontSize.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFa3b944)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

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