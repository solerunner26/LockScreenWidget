package com.solerunner26.lockscreenwidget.ui

import android.app.admin.DevicePolicyManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import android.text.format.DateFormat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.solerunner26.lockscreenwidget.LockController
import com.solerunner26.lockscreenwidget.LockDeviceAdminReceiver
import com.solerunner26.lockscreenwidget.LockWidgetProvider
import com.solerunner26.lockscreenwidget.Prefs
import com.solerunner26.lockscreenwidget.R
import com.solerunner26.lockscreenwidget.TapGestureLogic
import com.solerunner26.lockscreenwidget.ui.components.GradientButton
import com.solerunner26.lockscreenwidget.ui.theme.GradientBrush
import com.solerunner26.lockscreenwidget.ui.theme.OkGreen
import com.solerunner26.lockscreenwidget.ui.theme.WarnRed
import java.util.Date
import kotlinx.coroutines.launch

private val ICON_WHITE = 0xFFFFFFFF.toInt()
private val ICON_DARK = 0xFF202124.toInt()
private val ICON_VIOLET = 0xFF6D4AC8.toInt()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(themeMode: Int, onThemeModeChange: (Int) -> Unit) {
    val context = LocalContext.current
    val prefs = remember { Prefs.get(context) }
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

    var adminActive by remember { mutableStateOf(LockController.isAdminActive(context)) }
    var receiverOk by remember { mutableStateOf(checkAdminReceiverRegistered(context)) }
    var showHelp by remember { mutableStateOf(false) }

    var gestureMode by remember { mutableStateOf(prefs.gestureMode) }
    var tapWindow by remember { mutableStateOf(prefs.tapWindowMs) }
    var showIcon by remember { mutableStateOf(prefs.showIcon) }
    var iconColor by remember { mutableStateOf(prefs.iconColor) }
    var bgStyle by remember { mutableStateOf(prefs.backgroundStyle) }
    var showLabel by remember { mutableStateOf(prefs.showLabel) }
    var haptics by remember { mutableStateOf(prefs.haptics) }
    var lastLockAt by remember { mutableStateOf(prefs.lastLockAt) }

    fun refreshAdmin() {
        adminActive = LockController.isAdminActive(context)
        receiverOk = checkAdminReceiverRegistered(context)
        lastLockAt = prefs.lastLockAt
    }

    fun refreshWidgets() {
        context.sendBroadcast(
            Intent(context, LockWidgetProvider::class.java).setAction(LockWidgetProvider.ACTION_REFRESH)
        )
    }

    // Re-check admin status whenever we return to this screen (e.g. after the
    // system activation dialog).
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) refreshAdmin()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val adminLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { refreshAdmin() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(onClick = { showHelp = true }) {
                        Icon(
                            imageVector = Icons.Default.Help,
                            contentDescription = stringResource(R.string.help_title)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Gradient header with the app logo
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(GradientBrush)
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(R.drawable.logo),
                        contentDescription = null,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .border(2.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                    )
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(
                            stringResource(R.string.app_name),
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Private double-tap screen lock",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // ---- Status / device admin ----
            SectionCard(title = stringResource(R.string.app_name)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = if (adminActive) OkGreen else WarnRed
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = stringResource(if (adminActive) R.string.status_active else R.string.status_inactive),
                        fontWeight = FontWeight.Medium,
                        color = if (adminActive) OkGreen else WarnRed,
                        fontSize = 14.sp
                    )
                }
                if (!receiverOk) {
                    Text(
                        stringResource(R.string.admin_component_missing),
                        color = WarnRed,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
                Spacer(Modifier.height(12.dp))
                GradientButton(
                    text = stringResource(if (adminActive) R.string.btn_deactivate else R.string.btn_activate),
                    onClick = {
                        if (LockController.isAdminActive(context)) {
                            val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
                            dpm.removeActiveAdmin(LockController.adminComponent(context))
                            scope.launch { snackbar.showSnackbar(context.getString(R.string.msg_admin_revoked)) }
                            refreshAdmin()
                        } else {
                            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, LockController.adminComponent(context))
                                putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, context.getString(R.string.admin_explanation))
                            }
                            try {
                                adminLauncher.launch(intent)
                            } catch (_: ActivityNotFoundException) {
                                openSecuritySettings(context, snackbar, scope)
                            }
                        }
                    }
                )
                Spacer(Modifier.height(4.dp))
                TextButton(onClick = { openSecuritySettings(context, snackbar, scope) }) {
                    Text(stringResource(R.string.btn_open_admin_settings))
                }
                if (!adminActive) {
                    Text(
                        stringResource(R.string.hint_admin_fallback),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                val ll = lastLockAt
                Text(
                    if (ll > 0) {
                        stringResource(
                            R.string.last_locked_at,
                            DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(ll))
                        )
                    } else {
                        stringResource(R.string.last_locked_none)
                    },
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // ---- Lock gesture ----
            SectionCard(title = stringResource(R.string.section_gesture)) {
                RadioRow(
                    text = stringResource(R.string.gesture_double),
                    selected = gestureMode == Prefs.MODE_DOUBLE_TAP
                ) {
                    prefs.gestureMode = Prefs.MODE_DOUBLE_TAP
                    prefs.lastTapAt = TapGestureLogic.NO_TAP
                    gestureMode = Prefs.MODE_DOUBLE_TAP
                    refreshWidgets()
                }
                RadioRow(
                    text = stringResource(R.string.gesture_single),
                    selected = gestureMode == Prefs.MODE_SINGLE_TAP
                ) {
                    prefs.gestureMode = Prefs.MODE_SINGLE_TAP
                    gestureMode = Prefs.MODE_SINGLE_TAP
                    refreshWidgets()
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    stringResource(R.string.speed_label),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                SegmentedRow(
                    options = listOf(
                        300L to stringResource(R.string.speed_fast),
                        450L to stringResource(R.string.speed_normal),
                        600L to stringResource(R.string.speed_slow)
                    ),
                    selected = tapWindow
                ) { v -> prefs.tapWindowMs = v; tapWindow = v }
            }

            // ---- Widget look ----
            SectionCard(title = stringResource(R.string.section_look)) {
                SwitchRow(
                    text = stringResource(R.string.opt_show_icon),
                    checked = showIcon
                ) { v ->
                    prefs.showIcon = v
                    showIcon = v
                    refreshWidgets()
                }
                if (!showIcon) {
                    Text(
                        stringResource(R.string.widget_invisible_note),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                if (showIcon) {
                    Spacer(Modifier.height(8.dp))
                    Text(stringResource(R.string.icon_color_label), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    SegmentedRow(
                        options = listOf(
                            ICON_WHITE to stringResource(R.string.color_white),
                            ICON_DARK to stringResource(R.string.color_dark),
                            ICON_VIOLET to stringResource(R.string.color_violet)
                        ),
                        selected = iconColor
                    ) { v -> prefs.iconColor = v; iconColor = v; refreshWidgets() }
                    Spacer(Modifier.height(8.dp))
                    Text(stringResource(R.string.bg_label), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    SegmentedRow(
                        options = listOf(
                            Prefs.BG_TRANSLUCENT to stringResource(R.string.bg_translucent),
                            Prefs.BG_TRANSPARENT to stringResource(R.string.bg_transparent),
                            Prefs.BG_SOLID to stringResource(R.string.bg_solid)
                        ),
                        selected = bgStyle
                    ) { v -> prefs.backgroundStyle = v; bgStyle = v; refreshWidgets() }
                    Spacer(Modifier.height(8.dp))
                    SwitchRow(
                        text = stringResource(R.string.opt_show_label),
                        checked = showLabel
                    ) { v -> prefs.showLabel = v; showLabel = v; refreshWidgets() }
                }
            }

            // ---- Appearance / general ----
            SectionCard(title = stringResource(R.string.section_theme)) {
                Text(stringResource(R.string.theme_mode_label), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                SegmentedRow(
                    options = listOf(
                        Prefs.THEME_SYSTEM to stringResource(R.string.theme_system),
                        Prefs.THEME_LIGHT to stringResource(R.string.theme_light),
                        Prefs.THEME_DARK to stringResource(R.string.theme_dark)
                    ),
                    selected = themeMode
                ) { v -> onThemeModeChange(v) }
                Spacer(Modifier.height(8.dp))
                SwitchRow(
                    text = stringResource(R.string.opt_haptics),
                    checked = haptics
                ) { v -> prefs.haptics = v; haptics = v }
            }

            Spacer(Modifier.height(12.dp))
            GradientButton(
                text = stringResource(R.string.btn_test_lock),
                onClick = {
                    LockController.lock(context)
                    lastLockAt = prefs.lastLockAt
                }
            )
            Spacer(Modifier.height(24.dp))
        }
    }

    if (showHelp) {
        HelpDialog(onDismiss = { showHelp = false })
    }
}

/** Self-check: is our DeviceAdminReceiver properly registered with its admin meta-data? */
private fun checkAdminReceiverRegistered(context: Context): Boolean {
    return try {
        val info = context.packageManager.getReceiverInfo(
            ComponentName(context, LockDeviceAdminReceiver::class.java),
            PackageManager.GET_META_DATA
        )
        info.metaData?.containsKey("android.app.admin.device_admin") == true
    } catch (_: Exception) {
        false
    }
}

/** Fallback: open the system Security page where "Device admin apps" lives. */
private fun openSecuritySettings(context: Context, snackbar: SnackbarHostState, scope: kotlinx.coroutines.CoroutineScope) {
    try {
        context.startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
        scope.launch { snackbar.showSnackbar(context.getString(R.string.hint_admin_fallback)) }
    } catch (_: ActivityNotFoundException) {
        // ignore
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun RadioRow(text: String, selected: Boolean, onSelect: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onSelect)
        Text(text, Modifier.padding(start = 8.dp), fontSize = 14.sp)
    }
}

@Composable
private fun SwitchRow(text: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, Modifier.weight(1f), fontSize = 14.sp)
        Switch(checked = checked, onCheckedChange = onChecked)
    }
}

@Composable
private fun <T> SegmentedRow(options: List<Pair<T, String>>, selected: T, onSelected: (T) -> Unit) {
    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, (value, label) ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index, options.size),
                checked = value == selected,
                onClick = { onSelected(value) }
            ) {
                Text(label, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun HelpSection(title: String, body: String) {
    Text(
        title,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
    )
    Text(body, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 18.sp)
}

/** All the help text lives here — the main settings screen stays clean. */
@Composable
private fun HelpDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.help_title), fontWeight = FontWeight.Bold) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                HelpSection(
                    stringResource(R.string.section_how_to),
                    stringResource(R.string.how_to_text) + "\n\n" + stringResource(R.string.troubleshoot_admin)
                )
                HelpSection(stringResource(R.string.section_privacy), stringResource(R.string.privacy_text))
                HelpSection(stringResource(R.string.section_about), stringResource(R.string.about_text))
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("OK") }
        }
    )
}
