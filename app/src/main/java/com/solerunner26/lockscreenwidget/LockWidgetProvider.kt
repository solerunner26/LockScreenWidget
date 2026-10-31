package com.solerunner26.lockscreenwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.RemoteViews

/**
 * Home-screen widget provider.
 *
 * The widget is a freely resizable, repositionable tap area. Taps arrive here as an
 * explicit, immutable PendingIntent broadcast created by this app. The intent carries
 * a random per-install secret ([Prefs.tapToken]); broadcasts without it are forgeries
 * from other apps and are dropped.
 *
 * Battery: this receiver runs only when the system delivers a widget event or the
 * user taps the widget (updatePeriodMillis = 0 in lock_widget_info.xml). It never
 * runs periodically, holds no wake lock and starts no service. The only delayed work
 * is the one-shot "disarm" redraw below, which lives inside the tap broadcast
 * itself (goAsync, < 1 s) and needs no alarm.
 */
class LockWidgetProvider : AppWidgetProvider() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_WIDGET_TAP -> {
                if (!isTokenValid(context, intent)) return // forged broadcast from another app
                handleTap(context)
            }
            else -> super.onReceive(context, intent) // standard appwidget actions -> onUpdate() etc.
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (id in appWidgetIds) renderWidget(context, appWidgetManager, id)
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        renderWidget(context, appWidgetManager, appWidgetId)
    }

    private fun handleTap(context: Context) {
        val prefs = Prefs.get(context)
        val now = System.currentTimeMillis()
        val shouldLock = if (prefs.gestureMode == Prefs.MODE_SINGLE_TAP) {
            true
        } else {
            TapGestureLogic.isDoubleTap(prefs.lastTapAt, now, prefs.tapWindowMs)
        }

        if (shouldLock) {
            prefs.lastTapAt = TapGestureLogic.NO_TAP // reset so a third tap doesn't re-lock
            LockController.lock(context)
            updateAll(context)
        } else {
            prefs.lastTapAt = now
            // Show the "armed — tap again" state, then clear it again once the
            // double-tap window has passed so the zone goes back to invisible.
            updateAll(context)
            val pending = goAsync()
            Handler(Looper.getMainLooper()).postDelayed({
                try {
                    updateAll(context)
                } finally {
                    pending.finish()
                }
            }, prefs.tapWindowMs + DISARM_SLACK_MS)
        }
    }

    private fun isTokenValid(context: Context, intent: Intent): Boolean {
        val token = intent.getStringExtra(EXTRA_TOKEN) ?: return false
        return token == Prefs.get(context).tapToken
    }

    companion object {
        const val ACTION_WIDGET_TAP = "com.solerunner26.lockscreenwidget.action.WIDGET_TAP"
        private const val EXTRA_TOKEN = "token"

        private const val REQUEST_TAP = 1001
        private const val REQUEST_SETUP = 1002
        private const val DISARM_SLACK_MS = 60L
        private val ARMED_TINT = 0xFF6D4AC8.toInt() // violet flash for the "tap again" state

        /**
         * Re-renders every placed widget. Called in-process (settings changes,
         * admin enabled/disabled) — no broadcast needed, so nothing to forge.
         */
        fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, LockWidgetProvider::class.java))
            for (id in ids) renderWidget(context, manager, id)
        }

        private fun renderWidget(context: Context, manager: AppWidgetManager, appWidgetId: Int) {
            val prefs = Prefs.get(context)
            val views = RemoteViews(context.packageName, R.layout.widget_lock)

            // The ENTIRE widget area is the tap target: the user decides how big the
            // lock zone is by resizing the widget on the home screen. Until device
            // admin is granted, a tap opens the setup screen directly (an activity
            // PendingIntent is always allowed to start, unlike a background launch).
            val adminActive = LockController.isAdminActive(context)
            views.setOnClickPendingIntent(
                R.id.widget_root,
                if (adminActive) tapPendingIntent(context) else setupPendingIntent(context)
            )

            val now = System.currentTimeMillis()
            val armed = adminActive && prefs.gestureMode == Prefs.MODE_DOUBLE_TAP &&
                TapGestureLogic.isDoubleTap(prefs.lastTapAt, now, prefs.tapWindowMs)

            if (prefs.showIcon) {
                // Visible mode: lock icon (+ optional label) with a configurable background.
                views.setViewVisibility(R.id.widget_icon, View.VISIBLE)
                views.setImageViewResource(R.id.widget_icon, R.drawable.ic_lock_widget)
                views.setInt(R.id.widget_icon, "setColorFilter", if (armed) ARMED_TINT else prefs.iconColor)

                val background = when {
                    armed -> R.drawable.widget_bg_armed
                    prefs.backgroundStyle == Prefs.BG_TRANSPARENT -> R.drawable.widget_bg_transparent
                    prefs.backgroundStyle == Prefs.BG_SOLID -> R.drawable.widget_bg_solid
                    else -> R.drawable.widget_bg_translucent
                }
                views.setInt(R.id.widget_root, "setBackgroundResource", background)

                if (prefs.showLabel) {
                    views.setViewVisibility(R.id.widget_label, View.VISIBLE)
                    views.setTextViewText(
                        R.id.widget_label,
                        context.getString(if (armed) R.string.widget_label_again else R.string.widget_label)
                    )
                    views.setTextColor(R.id.widget_label, if (armed) ARMED_TINT else prefs.iconColor)
                } else {
                    views.setViewVisibility(R.id.widget_label, View.GONE)
                }
            } else {
                // Default: fully invisible lock zone — no icon, no label, no background.
                // Only a brief subtle flash after the first tap of a double tap ("armed").
                views.setViewVisibility(R.id.widget_icon, View.GONE)
                views.setViewVisibility(R.id.widget_label, View.GONE)
                views.setInt(
                    R.id.widget_root, "setBackgroundResource",
                    if (armed) R.drawable.widget_bg_armed else R.drawable.widget_bg_transparent
                )
            }

            manager.updateAppWidget(appWidgetId, views)
        }

        private fun tapPendingIntent(context: Context): PendingIntent {
            val intent = Intent(context, LockWidgetProvider::class.java)
                .setAction(ACTION_WIDGET_TAP)
                .putExtra(EXTRA_TOKEN, Prefs.get(context).tapToken)
            // Explicit component + FLAG_IMMUTABLE: only this receiver can receive it and
            // nobody can mutate it. The secret token in onReceive is the second layer.
            return PendingIntent.getBroadcast(
                context,
                REQUEST_TAP,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        private fun setupPendingIntent(context: Context): PendingIntent {
            val intent = Intent(context, SettingsActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            return PendingIntent.getActivity(
                context,
                REQUEST_SETUP,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
    }
}
