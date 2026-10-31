package com.solerunner26.lockscreenwidget

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast

/** Outcome of a lock attempt. */
enum class LockResult {
    LOCKED,
    ADMIN_MISSING,
    ERROR
}

/**
 * Single place where the lock happens. Two ways, in order:
 *  1. [LockAccessibilityService.lockScreen] — optional "power-button lock"; keeps
 *     fingerprint unlock working. The service is configured blind (no window
 *     content, no events), so it cannot read the screen.
 *  2. [DevicePolicyManager.lockNow] — device admin, force-lock policy only. Android
 *     then requires the PIN for the next unlock ("locked by work policy").
 */
object LockController {

    fun adminComponent(context: Context): ComponentName =
        ComponentName(context, LockDeviceAdminReceiver::class.java)

    /** The power-button lock (accessibility) is enabled and connected. */
    fun isPowerButtonLockOn(): Boolean = LockAccessibilityService.isRunning

    /** At least one lock method is available. */
    fun canLock(context: Context): Boolean = isPowerButtonLockOn() || isAdminActive(context)

    fun isAdminActive(context: Context): Boolean {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
            ?: return false
        return try {
            dpm.isAdminActive(adminComponent(context))
        } catch (_: SecurityException) {
            false
        }
    }

    /**
     * Attempts to lock the screen. No UI side effects — safe to call from tests and
     * from any context.
     */
    fun tryLock(context: Context): LockResult {
        if (LockAccessibilityService.lockScreen()) {
            Prefs.get(context).lastLockAt = System.currentTimeMillis()
            return LockResult.LOCKED
        }
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
            ?: return LockResult.ERROR
        if (!isAdminActive(context)) return LockResult.ADMIN_MISSING
        return try {
            dpm.lockNow() // the single privileged operation of this app
            Prefs.get(context).lastLockAt = System.currentTimeMillis()
            LockResult.LOCKED
        } catch (_: SecurityException) {
            LockResult.ADMIN_MISSING
        } catch (_: Exception) {
            LockResult.ERROR
        }
    }

    /**
     * tryLock() plus user feedback: a short vibration + toast on success, and the
     * settings screen opened when device admin is not active yet.
     */
    fun lock(context: Context): LockResult {
        return when (tryLock(context)) {
            LockResult.LOCKED -> {
                if (Prefs.get(context).haptics) vibrate(context)
                Toast.makeText(context, R.string.msg_screen_locked, Toast.LENGTH_SHORT).show()
                LockResult.LOCKED
            }
            LockResult.ADMIN_MISSING -> {
                Toast.makeText(context, R.string.msg_admin_needed, Toast.LENGTH_LONG).show()
                openSettings(context)
                LockResult.ADMIN_MISSING
            }
            LockResult.ERROR -> {
                Toast.makeText(context, R.string.msg_lock_failed, Toast.LENGTH_LONG).show()
                LockResult.ERROR
            }
        }
    }

    fun openSettings(context: Context) {
        val intent = Intent(context, SettingsActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    /** Best-effort short buzz confirming the lock. Requires only VIBRATE permission. */
    private fun vibrate(context: Context) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(40)
            }
        } catch (_: Exception) {
            // Vibration is cosmetic; never fail because of it.
        }
    }
}
