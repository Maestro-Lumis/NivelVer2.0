package com.example.nivelver20.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp

/**
 * Текст, который сам подбирает размер под доступное место.
 *
 * Начинает с [maxFontSizeSp] и уменьшает кегль шагом [stepSp], пока текст не влезет
 * по ширине и высоте (с учётом переноса на [maxLines] строк), но не мельче [minFontSizeSp].
 * Так короткие слова остаются крупными, средние переносятся на 2 строки, а очень длинные
 * плавно уменьшаются — и ничто не вылезает за границы и не обрезается неожиданно.
 *
 * Размеры принимаются в sp как Float (в проекте кегли хранятся так, напр. vocabularioWordFontSize).
 */
@Composable
fun AutoResizeText(
    text: String,
    maxFontSizeSp: Float,
    minFontSizeSp: Float,
    color: Color,
    modifier: Modifier = Modifier,
    fontWeight: FontWeight = FontWeight.Bold,
    textAlign: TextAlign = TextAlign.Center,
    maxLines: Int = 2,
    stepSp: Float = 1f
) {
    // Состояние сбрасывается при смене текста или заданных границ (новая карточка/слово).
    var fontSize by remember(text, maxFontSizeSp, minFontSizeSp, maxLines) {
        mutableStateOf(maxFontSizeSp)
    }
    // Пока подбираем размер — не рисуем, чтобы не мигало промежуточными кеглями.
    var readyToDraw by remember(text, maxFontSizeSp, minFontSizeSp, maxLines) {
        mutableStateOf(false)
    }

    Text(
        text = text,
        modifier = modifier.drawWithContent { if (readyToDraw) drawContent() },
        color = color,
        fontSize = fontSize.sp,
        fontWeight = fontWeight,
        textAlign = textAlign,
        maxLines = maxLines,
        softWrap = true,
        overflow = TextOverflow.Clip,
        onTextLayout = { result ->
            val overflows = result.didOverflowWidth || result.didOverflowHeight
            if (overflows && fontSize > minFontSizeSp) {
                fontSize = (fontSize - stepSp).coerceAtLeast(minFontSizeSp)
            } else {
                readyToDraw = true
            }
        }
    )
}
