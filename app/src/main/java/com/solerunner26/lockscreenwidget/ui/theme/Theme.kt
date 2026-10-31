package com.solerunner26.lockscreenwidget.ui.theme

import android.content.Context
import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.solerunner26.lockscreenwidget.Prefs
import com.solerunner26.lockscreenwidget.R

/** Extra theme values Material 3 has no slot for. */
@Immutable
data class QuickLockExtras(
    val gradient: Brush,
    val gradientStart: Color,
    val gradientEnd: Color,
    val success: Color,
    val isDark: Boolean
)

val LocalQuickLockExtras = staticCompositionLocalOf {
    QuickLockExtras(
        gradient = Brush.linearGradient(listOf(Violet, Magenta)),
        gradientStart = Violet,
        gradientEnd = Magenta,
        success = OkGreenLight,
        isDark = false
    )
}

/** Shortcut: `QuickLockTheme.extras.gradient` etc. */
object QuickLockTheme {
    val extras: QuickLockExtras
        @Composable get() = LocalQuickLockExtras.current
}

/** Wallpaper-based "Material You" palette, available on Android 12+. */
fun dynamicPalette(context: Context): AppPalette? {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return null
    val light = dynamicLightColorScheme(context)
    val dark = dynamicDarkColorScheme(context)
    return AppPalette(
        id = Prefs.PALETTE_DYNAMIC,
        nameRes = R.string.palette_dynamic,
        gradientStart = light.primary,
        gradientEnd = light.tertiary,
        light = light,
        dark = dark
    )
}

/** All palettes available on this device, in picker order. */
fun availablePalettes(context: Context): List<AppPalette> =
    listOfNotNull(dynamicPalette(context)) + StaticPalettes

fun paletteById(context: Context, id: Int): AppPalette =
    availablePalettes(context).firstOrNull { it.id == id } ?: AuroraPalette

private val AppTypography = Typography().run {
    copy(
        displaySmall = displaySmall.copy(fontWeight = FontWeight.SemiBold, letterSpacing = (-0.5).sp),
        headlineMedium = headlineMedium.copy(fontWeight = FontWeight.SemiBold, letterSpacing = (-0.25).sp),
        headlineSmall = headlineSmall.copy(fontWeight = FontWeight.SemiBold),
        titleLarge = titleLarge.copy(fontWeight = FontWeight.SemiBold),
        titleMedium = titleMedium.copy(fontWeight = FontWeight.SemiBold),
        labelLarge = labelLarge.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.2.sp)
    )
}

/**
 * Root theme. [themeMode] is one of Prefs.THEME_*; [darkSystem] is the current
 * system night-mode flag. Colors cross-fade when the user switches palettes.
 */
@Composable
fun QuickLockTheme(
    paletteId: Int,
    themeMode: Int,
    darkSystem: Boolean,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val palette = paletteById(context, paletteId)
    val dark = palette.alwaysDark || when (themeMode) {
        Prefs.THEME_LIGHT -> false
        Prefs.THEME_DARK -> true
        else -> darkSystem
    }
    val target = if (dark) palette.dark else palette.light
    val scheme = target.animated()

    val gradStart by animateColorAsState(palette.gradientStart, tween(ANIM_MS), label = "gradStart")
    val gradEnd by animateColorAsState(palette.gradientEnd, tween(ANIM_MS), label = "gradEnd")
    val extras = QuickLockExtras(
        gradient = Brush.linearGradient(listOf(gradStart, gradEnd)),
        gradientStart = gradStart,
        gradientEnd = gradEnd,
        success = if (dark) OkGreenDark else OkGreenLight,
        isDark = dark
    )

    CompositionLocalProvider(LocalQuickLockExtras provides extras) {
        MaterialTheme(colorScheme = scheme, typography = AppTypography, content = content)
    }
}

private const val ANIM_MS = 450

/** Smoothly animates the roles the UI actually uses. */
@Composable
private fun ColorScheme.animated(): ColorScheme {
    @Composable
    fun a(c: Color, name: String): Color = animateColorAsState(c, tween(ANIM_MS), label = name).value
    return copy(
        primary = a(primary, "primary"),
        onPrimary = a(onPrimary, "onPrimary"),
        primaryContainer = a(primaryContainer, "primaryContainer"),
        onPrimaryContainer = a(onPrimaryContainer, "onPrimaryContainer"),
        secondaryContainer = a(secondaryContainer, "secondaryContainer"),
        onSecondaryContainer = a(onSecondaryContainer, "onSecondaryContainer"),
        tertiaryContainer = a(tertiaryContainer, "tertiaryContainer"),
        onTertiaryContainer = a(onTertiaryContainer, "onTertiaryContainer"),
        background = a(background, "background"),
        onBackground = a(onBackground, "onBackground"),
        surface = a(surface, "surface"),
        onSurface = a(onSurface, "onSurface"),
        onSurfaceVariant = a(onSurfaceVariant, "onSurfaceVariant"),
        surfaceContainerLow = a(surfaceContainerLow, "surfaceContainerLow"),
        surfaceContainer = a(surfaceContainer, "surfaceContainer"),
        surfaceContainerHigh = a(surfaceContainerHigh, "surfaceContainerHigh"),
        surfaceContainerHighest = a(surfaceContainerHighest, "surfaceContainerHighest"),
        outline = a(outline, "outline"),
        outlineVariant = a(outlineVariant, "outlineVariant")
    )
}
