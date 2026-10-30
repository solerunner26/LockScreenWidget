package com.solerunner26.lockscreenwidget

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.solerunner26.lockscreenwidget.ui.SettingsScreen
import com.solerunner26.lockscreenwidget.ui.theme.QuickLockTheme

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
        var themeMode by remember { mutableStateOf(prefs.themeMode) }
        val darkTheme = when (themeMode) {
            Prefs.THEME_LIGHT -> false
            Prefs.THEME_DARK -> true
            else -> isSystemInDarkTheme()
        }
        QuickLockTheme(darkTheme = darkTheme) {
            SettingsScreen(
                themeMode = themeMode,
                onThemeModeChange = { mode ->
                    themeMode = mode
                    prefs.themeMode = mode
                }
            )
        }
    }
}
