package com.solerunner26.lockscreenwidget

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent

/**
 * Device admin receiver.
 *
 * The declared policy (res/xml/device_admin_policies.xml) contains ONLY
 * `<force-lock />`, i.e. USES_POLICY_FORCE_LOCK. That is the single capability the
 * app needs to call DevicePolicyManager.lockNow(). It cannot read the screen, see
 * notifications or OTPs, wipe data, change passwords, or access any user content.
 *
 * This is the privacy-preserving alternative to the accessibility service used by
 * apps like "Screen Off Widget": device admin CANNOT read the screen at all.
 *
 * The manifest entry MUST keep its DEVICE_ADMIN_ENABLED intent-filter: Settings and
 * the activation dialog only consider receivers that resolve for that action.
 */
class LockDeviceAdminReceiver : DeviceAdminReceiver() {

    // Widgets switch between "lock" taps and "open setup" taps depending on admin
    // status, so redraw them whenever the user grants or revokes it.
    override fun onEnabled(context: Context, intent: Intent) {
        LockWidgetProvider.updateAll(context)
    }

    override fun onDisabled(context: Context, intent: Intent) {
        LockWidgetProvider.updateAll(context)
    }
}
