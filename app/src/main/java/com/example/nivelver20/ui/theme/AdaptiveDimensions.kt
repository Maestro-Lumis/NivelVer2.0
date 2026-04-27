package com.example.nivelver20.ui.theme

import android.annotation.SuppressLint
import android.app.Dialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.sqrt

// Data class для хранения адаптивных размеров
data class AdaptiveDimensions(
    val screenWidth: Dp,
    val screenHeight: Dp,
    val screenType: ScreenType,

    // Отступы
    val horizontalPadding: Dp,
    val verticalPadding: Dp,
    val spaceBetweenButtons: Dp,

    // Ошибки
   // val errorMessageView: Dp,

    // Размеры кнопок
    val buttonHeight: Dp,
    val buttonCornerRadius: Dp,

    // Размеры для буквы Ñ (фото)
    val letterNSize: Dp,
    val letterNTopPadding: Dp,
    val letterNBottomPadding: Dp,

    // Размеры для нижних кнопок
    val bottomButtonHeight: Dp,
    val bottomButtonWidth: Dp,
    val bottomButtonsTopPadding: Dp,

    // Текст
    val titleFontSize: Float,
    val buttonFontSize: Float,
    val bottomButtonFontSize: Float,

    val exitDialog: Float,

    // Для экрана Perfil
    val perfilTitleFontSize: Float,
    val perfilUsernameFontSize: Float,
    val perfilNivelFontSize: Float,
    val perfilButtonHeight: Dp,
    val perfilButtonFontSize: Float,
    val perfilSpacingBetweenButtons: Dp,
    val perfilTopPadding: Dp,
    val perfilBottomPadding: Dp,

    // Для экрана выбора уровня
    val nivelItemSpacing: Dp,
    val nivelImageSize: Dp,
    val nivelCircleWidth: Dp,
    val nivelCircleHeight: Dp,
    val nivelTitleFontSize: Float,
    val nivelSideTextFontSize: Float,
    // Для экрана авторизации (Login)
    val loginTitleFontSize: Float,
    val loginLabelFontSize: Float,
    val loginInputHeight: Dp,
    val loginButtonWidth: Dp,
    val loginButtonHeight: Dp,
    val loginSpaceBetweenInputs: Dp,
    val loginSpaceBetweenButtons: Dp,

    // Для экрана Vocabulario
    val vocabularioCardHeight: Dp,
    val vocabularioCardCornerRadius: Dp,
    val vocabularioTitleFontSize: Float,
    val vocabularioWordFontSize: Float,
    val vocabularioCounterFontSize: Float,
    val vocabularioCardSpacing: Dp,
    val vocabularioPadding: Dp,
    val vocabularioBlockWeight: Float,  // Вес блока карточек (испанских/русских)
    val vocabularioBlockSpacing: Dp,
    val vocabularioPadingH: Dp,

    // Для экрана Audio
    val audioVolumeUp: Dp,
    val lineHeightForAudAndLect: TextUnit,
    val answerItemMinHeight: Dp,
    val audioWordFontSize: Float,
    val audioQuestion: Float,

    // lectura
    val lecturaAnswerFontSize: Float,

    // Для экрана Grammar
    val grammarQuestionFontSize: Float,
    val grammarAnswerFontSize: Float,
    val grammarAnswerMinHeight: Dp,
    val grammarAnswerPadding: Dp,
    val grammarDragDropBoxHeight: Dp,
    val grammarWordButtonHeight: Dp,
    val grammarWordButtonFontSize: Float,
    val grammarSpacingBetweenSections: Dp,

    // Для Flujo Warning Screen
    val flujoWarningTitleFontSize: Float,
    val flujoWarningTextFontSize: Float,
    val flujoWarningIconFontSize: Float,

    )

enum class ScreenType {
    SMALL_PHONE,    // 3.5" - 4.5"
    MEDIUM_PHONE,   // 4.5" - 5.5"
    LARGE_PHONE,    // 5.5" - 6.5"
    XLARGE_PHONE,   // 6.5" - 6.9"
    SMALL_TABLET,   // 6.9" - 9"
    MEDIUM_TABLET,  // 9" - 11"
    LARGE_TABLET    // 11"+
}

