package com.solerunner26.lockscreenwidget

import android.content.Context
import android.content.SharedPreferences
import java.security.SecureRandom

/**
 * App settings. Stored in a MODE_PRIVATE SharedPreferences file (never exported),
 * and the whole file is excluded from backup / cloud backup / device transfer
 * (see res/xml/backup_rules.xml and res/xml/data_extraction_rules.xml).
 *
 * Contains only UI preferences and two timestamps — no personal data whatsoever.
 */
class Prefs private constructor(private val sp: SharedPreferences) {

    companion object {
        private const val FILE = "quicklock_prefs" // MODE_PRIVATE by definition of the default

        const val MODE_SINGLE_TAP = 0
        const val MODE_DOUBLE_TAP = 1

        const val BG_TRANSLUCENT = 0
        const val BG_TRANSPARENT = 1
        const val BG_SOLID = 2

        const val THEME_SYSTEM = 0
        const val THEME_LIGHT = 1
        const val THEME_DARK = 2

        // Color palettes (see ui/theme/Palettes.kt). Stored by id, never by index
        // into a list, so reordering the picker never changes a user's choice.
        const val PALETTE_AURORA = 0
        const val PALETTE_OCEAN = 1
        const val PALETTE_SUNSET = 2
        const val PALETTE_FOREST = 3
        const val PALETTE_MIDNIGHT = 4
        const val PALETTE_DYNAMIC = 5

        @JvmStatic
        fun get(context: Context): Prefs =
            Prefs(context.getSharedPreferences(FILE, Context.MODE_PRIVATE))
    }

    var gestureMode: Int
        get() = sp.getInt("gesture_mode", MODE_DOUBLE_TAP)
        set(value) = sp.edit().putInt("gesture_mode", value).apply()

    var tapWindowMs: Long
        get() = sp.getLong("tap_window_ms", 450L)
        set(value) = sp.edit().putLong("tap_window_ms", value).apply()

    var iconColor: Int
        get() = sp.getInt("icon_color", 0xFFFFFFFF.toInt())
        set(value) = sp.edit().putInt("icon_color", value).apply()

    var backgroundStyle: Int
        get() = sp.getInt("background_style", BG_TRANSPARENT)
        set(value) = sp.edit().putInt("background_style", value).apply()

    /** When false (default) the widget is fully invisible: no icon, no background. */
    var showIcon: Boolean
        get() = sp.getBoolean("show_icon", false)
        set(value) = sp.edit().putBoolean("show_icon", value).apply()

    var showLabel: Boolean
        get() = sp.getBoolean("show_label", false)
        set(value) = sp.edit().putBoolean("show_label", value).apply()

    var themeMode: Int
        get() = sp.getInt("theme_mode", THEME_SYSTEM)
        set(value) = sp.edit().putInt("theme_mode", value).apply()

    var palette: Int
        get() = sp.getInt("palette", PALETTE_AURORA)
        set(value) = sp.edit().putInt("palette", value).apply()

    var haptics: Boolean
        get() = sp.getBoolean("haptics", true)
        set(value) = sp.edit().putBoolean("haptics", value).apply()

    /** Wall-clock timestamp of the last widget tap, or [TapGestureLogic.NO_TAP]. */
    var lastTapAt: Long
        get() = sp.getLong("last_tap_at", TapGestureLogic.NO_TAP)
        set(value) = sp.edit().putLong("last_tap_at", value).apply()

    /** Wall-clock timestamp of the last successful lock, or -1 if never. */
    var lastLockAt: Long
        get() = sp.getLong("last_lock_at", -1L)
        set(value) = sp.edit().putLong("last_lock_at", value).apply()

    /**
     * Random per-install secret embedded in the widget's immutable tap PendingIntent.
     * Other apps can neither read this private prefs file nor the PendingIntent's
     * extras, so a broadcast without the right token is a forgery and is dropped.
     */
    val tapToken: String
        get() {
            sp.getString("tap_token", null)?.let { return it }
            val bytes = ByteArray(16).also { SecureRandom().nextBytes(it) }
            val token = bytes.joinToString("") { "%02x".format(it) }
            sp.edit().putString("tap_token", token).commit()
            return token
        }
}
