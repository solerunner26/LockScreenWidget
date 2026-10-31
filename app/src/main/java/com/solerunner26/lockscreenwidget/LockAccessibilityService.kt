package com.solerunner26.lockscreenwidget

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Build
import android.view.accessibility.AccessibilityEvent

/**
 * OPTIONAL "power-button lock".
 *
 * Device-admin lockNow() makes Android demand the PIN on the next unlock ("device
 * was locked by work policy", fingerprint disabled). The accessibility global action
 * GLOBAL_ACTION_LOCK_SCREEN locks exactly like the power button instead, so
 * fingerprint/face unlock keep working.
 *
 * This service is deliberately blind (see res/xml/lock_accessibility_service.xml,
 * enforced by tools/validate_project.py):
 *  - canRetrieveWindowContent="false" -> it cannot read any screen content
 *  - no accessibilityEventTypes       -> the system sends it no events at all
 *  - packageNames = this app only     -> even if it did, only from Quick Lock
 * Its only capability used is performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN).
 * It is off until the user enables it in Android's Accessibility settings.
 */
class LockAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        instance = this
        LockWidgetProvider.updateAll(this)
    }

    // Never called: the service subscribes to no event types.
    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit

    override fun onInterrupt() = Unit

    override fun onUnbind(intent: Intent?): Boolean {
        instance = null
        LockWidgetProvider.updateAll(this)
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        instance = null
        super.onDestroy()
    }

    companion object {
        @Volatile
        private var instance: LockAccessibilityService? = null

        /** True while the user has the service enabled (the system keeps it bound). */
        val isRunning: Boolean
            get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && instance != null

        /** Locks like the power button. Returns false when the service is off. */
        fun lockScreen(): Boolean {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return false
            return instance?.performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN) ?: false
        }
    }
}
