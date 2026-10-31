package com.solerunner26.lockscreenwidget

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import com.solerunner26.lockscreenwidget.ui.SettingsScreen
import com.solerunner26.lockscreenwidget.ui.theme.QuickLockTheme
import com.solerunner26.lockscreenwidget.ui.theme.paletteById

/**
 * Launcher entry point: the Compose Material 3 settings screen.
 *
 * Security: the window is FLAG_SECURE, so this UI can never be captured by a
 * screenshot or a screen recorder — an eavesdropper recording the phone screen gets
 * nothing from this app. The activity holds no sensitive data anyway.
 */
class SettingsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Block screenshots and screen recording of this app's UI.
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            QuickLockAppTheme()
        }
    }

    @Composable
    private fun QuickLockAppTheme() {
        val context = LocalContext.current
        val prefs = remember { Prefs.get(context) }
        var themeMode by remember { mutableIntStateOf(prefs.themeMode) }
        var palette by remember { mutableIntStateOf(prefs.palette) }
        val darkSystem = isSystemInDarkTheme()

        // Keep status/navigation bar icons readable for the chosen look.
        val dark = paletteById(context, palette).alwaysDark || when (themeMode) {
            Prefs.THEME_LIGHT -> false
            Prefs.THEME_DARK -> true
            else -> darkSystem
        }
        LaunchedEffect(dark) {
            val transparent = android.graphics.Color.TRANSPARENT
            val style = if (dark) SystemBarStyle.dark(transparent)
            else SystemBarStyle.light(transparent, androidx.compose.ui.graphics.Color.Black.toArgb())
            enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
        }

        QuickLockTheme(paletteId = palette, themeMode = themeMode, darkSystem = darkSystem) {
            SettingsScreen(
                themeMode = themeMode,
                paletteId = palette,
                onThemeModeChange = { mode ->
                    themeMode = mode
                    prefs.themeMode = mode
                },
                onPaletteChange = { id ->
                    palette = id
                    prefs.palette = id
                }
            )
        }
    }
}
