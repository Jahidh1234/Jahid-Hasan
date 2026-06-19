package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    secondary = DarkSecondary,
    tertiary = DarkTertiary,
    background = DarkBackground,
    surface = DarkSurface,
    onBackground = DarkOnSurface,
    onSurface = DarkOnSurface,
    primaryContainer = DarkSurface,
    onPrimaryContainer = DarkOnSurface,
    surfaceVariant = DarkSurface.copy(alpha = 0.8f),
    onSurfaceVariant = DarkOnSurface.copy(alpha = 0.8f),
    outlineVariant = Color(0xFF1E3D59),
    errorContainer = HighPriorityBg,
    onErrorContainer = HighPriorityText
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    secondary = LightSecondary,
    tertiary = LightTertiary,
    background = LightBackground,
    surface = LightSurface,
    onBackground = LightOnSurface,
    onSurface = LightOnSurface,
    primaryContainer = LightSurface,
    onPrimaryContainer = LightOnSurface,
    surfaceVariant = EditorialLightGray,
    onSurfaceVariant = LightOnSurface.copy(alpha = 0.8f),
    outlineVariant = EditorialGray,
    errorContainer = HighPriorityBg,
    onErrorContainer = HighPriorityText
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep our beautiful brand colors consistent
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
