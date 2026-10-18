package com.daymark.app.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Indigo = Color(0xFF4F5BD5)

private val Light = lightColorScheme(
    primary = Indigo, onPrimary = Color.White,
    background = Color(0xFFF7F7F9), surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFEDEEF3),
)
private val Dark = darkColorScheme(
    primary = Color(0xFFA6AEFF), onPrimary = Color(0xFF14173F),
    background = Color(0xFF101114), surface = Color(0xFF1A1B20),
    surfaceVariant = Color(0xFF26272E),
)

/** Follows the system light/dark setting. A persisted user override (DataStore) is not built yet. */
@Composable
fun DaymarkTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (isSystemInDarkTheme()) Dark else Light, content = content)
}
