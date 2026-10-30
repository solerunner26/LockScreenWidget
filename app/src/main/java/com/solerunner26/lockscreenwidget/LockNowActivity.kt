package com.solerunner26.lockscreenwidget

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.WindowManager

/**
 * Invisible activity behind the static "Lock screen" app shortcut
 * (long-press the app icon on the home screen).
 *
 * Hardening:
 *  - FLAG_SECURE window: it can never appear in screenshots / screen recordings.
 *  - noHistory + excludeFromRecents: it leaves no trace in recents.
 *  - Before locking it verifies that the caller is this app itself or a real home
 *    launcher (shortcut taps are delivered via the launcher). Any other app trying
 *    to fire this intent is ignored.
 */
class LockNowActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Must be set before the window is shown to be fully effective.
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        super.onCreate(savedInstanceState)
        if (isCallerAllowed()) {
            LockController.lock(this)
        }
        finish()
    }

    private fun isCallerAllowed(): Boolean {
        val caller = callingPackage ?: return true // started by ourselves or the system
        if (caller == packageName) return true
        // Shortcut taps arrive through the home launcher — allow any installed HOME app.
        val homeIntent = Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_HOME)
            .setPackage(caller)
        return packageManager.resolveActivity(homeIntent, PackageManager.MATCH_DEFAULT_ONLY) != null
    }
}
