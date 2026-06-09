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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
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

// --- Colorblind-safe palette (Deuteranopia/Protanopia) ---
// Replaces red/green signaling with orange/blue, safe for ~8% of male population
private val ColorblindDanger = Color(0xFFFF6D00)   // Orange replaces red
private val ColorblindSuccess = Color(0xFF2979FF)  // Blue replaces green
private val ColorblindWarning = Color(0xFFFFD600)  // Yellow (unchanged, universally visible)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    accessibilityPrefs: AccessibilityPrefs = LocalAccessibilityPrefs.current,
    content: @Composable () -> Unit
) {
    val baseScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    // Apply colorblind-safe palette overrides
    val colorScheme = if (accessibilityPrefs.colorblindMode) {
        baseScheme.copy(
            error = ColorblindDanger,
            secondary = ColorblindSuccess,
            tertiary = ColorblindWarning
        )
    } else {
        baseScheme
    }.let { scheme ->
        // High contrast: boost surface differentiation
        if (accessibilityPrefs.highContrast && darkTheme) {
            scheme.copy(
                background = Color(0xFF000000),
                surface = Color(0xFF0D0D0D),
                surfaceVariant = Color(0xFF1A1A1A),
                outline = Color(0xFF4FC3F7),
                onBackground = Color(0xFFFFFFFF),
                onSurface = Color(0xFFFFFFFF)
            )
        } else if (accessibilityPrefs.highContrast && !darkTheme) {
            scheme.copy(
                background = Color(0xFFFFFFFF),
                surface = Color(0xFFF5F5F5),
                surfaceVariant = Color(0xFFE0E0E0),
                outline = Color(0xFF212121),
                onBackground = Color(0xFF000000),
                onSurface = Color(0xFF000000)
            )
        } else {
            scheme
        }
    }

    // Large text: scale all typography by 1.25×
    val typography = if (accessibilityPrefs.largeText) {
        fun TextStyle.scaled(): TextStyle = copy(
            fontSize = fontSize * 1.25f,
            lineHeight = lineHeight * 1.25f
        )
        Typography.copy(
            displayLarge = Typography.displayLarge.scaled(),
            displayMedium = Typography.displayMedium.scaled(),
            headlineSmall = Typography.headlineSmall.scaled(),
            titleLarge = Typography.titleLarge.scaled(),
            titleMedium = Typography.titleMedium.scaled(),
            bodyLarge = Typography.bodyLarge.scaled(),
            labelLarge = Typography.labelLarge.scaled(),
            labelSmall = Typography.labelSmall.scaled()
        )
    } else {
        Typography
    }
    
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
        typography = typography,
        content = content
    )
}
