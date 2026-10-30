package com.solerunner26.lockscreenwidget

import android.app.admin.DeviceAdminReceiver

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
 */
class LockDeviceAdminReceiver : DeviceAdminReceiver()
