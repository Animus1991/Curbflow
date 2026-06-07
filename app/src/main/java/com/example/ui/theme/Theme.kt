package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    secondary = EmeraldLive,
    tertiary = NeonBlue,
    background = NavyDeep,
    surface = NavySurface,
    surfaceVariant = NavyElevated,
    onPrimary = NavyDeep,
    onSecondary = NavyDeep,
    onTertiary = ChalkWhite,
    onBackground = ChalkWhite,
    onSurface = ChalkWhite,
    onSurfaceVariant = SmokeGrey,
    outline = GlassBorder,
    error = CrimsonDanger
)

private val LightColorScheme = lightColorScheme(
    primary = NeonBlue,
    secondary = EmeraldLive,
    tertiary = NeonCyan,
    background = BackgroundLight,
    surface = SurfaceLight,
    surfaceVariant = Color(0xFFF1F5F9),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = TextPrimaryLight,
    onSurface = TextPrimaryLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = BorderLight,
    error = CrimsonDanger
)

interface ThemeManager {
    fun toggleTheme()
    val isDarkTheme: Boolean
}

val LocalThemeManager = staticCompositionLocalOf<ThemeManager> {
    error("No ThemeManager provided")
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // We enforce CDL Dark Mode for professional driving focus, 
    // but allow light mode if explicitly set.
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
