package com.solerunner26.lockscreenwidget.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightScheme = lightColorScheme(
    primary = Violet,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE4DCFB),
    onPrimaryContainer = VioletDark,
    secondary = Magenta,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF7D9EE),
    onSecondaryContainer = MagentaDark,
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = Color(0xFFE9E4F5),
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = Color(0xFFC9C2DE)
)

private val DarkScheme = darkColorScheme(
    primary = Color(0xFFC9B6F7),
    onPrimary = VioletDark,
    primaryContainer = VioletDark,
    onPrimaryContainer = Color(0xFFE4DCFB),
    secondary = Color(0xFFEFA9D6),
    onSecondary = MagentaDark,
    secondaryContainer = MagentaDark,
    onSecondaryContainer = Color(0xFFF7D9EE),
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = Color(0xFF2A2540),
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = Color(0xFF4A4363)
)

@Composable
fun QuickLockTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkScheme else LightScheme,
        content = content
    )
}
