package com.livlify.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Accent = Color(0xFF7C5CFF)
val Background = Color(0xFF0B0B10)
val SurfaceColor = Color(0xFF16161D)
val SurfaceHigh = Color(0xFF22222C)
val TextPrimary = Color(0xFFF4F4F8)
val TextSecondary = Color(0xFF9A9AAB)

private val LivlifyColors = darkColorScheme(
    primary = Accent,
    onPrimary = Color.White,
    background = Background,
    onBackground = TextPrimary,
    surface = SurfaceColor,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceHigh,
    onSurfaceVariant = TextSecondary
)

@Composable
fun LivlifyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LivlifyColors,
        content = content
    )
}