// Composable функция для получения адаптивных размеров
@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun rememberAdaptiveDimensions(): AdaptiveDimensions {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    return remember(configuration.screenWidthDp, configuration.screenHeightDp) {
        val widthDp = configuration.screenWidthDp
        val heightDp = configuration.screenHeightDp
        val screenDiagonal = with(density) {
            sqrt(
                (widthDp * widthDp + heightDp * heightDp).toDouble()
            ).toFloat()
        }

        val screenType = when {
            screenDiagonal < 4.5f * 160 -> ScreenType.SMALL_PHONE
            screenDiagonal < 5.5f * 160 -> ScreenType.MEDIUM_PHONE
            screenDiagonal < 6.5f * 160 -> ScreenType.LARGE_PHONE
            screenDiagonal < 6.9f * 160 -> ScreenType.XLARGE_PHONE
            screenDiagonal < 9f * 160 -> ScreenType.SMALL_TABLET
            screenDiagonal < 11f * 160 -> ScreenType.MEDIUM_TABLET
            else -> ScreenType.LARGE_TABLET
        }

        when (screenType) {
            ScreenType.SMALL_PHONE -> AdaptiveDimensions(
                screenWidth = widthDp.dp,
                screenHeight = heightDp.dp,
                screenType = screenType,
                horizontalPadding = 20.dp,
                verticalPadding = 20.dp,
                spaceBetweenButtons = 20.dp,
                buttonHeight = 40.dp,
                buttonCornerRadius = 24.dp,
                letterNSize = 200.dp,
                letterNTopPadding = 20.dp,
                letterNBottomPadding = 20.dp,
                bottomButtonHeight = 40.dp,
                bottomButtonWidth = 40.dp,
                bottomButtonsTopPadding = 20.dp,
                titleFontSize = 22f,
                buttonFontSize = 18f,
                bottomButtonFontSize = 20f,
                nivelItemSpacing = 20.dp,
                nivelImageSize = 90.dp,
                nivelCircleWidth = 120.dp,
                nivelCircleHeight = 90.dp,
                nivelTitleFontSize = 28f,
                nivelSideTextFontSize = 28f,



                // Диалог
                exitDialog = 14f,

                // Для экрана авторизации
                loginTitleFontSize = 28f,
                loginLabelFontSize = 16f,
                loginInputHeight = 50.dp,
                loginButtonWidth = 250.dp,
                loginButtonHeight = 50.dp,
                loginSpaceBetweenInputs = 15.dp,
                loginSpaceBetweenButtons = 15.dp,

                // Для экрана Vocabulario
                vocabularioCardHeight = 60.dp,
                vocabularioCardCornerRadius = 12.dp,
                vocabularioTitleFontSize = 18f,
                vocabularioWordFontSize = 18f,
                vocabularioCounterFontSize = 18f,
                vocabularioCardSpacing = 10.dp,
                vocabularioPadding = 16.dp,
                vocabularioBlockWeight = 0.3f,
                vocabularioBlockSpacing = 10.dp,
                vocabularioPadingH = 12.dp,

                // Для экрана Audio
                audioVolumeUp = 50.dp,
                lineHeightForAudAndLect = 25.sp,
                answerItemMinHeight = 22.dp,
                audioWordFontSize = 7f,
                audioQuestion = 13f,

                // lectura
                lecturaAnswerFontSize = 10f,

                // Ошибки
                // Для экрана Grammar
                grammarQuestionFontSize = 13f,
                grammarAnswerFontSize = 11f,
                grammarAnswerMinHeight = 35.dp,
                grammarAnswerPadding = 6.dp,
                grammarDragDropBoxHeight = 35.dp,
                grammarWordButtonHeight = 26.dp,
                grammarWordButtonFontSize = 9f,
                grammarSpacingBetweenSections = 2.dp,

                // Для экрана Perfil
                perfilTitleFontSize = 22f,
                perfilUsernameFontSize = 14f,
                perfilNivelFontSize = 20f,
                perfilButtonHeight = 35.dp,
                perfilButtonFontSize = 14f,
                perfilSpacingBetweenButtons = 8.dp,
                perfilTopPadding = 10.dp,
                perfilBottomPadding = 10.dp,

                // Flujo Warning Screen
                flujoWarningTitleFontSize = 22f,
                flujoWarningTextFontSize = 18f,
                flujoWarningIconFontSize = 14f,
            )

            ScreenType.MEDIUM_PHONE -> AdaptiveDimensions(
                screenWidth = widthDp.dp,
                screenHeight = heightDp.dp,
                screenType = screenType,
                horizontalPadding = 20.dp,
                verticalPadding = 20.dp,
                spaceBetweenButtons = 20.dp,
                buttonHeight = 45.dp,
                buttonCornerRadius = 28.dp,
                letterNSize = 240.dp,
                letterNTopPadding = 20.dp,
                letterNBottomPadding = 20.dp,
                bottomButtonHeight = 45.dp,
                bottomButtonWidth = 160.dp,
                bottomButtonsTopPadding = 20.dp,
                titleFontSize = 26f,
                buttonFontSize = 20f,
                bottomButtonFontSize = 22f,
                nivelItemSpacing = 20.dp,
                nivelImageSize = 110.dp,
                nivelCircleWidth = 155.dp,
                nivelCircleHeight = 80.dp,
                nivelTitleFontSize = 32f,
                nivelSideTextFontSize = 32f,

                // Диалог
                exitDialog = 14f,

                // Для экрана авторизации
                loginTitleFontSize = 32f,
                loginLabelFontSize = 18f,
                loginInputHeight = 56.dp,
                loginButtonWidth = 280.dp,
                loginButtonHeight = 56.dp,
                loginSpaceBetweenInputs = 20.dp,
                loginSpaceBetweenButtons = 20.dp,

                // Для экрана Vocabulario
                vocabularioCardHeight = 70.dp,
                vocabularioCardCornerRadius = 14.dp,
                vocabularioTitleFontSize = 24f,
                vocabularioWordFontSize = 18f,
                vocabularioCounterFontSize = 24f,
                vocabularioCardSpacing = 12.dp,
                vocabularioPadding = 18.dp,
                vocabularioBlockWeight = 0.3f,
                vocabularioBlockSpacing = 10.dp,
                vocabularioPadingH = 12.dp,

                // Для экрана Audio
                audioVolumeUp = 66.dp,
                lineHeightForAudAndLect = 30.sp,
                answerItemMinHeight = 25.dp,
                audioWordFontSize = 16f,
                audioQuestion = 17f,

                // lectura
                lecturaAnswerFontSize = 11f,

                // Ошибки
                // Для экрана Grammar
                grammarQuestionFontSize = 16f,  // Было 18f
                grammarAnswerFontSize = 14f,    // Было 16f
                grammarAnswerMinHeight = 42.dp,  // Было 50.dp - УМЕНЬШИЛИ!
                grammarAnswerPadding = 10.dp,   // Было 12.dp
                grammarDragDropBoxHeight = 55.dp,  // Было 60.dp
                grammarWordButtonHeight = 38.dp,   // Было 40.dp
                grammarWordButtonFontSize = 14f,   // Было 15f
                grammarSpacingBetweenSections = 6.dp,  // Было 8.dp


                // Для экрана Perfil
                perfilTitleFontSize = 26f,
                perfilUsernameFontSize = 16f,
                perfilNivelFontSize = 24f,
                perfilButtonHeight = 40.dp,
                perfilButtonFontSize = 16f,
                perfilSpacingBetweenButtons = 10.dp,
                perfilTopPadding = 12.dp,
                perfilBottomPadding = 12.dp,

                // Flujo Warning Screen
                flujoWarningTitleFontSize = 26f,
                flujoWarningTextFontSize = 22f,
                flujoWarningIconFontSize = 16f,
            )

            ScreenType.LARGE_PHONE -> AdaptiveDimensions(
                screenWidth = widthDp.dp,
                screenHeight = heightDp.dp,
                screenType = screenType,
                horizontalPadding = 20.dp,
                verticalPadding = 20.dp,
                spaceBetweenButtons = 20.dp,
                buttonHeight = 60.dp,
                buttonCornerRadius = 30.dp,
                letterNSize = 260.dp,
                letterNTopPadding = 20.dp,
                letterNBottomPadding = 20.dp,
                bottomButtonHeight = 60.dp,
                bottomButtonWidth = 180.dp,
                bottomButtonsTopPadding = 20.dp,
                titleFontSize = 28f,
                buttonFontSize = 22f,
                bottomButtonFontSize = 24f,
                nivelItemSpacing = 20.dp,
                nivelImageSize = 150.dp,
                nivelCircleWidth = 200.dp,
                nivelCircleHeight = 150.dp,
                nivelTitleFontSize = 36f,
                nivelSideTextFontSize = 36f,

                // Диалог
                exitDialog = 14f,

                // Для экрана авторизации
                loginTitleFontSize = 36f,
                loginLabelFontSize = 20f,
                loginInputHeight = 60.dp,
                loginButtonWidth = 300.dp,
                loginButtonHeight = 60.dp,
                loginSpaceBetweenInputs = 20.dp,
                loginSpaceBetweenButtons = 20.dp,

                // Для экрана Vocabulario
                vocabularioCardHeight = 20.dp,
                vocabularioCardCornerRadius = 18.dp,
                vocabularioTitleFontSize = 28f,
                vocabularioWordFontSize = 20f,
                vocabularioCounterFontSize = 28f,
                vocabularioCardSpacing = 6.dp,
                vocabularioPadding = 16.dp,
                vocabularioBlockWeight = 0.3f,
                vocabularioBlockSpacing = 10.dp,
                vocabularioPadingH = 12.dp,

                // Для экрана Audio
                audioVolumeUp = 150.dp,
                lineHeightForAudAndLect = 35.sp,
                answerItemMinHeight = 37.dp,
                audioWordFontSize = 33f,
                audioQuestion = 20f,

                // lectura
                lecturaAnswerFontSize = 13f,

                // Ошибки

                grammarQuestionFontSize = 24f,
                grammarAnswerFontSize = 22f,
                grammarAnswerMinHeight = 65.dp,
                grammarAnswerPadding = 18.dp,
                grammarDragDropBoxHeight = 100.dp,
                grammarWordButtonHeight = 65.dp,
                grammarWordButtonFontSize = 21f,
                grammarSpacingBetweenSections = 16.dp,


                // Для экрана Perfil
                perfilTitleFontSize = 32f,
                perfilUsernameFontSize = 18f,
                perfilNivelFontSize = 28f,
                perfilButtonHeight = 50.dp,
                perfilButtonFontSize = 18f,
                perfilSpacingBetweenButtons = 12.dp,
                perfilTopPadding = 15.dp,
                perfilBottomPadding = 15.dp,

                // Flujo Warning Screen
                flujoWarningTitleFontSize = 32f,
                flujoWarningTextFontSize = 28f,
                flujoWarningIconFontSize = 22f,
            )

            ScreenType.XLARGE_PHONE -> AdaptiveDimensions(
                screenWidth = widthDp.dp,
                screenHeight = heightDp.dp,
                screenType = screenType,
                horizontalPadding = 20.dp,
                verticalPadding = 20.dp,
                spaceBetweenButtons = 20.dp,
                buttonHeight = 65.dp,
                buttonCornerRadius = 32.dp,
                letterNSize = 280.dp,
                letterNTopPadding = 20.dp,
                letterNBottomPadding = 20.dp,
                bottomButtonHeight = 65.dp,
                bottomButtonWidth = 190.dp,
                bottomButtonsTopPadding = 20.dp,
                titleFontSize = 30f,
                buttonFontSize = 24f,
                bottomButtonFontSize = 26f,
                nivelItemSpacing = 20.dp,
                nivelImageSize = 160.dp,
                nivelCircleWidth = 240.dp,
                nivelCircleHeight = 160.dp,
                nivelTitleFontSize = 38f,
                nivelSideTextFontSize = 38f,

                // Диалог
                exitDialog = 19f,

                // Для экрана авторизации
                loginTitleFontSize = 38f,
                loginLabelFontSize = 22f,
                loginInputHeight = 64.dp,
                loginButtonWidth = 320.dp,
                loginButtonHeight = 64.dp,
                loginSpaceBetweenInputs = 22.dp,
                loginSpaceBetweenButtons = 22.dp,

                // Для экрана Vocabulario
                vocabularioCardHeight = 25.dp,
                vocabularioCardCornerRadius = 19.dp,
                vocabularioTitleFontSize = 30f,
                vocabularioWordFontSize = 22f,
                vocabularioCounterFontSize = 30f,
                vocabularioCardSpacing = 8.dp,
                vocabularioPadding = 18.dp,
                vocabularioBlockWeight = 0.3f,
                vocabularioBlockSpacing = 10.dp,
                vocabularioPadingH = 12.dp,

                // Для экрана Audio
                audioVolumeUp = 200.dp,
                lineHeightForAudAndLect = 40.sp,
                answerItemMinHeight = 39.dp,
                audioWordFontSize = 20f,
                audioQuestion = 23f,

                // lectura
                lecturaAnswerFontSize = 15f,

                // Для экрана Grammar

                grammarQuestionFontSize = 26f,
                grammarAnswerFontSize = 24f,
                grammarAnswerMinHeight = 72.dp,
                grammarAnswerPadding = 20.dp,
                grammarDragDropBoxHeight = 110.dp,
                grammarWordButtonHeight = 72.dp,
                grammarWordButtonFontSize = 23f,
                grammarSpacingBetweenSections = 18.dp,

                // Для экрана Perfil
                perfilTitleFontSize = 36f,
                perfilUsernameFontSize = 20f,
                perfilNivelFontSize = 32f,
                perfilButtonHeight = 55.dp,
                perfilButtonFontSize = 20f,
                perfilSpacingBetweenButtons = 14.dp,
                perfilTopPadding = 16.dp,
                perfilBottomPadding = 16.dp,

                // Flujo Warning Screen
                flujoWarningTitleFontSize = 36f,
                flujoWarningTextFontSize = 32f,
                flujoWarningIconFontSize = 28f,
            )

            ScreenType.SMALL_TABLET -> AdaptiveDimensions(
                screenWidth = widthDp.dp,
                screenHeight = heightDp.dp,
                screenType = screenType,
                horizontalPadding = 20.dp,
                verticalPadding = 40.dp,
                spaceBetweenButtons = 20.dp,
                buttonHeight = 58.dp,
                buttonCornerRadius = 34.dp,
                letterNSize = 300.dp,
                letterNTopPadding = 20.dp,
                letterNBottomPadding = 20.dp,
                bottomButtonHeight = 58.dp,
                bottomButtonWidth = 200.dp,
                bottomButtonsTopPadding = 20.dp,
                titleFontSize = 32f,
                buttonFontSize = 24f,
                bottomButtonFontSize = 26f,
                nivelItemSpacing = 20.dp,
                nivelImageSize = 180.dp,
                nivelCircleWidth = 260.dp,
                nivelCircleHeight = 145.dp,
                nivelTitleFontSize = 42f,
                nivelSideTextFontSize = 42f,

                // Диалог
                exitDialog = 22f,

                // Для экрана авторизации
                loginTitleFontSize = 48f,
                loginLabelFontSize = 28f,
                loginInputHeight = 68.dp,
                loginButtonWidth = 250.dp,
                loginButtonHeight = 68.dp,
                loginSpaceBetweenInputs = 25.dp,
                loginSpaceBetweenButtons = 25.dp,

                // Для экрана Vocabulario
                vocabularioCardHeight = 40.dp,
                vocabularioCardCornerRadius = 18.dp,
                vocabularioTitleFontSize = 36f,
                vocabularioWordFontSize = 26f,
                vocabularioCounterFontSize = 36f,
                vocabularioCardSpacing = 6.dp,
                vocabularioPadding = 22.dp,
                vocabularioBlockWeight = 0.3f,
                vocabularioBlockSpacing = 10.dp,
                vocabularioPadingH = 12.dp,

                // Для экрана Audio
                audioVolumeUp = 200.dp,
                lineHeightForAudAndLect = 45.sp,
                answerItemMinHeight = 42.dp,
                audioWordFontSize = 38f,
                audioQuestion = 30f,

                // lectura
                lecturaAnswerFontSize = 17f,

                // Ошибки

                // Для экрана Grammar
                grammarQuestionFontSize = 28f,
                grammarAnswerFontSize = 26f,
                grammarAnswerMinHeight = 75.dp,
                grammarAnswerPadding = 20.dp,
                grammarDragDropBoxHeight = 110.dp,
                grammarWordButtonHeight = 70.dp,
                grammarWordButtonFontSize = 24f,
                grammarSpacingBetweenSections = 18.dp,

                // Для экрана Perfil
                perfilTitleFontSize = 42f,
                perfilUsernameFontSize = 24f,
                perfilNivelFontSize = 38f,
                perfilButtonHeight = 60.dp,
                perfilButtonFontSize = 22f,
                perfilSpacingBetweenButtons = 16.dp,
                perfilTopPadding = 18.dp,
                perfilBottomPadding = 18.dp,

                // Flujo Warning Screen
                flujoWarningTitleFontSize = 42f,
                flujoWarningTextFontSize = 38f,
                flujoWarningIconFontSize = 30f,

            )

            ScreenType.MEDIUM_TABLET -> AdaptiveDimensions(
                screenWidth = widthDp.dp,
                screenHeight = heightDp.dp,
                screenType = screenType,
                horizontalPadding = 20.dp,
                verticalPadding = 40.dp,
                spaceBetweenButtons = 20.dp,
                buttonHeight = 77.dp,
                buttonCornerRadius = 38.dp,
                letterNSize = 350.dp,
                letterNTopPadding = 20.dp,
                letterNBottomPadding = 20.dp,
                bottomButtonHeight = 77.dp,
                bottomButtonWidth = 220.dp,
                bottomButtonsTopPadding = 20.dp,
                titleFontSize = 44f,
                buttonFontSize = 36f,
                bottomButtonFontSize = 32f,
                nivelItemSpacing = 20.dp,
                nivelImageSize = 190.dp,
                nivelCircleWidth = 390.dp,
                nivelCircleHeight = 230.dp,
                nivelTitleFontSize = 66f,
                nivelSideTextFontSize = 66f,

                // Диалог
                exitDialog = 25f,

                // Для экрана авторизации
                loginTitleFontSize = 55f,
                loginLabelFontSize = 35f,
                loginInputHeight = 76.dp,
                loginButtonWidth = 400.dp,
                loginButtonHeight = 77.dp,
                loginSpaceBetweenInputs = 30.dp,
                loginSpaceBetweenButtons = 30.dp,

                // Для экрана Vocabulario
                vocabularioCardHeight = 90.dp,
                vocabularioCardCornerRadius = 20.dp,
                vocabularioTitleFontSize = 44f,
                vocabularioWordFontSize = 30f,
                vocabularioCounterFontSize = 44f,
                vocabularioCardSpacing = 18.dp,
                vocabularioPadding = 24.dp,
                vocabularioBlockWeight = 0.3f,
                vocabularioBlockSpacing = 10.dp,
                vocabularioPadingH = 12.dp,

                // Для экрана Audio
                audioVolumeUp = 250.dp,
                lineHeightForAudAndLect = 55.sp,
                answerItemMinHeight = 45.dp,
                audioWordFontSize = 42f,
                audioQuestion = 40f,

                // lectura
                lecturaAnswerFontSize = 22f,
                // Ошибки

                // Для экрана Grammar
                grammarQuestionFontSize = 32f,
                grammarAnswerFontSize = 28f,
                grammarAnswerMinHeight = 85.dp,
                grammarAnswerPadding = 22.dp,
                grammarDragDropBoxHeight = 120.dp,
                grammarWordButtonHeight = 75.dp,
                grammarWordButtonFontSize = 26f,
                grammarSpacingBetweenSections = 20.dp,

                // Для экрана Perfil
                perfilTitleFontSize = 50f,
                perfilUsernameFontSize = 30f,
                perfilNivelFontSize = 45f,
                perfilButtonHeight = 70.dp,
                perfilButtonFontSize = 28f,
                perfilSpacingBetweenButtons = 18.dp,
                perfilTopPadding = 20.dp,
                perfilBottomPadding = 20.dp,

                // Flujo Warning Screen
                flujoWarningTitleFontSize = 50f,
                flujoWarningTextFontSize = 46f,
                flujoWarningIconFontSize = 38f,

            )

            ScreenType.LARGE_TABLET -> AdaptiveDimensions(
                screenWidth = widthDp.dp,
                screenHeight = heightDp.dp,
                screenType = screenType,
                horizontalPadding = 20.dp,
                verticalPadding = 40.dp,
                spaceBetweenButtons = 20.dp,
                buttonHeight = 87.dp,
                buttonCornerRadius = 42.dp,
                letterNSize = 400.dp,
                letterNTopPadding = 20.dp,
                letterNBottomPadding = 20.dp,
                bottomButtonHeight = 87.dp,
                bottomButtonWidth = 240.dp,
                bottomButtonsTopPadding = 20.dp,
                titleFontSize = 40f,
                buttonFontSize = 28f,
                bottomButtonFontSize = 30f,
                nivelItemSpacing = 20.dp,
                nivelImageSize = 220.dp,
                nivelCircleWidth = 570.dp,
                nivelCircleHeight = 170.dp,
                nivelTitleFontSize = 54f,
                nivelSideTextFontSize = 54f,

                // Диалог
                exitDialog = 27f,

                // Для экрана авторизации
                loginTitleFontSize = 59f,
                loginLabelFontSize = 38f,
                loginInputHeight = 84.dp,
                loginButtonWidth = 480.dp,
                loginButtonHeight = 87.dp,
                loginSpaceBetweenInputs = 35.dp,
                loginSpaceBetweenButtons = 35.dp,

                // Для экрана Vocabulario
                vocabularioCardHeight = 140.dp,
                vocabularioCardCornerRadius = 22.dp,
                vocabularioTitleFontSize = 49f,
                vocabularioWordFontSize = 34f,
                vocabularioCounterFontSize = 49f,
                vocabularioCardSpacing = 20.dp,
                vocabularioPadding = 26.dp,
                vocabularioBlockWeight = 0.3f,
                vocabularioBlockSpacing = 10.dp,
                vocabularioPadingH = 12.dp,

                // Для экрана Audio
                audioVolumeUp = 300.dp,
                lineHeightForAudAndLect = 60.sp,
                answerItemMinHeight = 50.dp,
                audioWordFontSize = 46f,
                audioQuestion = 45f,

                // lectura
                lecturaAnswerFontSize = 25f,

                // Ошибки

                // Для экрана Grammar
                grammarQuestionFontSize = 36f,
                grammarAnswerFontSize = 32f,
                grammarAnswerMinHeight = 100.dp,
                grammarAnswerPadding = 26.dp,
                grammarDragDropBoxHeight = 140.dp,
                grammarWordButtonHeight = 85.dp,
                grammarWordButtonFontSize = 30f,
                grammarSpacingBetweenSections = 24.dp,

                // Для экрана Perfil

                perfilTitleFontSize = 56f,
                perfilUsernameFontSize = 34f,
                perfilNivelFontSize = 50f,
                perfilButtonHeight = 80.dp,
                perfilButtonFontSize = 32f,
                perfilSpacingBetweenButtons = 20.dp,
                perfilTopPadding = 24.dp,
                perfilBottomPadding = 24.dp,

                // Flujo Warning Screen
                flujoWarningTitleFontSize = 56f,
                flujoWarningTextFontSize = 48f,
                flujoWarningIconFontSize = 40f,
            )
        }
    }
}