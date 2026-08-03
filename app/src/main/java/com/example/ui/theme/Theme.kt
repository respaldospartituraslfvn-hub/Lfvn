package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = CyanAccent,
    onPrimary = StudioBackground,
    primaryContainer = StudioSurfaceVariant,
    onPrimaryContainer = CyanAccent,
    secondary = AmberAccent,
    onSecondary = StudioBackground,
    secondaryContainer = StudioSurfaceVariant,
    onSecondaryContainer = AmberAccent,
    tertiary = PurpleAccent,
    background = StudioBackground,
    onBackground = TextPrimary,
    surface = StudioCard,
    onSurface = TextPrimary,
    surfaceVariant = StudioSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = StudioCardBorder
)

@Composable
fun MetroPulseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
