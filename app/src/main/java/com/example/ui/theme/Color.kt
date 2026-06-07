package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// --- Curbflow Design Language (CDL) v2.0 - High Contrast Palette ---

// 1. Primary Tones (Deep Navigation Aesthetic)
val NavyDeep = Color(0xFF0A0E14)      // Primary Background (Absorbs glare)
val NavySurface = Color(0xFF141A23)   // Surface for cards and layers
val NavyElevated = Color(0xFF1E2631)  // Hover/Active states

// 2. High-Affordance Accents (Neon/Vivid for outdoor visibility)
val NeonCyan = Color(0xFF00E5FF)      // Primary Action (Command Color)
val NeonBlue = Color(0xFF2979FF)      // Secondary/Navigation Info
val EmeraldLive = Color(0xFF00E676)   // Success/Availability
val AmberWarning = Color(0xFFFFC400)  // Caution/Moderate Demand
val CrimsonDanger = Color(0xFFFF1744) // Full/Restricted

// 3. Typographic Hierarchy
val ChalkWhite = Color(0xFFF5F7FA)    // Primary Text (Ultra high contrast)
val SmokeGrey = Color(0xFF94A3B8)     // Secondary Text
val SlateGrey = Color(0xFF475569)     // Muted/Decorative

// 4. Border & Divider (Structural Affinity)
val GlassBorder = Color(0xFF334155).copy(alpha = 0.5f)
val CyberLine = Color(0xFF00E5FF).copy(alpha = 0.2f)

// Mapping Legacy Aliases for backward compatibility during refactor
val BrandCyan = NeonCyan
val BrandGreen = EmeraldLive
val BackgroundDark = NavyDeep
val SurfaceDark = NavySurface
val StrongText = ChalkWhite
val MutedText = SmokeGrey
val BorderSubtle = GlassBorder

val ProbabilityHigh = EmeraldLive
val ProbabilityMedium = AmberWarning
val ProbabilityLow = CrimsonDanger
val ProbabilityUnknown = SlateGrey

// Light Mode (Optimized for readability if switched)
val BackgroundLight = Color(0xFFF8FAFC)
val SurfaceLight = Color(0xFFFFFFFF)
val TextPrimaryLight = Color(0xFF0F172A)
val TextSecondaryLight = Color(0xFF64748B)
val BorderLight = Color(0xFFE2E8F0)
