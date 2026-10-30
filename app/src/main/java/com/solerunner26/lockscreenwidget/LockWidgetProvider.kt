package com.solerunner26.lockscreenwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Bundle
import android.os.Process
import android.view.View
import android.widget.RemoteViews

/**
 * Home-screen widget provider.
 *
 * The widget is a freely resizable, repositionable tap area. Taps arrive here as an
 * explicit, immutable PendingIntent broadcast created by this app; the custom
 * actions are additionally guarded by a caller-UID check so no other app can forge
 * a "lock" broadcast.
 *
 * Battery: this receiver runs only when the system delivers a widget event or the
 * user taps the widget (updatePeriodMillis = 0 in lock_widget_info.xml). It never
 * runs periodically, holds no wake lock and starts no service.
 */
class LockWidgetProvider : AppWidgetProvider() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_WIDGET_TAP -> {
                if (!isCallerAllowed()) return // ignore forged broadcasts from other apps
                handleTap(context)
            }
            ACTION_REFRESH -> {
                if (!isCallerAllowed()) return
                updateAllWidgets(context)
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
            updateAllWidgets(context)
        } else {
            prefs.lastTapAt = now
            // Re-render so the widget shows the "armed — tap again" state.
            if (prefs.gestureMode == Prefs.MODE_DOUBLE_TAP) updateAllWidgets(context)
        }
    }

    private fun updateAllWidgets(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        val ids = manager.getAppWidgetIds(ComponentName(context, LockWidgetProvider::class.java))
        for (id in ids) renderWidget(context, manager, id)
    }

    private fun renderWidget(context: Context, manager: AppWidgetManager, appWidgetId: Int) {
        val prefs = Prefs.get(context)
        val views = RemoteViews(context.packageName, R.layout.widget_lock)

        // The ENTIRE widget area is the tap target: the user decides how big the
        // lock zone is by resizing the widget on the home screen.
        views.setOnClickPendingIntent(R.id.widget_root, tapPendingIntent(context))

        val now = System.currentTimeMillis()
        val armed = prefs.gestureMode == Prefs.MODE_DOUBLE_TAP &&
            TapGestureLogic.isDoubleTap(prefs.lastTapAt, now, prefs.tapWindowMs)

        if (prefs.showIcon) {
            // Visible mode: lock icon (+ optional label) with a configurable background.
            views.setViewVisibility(R.id.widget_icon, View.VISIBLE)
            views.setImageViewResource(R.id.widget_icon, R.drawable.ic_lock_widget)
            views.setInt(R.id.widget_icon, "setColorFilter", if (armed) ARMED_TINT else prefs.iconColor)

            val background = when (prefs.backgroundStyle) {
                Prefs.BG_TRANSPARENT -> R.drawable.widget_bg_transparent
                Prefs.BG_SOLID -> R.drawable.widget_bg_solid
                else -> if (armed) R.drawable.widget_bg_armed else R.drawable.widget_bg_translucent
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
        val intent = Intent(context, LockWidgetProvider::class.java).setAction(ACTION_WIDGET_TAP)
        // Explicit component + FLAG_IMMUTABLE: only this receiver can receive it and
        // nobody can mutate it. The caller-UID check in onReceive is the second layer.
        return PendingIntent.getBroadcast(
            context,
            REQUEST_TAP,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    /**
     * Custom actions are only honored when the broadcast was sent by this app itself
     * (its PendingIntent fires with our UID) or by the system. Anything else is
     * dropped, so a malicious app cannot use this receiver to lock the user's phone.
     */
    private fun isCallerAllowed(): Boolean {
        val uid = Binder.getCallingUid()
        // 1000 = android.os.Process.SYSTEM_UID (written as a literal for clarity on all API levels)
        return uid == Process.myUid() || uid == SYSTEM_UID
    }

    companion object {
        const val ACTION_WIDGET_TAP = "com.solerunner26.lockscreenwidget.action.WIDGET_TAP"
        const val ACTION_REFRESH = "com.solerunner26.lockscreenwidget.action.REFRESH"

        private const val REQUEST_TAP = 1001
        private const val SYSTEM_UID = 1000
        private val ARMED_TINT = 0xFF6D4AC8.toInt() // violet flash for the "tap again" state
    }
}
