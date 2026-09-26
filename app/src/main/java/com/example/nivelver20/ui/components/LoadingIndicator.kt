package com.example.nivelver20.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Единый индикатор загрузки: крутящийся спиннер + подпись «Cargando...»,
 * чтобы состояние загрузки читалось однозначно (а не как одинокая «точка»).
 */
@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier,
    text: String = "Cargando..."
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = Color(0xFFa3b944),
            strokeWidth = 5.dp,
            modifier = Modifier.size(56.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = text,
            color = Color(0xFFf2edd0),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
