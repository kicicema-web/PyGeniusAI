package com.pygeniusai.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PyGeniusColors.Primary,
    onPrimary = Color.White,
    primaryContainer = PyGeniusColors.PrimaryDark,
    onPrimaryContainer = Color.White,
    secondary = PyGeniusColors.Secondary,
    onSecondary = Color.White,
    secondaryContainer = PyGeniusColors.SecondaryDark,
    onSecondaryContainer = Color.White,
    background = PyGeniusColors.Background,
    onBackground = PyGeniusColors.OnBackground,
    surface = PyGeniusColors.Surface,
    onSurface = PyGeniusColors.OnSurface,
    surfaceVariant = PyGeniusColors.SurfaceVariant,
    onSurfaceVariant = PyGeniusColors.OnSurfaceVariant,
    error = PyGeniusColors.Error,
    onError = Color.White,
    outline = PyGeniusColors.OnSurfaceMuted
)

private val LightColorScheme = lightColorScheme(
    primary = PyGeniusColors.PrimaryDark,
    onPrimary = Color.White,
    primaryContainer = PyGeniusColors.PrimaryLight,
    onPrimaryContainer = Color.Black,
    secondary = PyGeniusColors.SecondaryDark,
    onSecondary = Color.White,
    secondaryContainer = PyGeniusColors.Secondary,
    onSecondaryContainer = Color.Black,
    background = Color(0xFFF5F5F5),
    onBackground = Color(0xFF121212),
    surface = Color.White,
    onSurface = Color(0xFF121212),
    surfaceVariant = Color(0xFFE0E0E0),
    onSurfaceVariant = Color(0xFF616161),
    error = PyGeniusColors.Error,
    onError = Color.White,
    outline = Color(0xFF9E9E9E)
)

@Composable
fun PyGeniusTheme(
    darkTheme: Boolean = true, // Default to dark theme for coding
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Composition locals for custom theme values
val LocalPyGeniusColors = staticCompositionLocalOf { PyGeniusColors }
