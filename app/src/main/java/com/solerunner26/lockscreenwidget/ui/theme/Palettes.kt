package com.solerunner26.lockscreenwidget.ui.theme

import androidx.annotation.StringRes
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.solerunner26.lockscreenwidget.Prefs
import com.solerunner26.lockscreenwidget.R

/**
 * One selectable look for the app. Every palette ships a hand-tuned light AND dark
 * Material 3 scheme (all surface-container roles filled in, so no baseline purple
 * leaks into cards, sheets or switches) plus the two-tone gradient used by the hero
 * card and primary buttons.
 */
data class AppPalette(
    val id: Int,
    @StringRes val nameRes: Int,
    val gradientStart: Color,
    val gradientEnd: Color,
    val light: ColorScheme,
    val dark: ColorScheme,
    /** Night-only palettes ignore the System/Light/Dark setting. */
    val alwaysDark: Boolean = false
)

private data class Tones(
    val primary: Color, val onPrimary: Color, val primaryContainer: Color, val onPrimaryContainer: Color,
    val secondary: Color, val onSecondary: Color, val secondaryContainer: Color, val onSecondaryContainer: Color,
    val tertiary: Color, val onTertiary: Color, val tertiaryContainer: Color, val onTertiaryContainer: Color,
    val background: Color, val lowest: Color, val low: Color, val mid: Color, val high: Color, val highest: Color,
    val onSurface: Color, val onSurfaceVariant: Color, val outline: Color, val outlineVariant: Color
)

private fun Tones.toLight() = lightColorScheme(
    primary = primary, onPrimary = onPrimary,
    primaryContainer = primaryContainer, onPrimaryContainer = onPrimaryContainer,
    secondary = secondary, onSecondary = onSecondary,
    secondaryContainer = secondaryContainer, onSecondaryContainer = onSecondaryContainer,
    tertiary = tertiary, onTertiary = onTertiary,
    tertiaryContainer = tertiaryContainer, onTertiaryContainer = onTertiaryContainer,
    background = background, onBackground = onSurface,
    surface = background, onSurface = onSurface,
    surfaceVariant = highest, onSurfaceVariant = onSurfaceVariant,
    surfaceBright = lowest, surfaceDim = highest,
    surfaceContainerLowest = lowest, surfaceContainerLow = low, surfaceContainer = mid,
    surfaceContainerHigh = high, surfaceContainerHighest = highest,
    outline = outline, outlineVariant = outlineVariant
)

private fun Tones.toDark() = darkColorScheme(
    primary = primary, onPrimary = onPrimary,
    primaryContainer = primaryContainer, onPrimaryContainer = onPrimaryContainer,
    secondary = secondary, onSecondary = onSecondary,
    secondaryContainer = secondaryContainer, onSecondaryContainer = onSecondaryContainer,
    tertiary = tertiary, onTertiary = onTertiary,
    tertiaryContainer = tertiaryContainer, onTertiaryContainer = onTertiaryContainer,
    background = background, onBackground = onSurface,
    surface = background, onSurface = onSurface,
    surfaceVariant = highest, onSurfaceVariant = onSurfaceVariant,
    surfaceBright = highest, surfaceDim = background,
    surfaceContainerLowest = lowest, surfaceContainerLow = low, surfaceContainer = mid,
    surfaceContainerHigh = high, surfaceContainerHighest = highest,
    outline = outline, outlineVariant = outlineVariant
)

private fun c(hex: Long) = Color(hex)

val AuroraPalette = AppPalette(
    id = Prefs.PALETTE_AURORA,
    nameRes = R.string.palette_aurora,
    gradientStart = Violet,
    gradientEnd = Magenta,
    light = Tones(
        c(0xFF6D4AC8), c(0xFFFFFFFF), c(0xFFE8DEFF), c(0xFF23005C),
        c(0xFF9C3A7C), c(0xFFFFFFFF), c(0xFFFFD8EE), c(0xFF3B0030),
        c(0xFFC94FA0), c(0xFFFFFFFF), c(0xFFFFD8EE), c(0xFF3B0030),
        c(0xFFFBF8FF), c(0xFFFFFFFF), c(0xFFF6F1FD), c(0xFFF0EAF8), c(0xFFEAE4F3), c(0xFFE4DEED),
        c(0xFF1D1A24), c(0xFF4A4458), c(0xFF7B7489), c(0xFFCBC3D9)
    ).toLight(),
    dark = Tones(
        c(0xFFCFBDFF), c(0xFF3A1E8F), c(0xFF5233AE), c(0xFFE8DEFF),
        c(0xFFFFAEDD), c(0xFF5C1149), c(0xFF7F2A66), c(0xFFFFD8EE),
        c(0xFFFFAEDD), c(0xFF5C1149), c(0xFF7F2A66), c(0xFFFFD8EE),
        c(0xFF141218), c(0xFF0F0D13), c(0xFF1D1A22), c(0xFF211E27), c(0xFF2B2832), c(0xFF36333D),
        c(0xFFE7E0EE), c(0xFFCBC3D9), c(0xFF958EA3), c(0xFF4A4458)
    ).toDark()
)

