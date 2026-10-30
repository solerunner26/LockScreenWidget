package com.solerunner26.lockscreenwidget

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
            updateTile()
        }
    }

    override fun onClick() {
        super.onClick()
        // Locks immediately when admin is active; otherwise opens the setup screen.
        LockController.lock(applicationContext)
        qsTile?.apply {
            state = Tile.STATE_INACTIVE
            updateTile()
        }
    }
}
