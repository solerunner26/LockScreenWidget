package com.solerunner26.lockscreenwidget

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented smoke tests (run on a device/emulator with:
 * ./gradlew :app:connectedDebugAndroidTest).
 *
 * These verify the security-relevant behavior: without device admin the app must
 * refuse to lock and report ADMIN_MISSING (never crash), and the gesture logic holds
 * on a real Android runtime.
 */
@RunWith(AndroidJUnit4::class)
class SmokeTest {

    @Test
    fun prefsHaveSafeDefaults() {
        val prefs = Prefs.get(ApplicationProvider.getApplicationContext())
        assertEquals(Prefs.MODE_DOUBLE_TAP, prefs.gestureMode)
        assertEquals(450L, prefs.tapWindowMs)
        assertTrue(prefs.haptics)
        assertFalse(prefs.showIcon) // widget is invisible by default
        assertEquals(Prefs.BG_TRANSPARENT, prefs.backgroundStyle)
        assertEquals(Prefs.THEME_SYSTEM, prefs.themeMode)
        assertEquals(TapGestureLogic.NO_TAP, prefs.lastTapAt)
    }

    @Test
    fun lockWithoutAdminReportsAdminMissingAndDoesNotCrash() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        // On a test device admin is not active, so this must be ADMIN_MISSING, not a crash.
        if (!LockController.isAdminActive(context)) {
            assertEquals(LockResult.ADMIN_MISSING, LockController.tryLock(context))
        }
    }

    @Test
    fun adminComponentIsBoundToThisPackage() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val component = LockController.adminComponent(context)
        assertEquals(context.packageName, component.packageName)
        assertEquals(LockDeviceAdminReceiver::class.java.name, component.className)
    }

    @Test
    fun gestureLogicMatchesSpec() {
        assertFalse(TapGestureLogic.isDoubleTap(TapGestureLogic.NO_TAP, 100L, 450L))
        assertTrue(TapGestureLogic.isDoubleTap(100L, 550L, 450L))
        assertFalse(TapGestureLogic.isDoubleTap(100L, 551L, 450L))
    }
}
