package com.solerunner26.lockscreenwidget.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Two-tone violet → magenta palette (deliberately no white/blue theme).
val Violet = Color(0xFF6D4AC8)
val VioletDark = Color(0xFF4A3390)
val Magenta = Color(0xFFC94FA0)
val MagentaDark = Color(0xFF9C3A7C)

val LightBackground = Color(0xFFF4F1FA)
val LightSurface = Color(0xFFFFFFFF)
val LightOnSurface = Color(0xFF241E33)
val LightOnSurfaceVariant = Color(0xFF6B6580)

val DarkBackground = Color(0xFF14121C)
val DarkSurface = Color(0xFF1F1B2E)
val DarkOnSurface = Color(0xFFEAE6F7)
val DarkOnSurfaceVariant = Color(0xFFA79FC4)

val OkGreen = Color(0xFF34A853)
val WarnRed = Color(0xFFEA4335)

/** The signature two-color gradient used for the header and all primary buttons. */
val GradientBrush: Brush = Brush.linearGradient(listOf(Violet, Magenta))
