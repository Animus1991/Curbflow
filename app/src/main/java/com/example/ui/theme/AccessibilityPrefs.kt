package com.example.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Production accessibility preferences that actually re-theme the app.
 *
 * - **colorblindMode**: replaces red/green with orange/blue (Deuteranopia-safe)
 * - **highContrast**: raises surface/background contrast and outlines
 * - **largeText**: scales all typography by 1.25×
 *
 * Persisted via SharedPreferences in SettingsScreen; read in MainActivity
 * and provided via CompositionLocal to the entire tree.
 */
data class AccessibilityPrefs(
    val colorblindMode: Boolean = false,
    val highContrast: Boolean = false,
    val largeText: Boolean = false
)

val LocalAccessibilityPrefs = staticCompositionLocalOf { AccessibilityPrefs() }
