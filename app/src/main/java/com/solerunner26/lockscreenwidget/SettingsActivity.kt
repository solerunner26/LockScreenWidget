package com.solerunner26.lockscreenwidget

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.RadioGroup
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import java.text.DateFormat
import java.util.Date

/**
 * Setup & settings screen (also the launcher entry point).
 *
 * Security: the window is FLAG_SECURE, so this UI can never be captured by a
 * screenshot or a screen recorder — an eavesdropper recording the phone screen gets
 * nothing from this app. The activity holds no sensitive data anyway.
 */
class SettingsActivity : Activity() {

    private lateinit var prefs: Prefs

    private lateinit var tvStatus: TextView
    private lateinit var btnAdmin: Button
    private lateinit var rgGesture: RadioGroup
    private lateinit var rgSpeed: RadioGroup
    private lateinit var rgIconColor: RadioGroup
    private lateinit var rgBackground: RadioGroup
    private lateinit var switchLabel: Switch
    private lateinit var switchHaptics: Switch
    private lateinit var btnTestLock: Button
    private lateinit var tvLastLocked: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        // Block screenshots and screen recording of this app's UI.
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        prefs = Prefs.get(this)

        tvStatus = findViewById(R.id.tv_status)
        btnAdmin = findViewById(R.id.btn_admin)
        rgGesture = findViewById(R.id.rg_gesture)
        rgSpeed = findViewById(R.id.rg_speed)
        rgIconColor = findViewById(R.id.rg_icon_color)
        rgBackground = findViewById(R.id.rg_background)
        switchLabel = findViewById(R.id.switch_label)
        switchHaptics = findViewById(R.id.switch_haptics)
        btnTestLock = findViewById(R.id.btn_test_lock)
        tvLastLocked = findViewById(R.id.tv_last_locked)

        applyPrefsToUi()
        installListeners()
        refreshStatus()
    }

    override fun onResume() {
        super.onResume()
        refreshStatus()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_ADMIN) refreshStatus()
    }

    private fun refreshStatus() {
        val active = LockController.isAdminActive(this)
        tvStatus.text = getString(if (active) R.string.status_active else R.string.status_inactive)
        tvStatus.setTextColor(getColor(if (active) R.color.ok_green else R.color.warn_red))
        btnAdmin.text = getString(if (active) R.string.btn_deactivate else R.string.btn_activate)

        val lastLock = prefs.lastLockAt
        tvLastLocked.text = if (lastLock > 0) {
            val formatted = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                .format(Date(lastLock))
            getString(R.string.last_locked_at, formatted)
        } else {
            getString(R.string.last_locked_none)
        }
    }

    private fun applyPrefsToUi() {
        rgGesture.check(
            if (prefs.gestureMode == Prefs.MODE_SINGLE_TAP) R.id.radio_single else R.id.radio_double
        )
        rgSpeed.check(
            when (prefs.tapWindowMs) {
                300L -> R.id.radio_300
                600L -> R.id.radio_600
                else -> R.id.radio_450
            }
        )
        rgIconColor.check(
            when (prefs.iconColor) {
                0xFF202124.toInt() -> R.id.radio_color_dark
                0xFF1A73E8.toInt() -> R.id.radio_color_blue
                else -> R.id.radio_color_white
            }
        )
        rgBackground.check(
            when (prefs.backgroundStyle) {
                Prefs.BG_TRANSPARENT -> R.id.radio_bg_transparent
                Prefs.BG_SOLID -> R.id.radio_bg_solid
                else -> R.id.radio_bg_translucent
            }
        )
        switchLabel.isChecked = prefs.showLabel
        switchHaptics.isChecked = prefs.haptics
    }

    private fun installListeners() {
        btnAdmin.setOnClickListener {
            if (LockController.isAdminActive(this)) {
                // Revoking is instant and needs no confirmation.
                val dpm = getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager
                dpm.removeActiveAdmin(LockController.adminComponent(this))
                Toast.makeText(this, R.string.msg_admin_revoked, Toast.LENGTH_SHORT).show()
                refreshStatus()
            } else {
                val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                    putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, LockController.adminComponent(this@SettingsActivity))
                    putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, getString(R.string.admin_explanation))
                }
                startActivityForResult(intent, REQUEST_ENABLE_ADMIN)
            }
        }

        rgGesture.setOnCheckedChangeListener { _, checkedId ->
            prefs.gestureMode =
                if (checkedId == R.id.radio_single) Prefs.MODE_SINGLE_TAP else Prefs.MODE_DOUBLE_TAP
            prefs.lastTapAt = TapGestureLogic.NO_TAP
            refreshWidgets()
        }

        rgSpeed.setOnCheckedChangeListener { _, checkedId ->
            prefs.tapWindowMs = when (checkedId) {
                R.id.radio_300 -> 300L
                R.id.radio_600 -> 600L
                else -> 450L
            }
        }

        rgIconColor.setOnCheckedChangeListener { _, checkedId ->
            prefs.iconColor = when (checkedId) {
                R.id.radio_color_dark -> 0xFF202124.toInt()
                R.id.radio_color_blue -> 0xFF1A73E8.toInt()
                else -> 0xFFFFFFFF.toInt()
            }
            refreshWidgets()
        }

        rgBackground.setOnCheckedChangeListener { _, checkedId ->
            prefs.backgroundStyle = when (checkedId) {
                R.id.radio_bg_transparent -> Prefs.BG_TRANSPARENT
                R.id.radio_bg_solid -> Prefs.BG_SOLID
                else -> Prefs.BG_TRANSLUCENT
            }
            refreshWidgets()
        }

        switchLabel.setOnCheckedChangeListener { _, isChecked ->
            prefs.showLabel = isChecked
            refreshWidgets()
        }

        switchHaptics.setOnCheckedChangeListener { _, isChecked ->
            prefs.haptics = isChecked
        }

        btnTestLock.setOnClickListener { LockController.lock(this) }
    }

    /** Ask the widget provider to re-render every placed widget with the new look. */
    private fun refreshWidgets() {
        sendBroadcast(
            Intent(this, LockWidgetProvider::class.java).setAction(LockWidgetProvider.ACTION_REFRESH)
        )
    }

    companion object {
        private const val REQUEST_ENABLE_ADMIN = 4001
    }
}
