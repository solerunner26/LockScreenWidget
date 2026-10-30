package com.solerunner26.lockscreenwidget

import android.app.Application

/**
 * Minimal Application class.
 *
 * The app deliberately has no global state, no SDKs, no analytics and no network
 * code — there is nothing to initialize. Keeping this class tiny makes the whole
 * app easy to audit: everything interesting happens in the 6 small classes next to
 * this one.
 */
class QuickLockApp : Application()