val OceanPalette = AppPalette(
    id = Prefs.PALETTE_OCEAN,
    nameRes = R.string.palette_ocean,
    gradientStart = c(0xFF00838F),
    gradientEnd = c(0xFF3F51B5),
    light = Tones(
        c(0xFF00677F), c(0xFFFFFFFF), c(0xFFB8EAFF), c(0xFF001F28),
        c(0xFF2E5EAA), c(0xFFFFFFFF), c(0xFFD8E2FF), c(0xFF001A43),
        c(0xFF3F51B5), c(0xFFFFFFFF), c(0xFFDEE0FF), c(0xFF00105C),
        c(0xFFF5FAFD), c(0xFFFFFFFF), c(0xFFEFF4F8), c(0xFFE9EEF2), c(0xFFE3E9EC), c(0xFFDDE3E7),
        c(0xFF171C1F), c(0xFF40484C), c(0xFF70787D), c(0xFFBFC8CC)
    ).toLight(),
    dark = Tones(
        c(0xFF5DD5FC), c(0xFF003544), c(0xFF004D61), c(0xFFB8EAFF),
        c(0xFFADC6FF), c(0xFF002E6A), c(0xFF1F4690), c(0xFFD8E2FF),
        c(0xFFBAC3FF), c(0xFF08218A), c(0xFF2A3D9F), c(0xFFDEE0FF),
        c(0xFF0F1417), c(0xFF0A0F12), c(0xFF171C1F), c(0xFF1B2023), c(0xFF252B2E), c(0xFF303538),
        c(0xFFDEE3E6), c(0xFFBFC8CC), c(0xFF8A9296), c(0xFF40484C)
    ).toDark()
)

val SunsetPalette = AppPalette(
    id = Prefs.PALETTE_SUNSET,
    nameRes = R.string.palette_sunset,
    gradientStart = c(0xFFD84315),
    gradientEnd = c(0xFFE8710A),
    light = Tones(
        c(0xFFA8431F), c(0xFFFFFFFF), c(0xFFFFDBCF), c(0xFF3A0B00),
        c(0xFF8B5000), c(0xFFFFFFFF), c(0xFFFFDCBE), c(0xFF2C1600),
        c(0xFFB3261E), c(0xFFFFFFFF), c(0xFFFFDAD5), c(0xFF410001),
        c(0xFFFFF8F5), c(0xFFFFFFFF), c(0xFFFFF1EC), c(0xFFFCEAE4), c(0xFFF6E4DE), c(0xFFF1DFD9),
        c(0xFF231917), c(0xFF53433F), c(0xFF85736E), c(0xFFD8C2BC)
    ).toLight(),
    dark = Tones(
        c(0xFFFFB59C), c(0xFF5C1900), c(0xFF842D0A), c(0xFFFFDBCF),
        c(0xFFFFB870), c(0xFF4A2800), c(0xFF693C00), c(0xFFFFDCBE),
        c(0xFFFFB4A9), c(0xFF690003), c(0xFF930009), c(0xFFFFDAD5),
        c(0xFF1A110F), c(0xFF140C0A), c(0xFF231917), c(0xFF271D1B), c(0xFF322825), c(0xFF3D322F),
        c(0xFFF1DFD9), c(0xFFD8C2BC), c(0xFFA08C87), c(0xFF53433F)
    ).toDark()
)

val ForestPalette = AppPalette(
    id = Prefs.PALETTE_FOREST,
    nameRes = R.string.palette_forest,
    gradientStart = c(0xFF137A43),
    gradientEnd = c(0xFF4E8A2B),
    light = Tones(
        c(0xFF1B6D3A), c(0xFFFFFFFF), c(0xFFA4F4B4), c(0xFF00210C),
        c(0xFF3B6939), c(0xFFFFFFFF), c(0xFFBCF0B4), c(0xFF002204),
        c(0xFF39656B), c(0xFFFFFFFF), c(0xFFBCEBF2), c(0xFF001F23),
        c(0xFFF6FBF3), c(0xFFFFFFFF), c(0xFFF0F5ED), c(0xFFEAEFE7), c(0xFFE4EAE2), c(0xFFDFE4DC),
        c(0xFF181D18), c(0xFF414941), c(0xFF717970), c(0xFFC1C9BF)
    ).toLight(),
    dark = Tones(
        c(0xFF89D89A), c(0xFF00391A), c(0xFF005228), c(0xFFA4F4B4),
        c(0xFFA1D39A), c(0xFF0A390F), c(0xFF235024), c(0xFFBCF0B4),
        c(0xFFA1CED5), c(0xFF00363B), c(0xFF1F4D53), c(0xFFBCEBF2),
        c(0xFF101510), c(0xFF0B0F0B), c(0xFF181D18), c(0xFF1C211C), c(0xFF262B26), c(0xFF313631),
        c(0xFFDFE4DC), c(0xFFC1C9BF), c(0xFF8B9389), c(0xFF414941)
    ).toDark()
)

/** True-black night theme: OLED friendly, dimmed accents, easy on the eyes in bed. */
private val MidnightScheme = Tones(
    c(0xFFA8B4FF), c(0xFF0F1F6B), c(0xFF2A3A8C), c(0xFFDDE1FF),
    c(0xFFC3B5FF), c(0xFF2B1A66), c(0xFF3A2E73), c(0xFFE5DEFF),
    c(0xFF9ECFE8), c(0xFF003548), c(0xFF1C4B60), c(0xFFC4E7FF),
    c(0xFF000000), c(0xFF000000), c(0xFF0B0B10), c(0xFF101016), c(0xFF17171F), c(0xFF1F1F29),
    c(0xFFE3E2EC), c(0xFFA9A8B8), c(0xFF6F6E80), c(0xFF2E2E3A)
).toDark()

val MidnightPalette = AppPalette(
    id = Prefs.PALETTE_MIDNIGHT,
    nameRes = R.string.palette_midnight,
    gradientStart = c(0xFF303F9F),
    gradientEnd = c(0xFF6A4BB0),
    light = MidnightScheme,
    dark = MidnightScheme,
    alwaysDark = true
)

/** Static palettes in picker order. "Material You" (wallpaper colors) is added on Android 12+. */
val StaticPalettes = listOf(AuroraPalette, OceanPalette, SunsetPalette, ForestPalette, MidnightPalette)
