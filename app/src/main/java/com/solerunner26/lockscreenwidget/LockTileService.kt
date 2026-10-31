package com.solerunner26.lockscreenwidget

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

/**
 * Optional Quick Settings tile ("Lock screen") — a second quick-access trigger next
 * to Wi-Fi / Bluetooth in the notification shade.
 *
 * The service is bound with BIND_QUICK_SETTINGS_TILE, so only the system can bind it,
 * and the system only starts it while the tile is visible/used. It never runs on its
 * own, so it costs no battery.
 */
class LockTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        qsTile?.apply {
            label = getString(R.string.tile_label)
            state = Tile.STATE_INACTIVE
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                subtitle = getString(
                    if (LockController.isAdminActive(applicationContext)) R.string.tile_subtitle_ready
                    else R.string.tile_subtitle_setup
                )
            }
            updateTile()
        }
    }

    override fun onClick() {
        super.onClick()
        if (LockController.isAdminActive(applicationContext)) {
            LockController.lock(applicationContext)
        } else {
            // A tile may not start activities directly on Android 14+; it must
            // collapse the shade and hand the system a PendingIntent.
            openSetup()
        }
    }

    @SuppressLint("StartActivityAndCollapseDeprecated")
    private fun openSetup() {
        val intent = Intent(this, SettingsActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startActivityAndCollapse(
                PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            )
        } else {
            @Suppress("DEPRECATION")
            startActivityAndCollapse(intent)
        }
    }
}
