package com.salehsafary.darkcalc.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkCalcColors = darkColorScheme(
    primary = Color(0xFF8FA8C7),
    secondary = Color(0xFF64748B),
    background = Color(0xFF08111F),
    surface = Color(0xFF0D1726),
    onPrimary = Color(0xFF08111F),
    onSecondary = Color.White,
    onBackground = Color(0xFFE5EAF0),
    onSurface = Color(0xFFE5EAF0)
)

@Composable
fun DarkCalcTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkCalcColors,
        content = content
    )
}