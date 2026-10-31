package com.solerunner26.lockscreenwidget.ui

import android.app.StatusBarManager
import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.appwidget.AppWidgetManager
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Icon as PlatformIcon
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.AppSettingsAlt
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BrightnessAuto
import androidx.compose.material.icons.outlined.Construction
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.solerunner26.lockscreenwidget.BuildConfig
import com.solerunner26.lockscreenwidget.LockController
import com.solerunner26.lockscreenwidget.LockDeviceAdminReceiver
import com.solerunner26.lockscreenwidget.LockTileService
import com.solerunner26.lockscreenwidget.LockWidgetProvider
import com.solerunner26.lockscreenwidget.Prefs
import com.solerunner26.lockscreenwidget.R
import com.solerunner26.lockscreenwidget.TapGestureLogic
import com.solerunner26.lockscreenwidget.ui.components.GradientButton
import com.solerunner26.lockscreenwidget.ui.theme.AppPalette
import com.solerunner26.lockscreenwidget.ui.theme.QuickLockTheme
import com.solerunner26.lockscreenwidget.ui.theme.Violet
import com.solerunner26.lockscreenwidget.ui.theme.availablePalettes
import com.solerunner26.lockscreenwidget.ui.theme.paletteById
import java.text.DateFormat
import java.util.Date
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val ICON_WHITE = 0xFFFFFFFF.toInt()
private val ICON_DARK = 0xFF202124.toInt()
private val ICON_VIOLET = 0xFF6D4AC8.toInt()
private val ICON_MAGENTA = 0xFFC94FA0.toInt()

private enum class HelpTopic(@StringRes val title: Int, val icon: ImageVector) {
    HOW_TO(R.string.section_how_to, Icons.AutoMirrored.Outlined.MenuBook),
    ADMIN(R.string.help_admin_title, Icons.Outlined.Construction),
    PRIVACY(R.string.section_privacy, Icons.Outlined.PrivacyTip),
    ABOUT(R.string.section_about, Icons.Outlined.Info)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    themeMode: Int,
    paletteId: Int,
    onThemeModeChange: (Int) -> Unit,
    onPaletteChange: (Int) -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { Prefs.get(context) }
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val resources = LocalResources.current
    val adminExplanation = stringResource(R.string.admin_explanation)
    val adbCommand = stringResource(R.string.adb_command)

    var adminActive by remember { mutableStateOf(LockController.isAdminActive(context)) }
    var receiverOk by remember { mutableStateOf(checkAdminReceiverRegistered(context)) }
    var helpMenuOpen by remember { mutableStateOf(false) }
    var helpTopic by remember { mutableStateOf<HelpTopic?>(null) }

    var gestureMode by remember { mutableIntStateOf(prefs.gestureMode) }
    var tapWindow by remember { mutableLongStateOf(prefs.tapWindowMs) }
    var showIcon by remember { mutableStateOf(prefs.showIcon) }
    var iconColor by remember { mutableIntStateOf(prefs.iconColor) }
    var bgStyle by remember { mutableIntStateOf(prefs.backgroundStyle) }
    var showLabel by remember { mutableStateOf(prefs.showLabel) }
    var haptics by remember { mutableStateOf(prefs.haptics) }
    var lastLockAt by remember { mutableLongStateOf(prefs.lastLockAt) }

    fun refreshAdmin() {
        adminActive = LockController.isAdminActive(context)
        receiverOk = checkAdminReceiverRegistered(context)
        lastLockAt = prefs.lastLockAt
        LockWidgetProvider.updateAll(context)
    }

    fun refreshWidgets() = LockWidgetProvider.updateAll(context)

    fun toast(@StringRes res: Int) {
        scope.launch { snackbar.showSnackbar(resources.getString(res)) }
    }

    // Re-check admin status whenever we return to this screen (e.g. after the
    // system activation dialog or the Settings app).
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

    fun requestAdmin() {
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, LockController.adminComponent(context))
            putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, adminExplanation)
        }
        try {
            adminLauncher.launch(intent)
        } catch (_: ActivityNotFoundException) {
            openDeviceAdminList(context, snackbar, scope)
        }
    }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.surface,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleLarge) },
                actions = {
                    Box {
                        IconButton(onClick = { helpMenuOpen = true }) {
                            Icon(
                                Icons.AutoMirrored.Outlined.HelpOutline,
                                contentDescription = stringResource(R.string.help_title)
                            )
                        }
                        DropdownMenu(
                            expanded = helpMenuOpen,
                            onDismissRequest = { helpMenuOpen = false },
                            shape = RoundedCornerShape(20.dp),
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ) {
                            HelpTopic.entries.forEach { topic ->
                                DropdownMenuItem(
                                    text = { Text(stringResource(topic.title)) },
                                    leadingIcon = { Icon(topic.icon, contentDescription = null) },
                                    onClick = {
                                        helpMenuOpen = false
                                        helpTopic = topic
                                    }
                                )
                            }
                        }
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            HeroCard(
                adminActive = adminActive,
                lastLockAt = lastLockAt,
                onPrimary = {
                    if (adminActive) {
                        LockController.lock(context)
                        lastLockAt = prefs.lastLockAt
                    } else {
                        requestAdmin()
                    }
                }
            )

            // ---- One-time setup (only while device admin is missing) ----
            AnimatedVisibility(
                visible = !adminActive,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                SetupCard(
                    receiverOk = receiverOk,
                    onActivate = { requestAdmin() },
                    onOpenList = { openDeviceAdminList(context, snackbar, scope) },
                    onAppInfo = { openAppInfo(context) },
                    onCopyAdb = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(
                            ClipData.newPlainText("adb-dpm", adbCommand)
                        )
                        toast(R.string.adb_copied)
                    }
                )
            }

            // ---- Lock gesture ----
            SectionLabel(R.string.section_gesture)
            SettingsGroup {
                PrefItem(
                    icon = Icons.Outlined.TouchApp,
                    title = stringResource(R.string.gesture_title),
                    subtitle = stringResource(
                        if (gestureMode == Prefs.MODE_DOUBLE_TAP) R.string.gesture_double_sub else R.string.gesture_single_sub
                    )
                ) {
                    Segmented(
                        options = listOf(
                            Prefs.MODE_DOUBLE_TAP to stringResource(R.string.gesture_double),
                            Prefs.MODE_SINGLE_TAP to stringResource(R.string.gesture_single)
                        ),
                        selected = gestureMode
                    ) { v ->
                        prefs.gestureMode = v
                        prefs.lastTapAt = TapGestureLogic.NO_TAP
                        gestureMode = v
                        refreshWidgets()
                    }
                }
                AnimatedVisibility(
                    visible = gestureMode == Prefs.MODE_DOUBLE_TAP,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    PrefItem(
                        icon = Icons.Outlined.Speed,
                        title = stringResource(R.string.speed_label),
                        subtitle = stringResource(R.string.speed_sub, tapWindow)
                    ) {
                        Segmented(
                            options = listOf(
                                300L to stringResource(R.string.speed_fast),
                                450L to stringResource(R.string.speed_normal),
                                600L to stringResource(R.string.speed_slow)
                            ),
                            selected = tapWindow
                        ) { v -> prefs.tapWindowMs = v; tapWindow = v }
                    }
                }
            }

            // ---- Widget ----
            SectionLabel(R.string.section_look)
            SettingsGroup {
                GroupTile {
                    WidgetPreview(
                        showIcon = showIcon,
                        iconColor = Color(iconColor),
                        bgStyle = bgStyle,
                        showLabel = showLabel,
                        gestureMode = gestureMode,
                        tapWindowMs = tapWindow
                    )
                }
                PrefItem(
                    icon = if (showIcon) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                    title = stringResource(R.string.opt_show_icon),
                    subtitle = stringResource(if (showIcon) R.string.widget_visible_note else R.string.widget_invisible_note),
                    trailing = {
                        ThemedSwitch(showIcon) { v -> prefs.showIcon = v; showIcon = v; refreshWidgets() }
                    },
                    onClick = { val v = !showIcon; prefs.showIcon = v; showIcon = v; refreshWidgets() }
                )
                AnimatedVisibility(
                    visible = showIcon,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(GROUP_GAP)) {
                        PrefItem(
                            icon = Icons.Outlined.Palette,
                            title = stringResource(R.string.icon_color_label)
                        ) {
                            ColorSwatches(
                                options = listOf(
                                    ICON_WHITE to R.string.color_white,
                                    ICON_DARK to R.string.color_dark,
                                    ICON_VIOLET to R.string.color_violet,
                                    ICON_MAGENTA to R.string.color_magenta
                                ),
                                selected = iconColor
                            ) { v -> prefs.iconColor = v; iconColor = v; refreshWidgets() }
                        }
                        PrefItem(
                            icon = Icons.Outlined.Dashboard,
                            title = stringResource(R.string.bg_label)
                        ) {
                            Segmented(
                                options = listOf(
                                    Prefs.BG_TRANSPARENT to stringResource(R.string.bg_transparent),
                                    Prefs.BG_TRANSLUCENT to stringResource(R.string.bg_translucent),
                                    Prefs.BG_SOLID to stringResource(R.string.bg_solid)
                                ),
                                selected = bgStyle
                            ) { v -> prefs.backgroundStyle = v; bgStyle = v; refreshWidgets() }
                        }
                        PrefItem(
                            icon = Icons.AutoMirrored.Outlined.Label,
                            title = stringResource(R.string.opt_show_label),
                            trailing = {
                                ThemedSwitch(showLabel) { v -> prefs.showLabel = v; showLabel = v; refreshWidgets() }
                            },
                            onClick = { val v = !showLabel; prefs.showLabel = v; showLabel = v; refreshWidgets() }
                        )
                    }
                }
            }

            // ---- Lock triggers ----
            SectionLabel(R.string.section_triggers)
            SettingsGroup {
                PrefItem(
                    icon = Icons.Outlined.Widgets,
                    title = stringResource(R.string.trigger_widget),
                    subtitle = stringResource(R.string.trigger_widget_sub),
                    trailing = { ChevronAction() },
                    onClick = { requestPinWidget(context, snackbar, scope) }
                )
                PrefItem(
                    icon = Icons.Outlined.AppSettingsAlt,
                    title = stringResource(R.string.trigger_tile),
                    subtitle = stringResource(R.string.trigger_tile_sub),
                    trailing = { ChevronAction() },
                    onClick = { requestAddTile(context, snackbar, scope) }
                )
                PrefItem(
                    icon = Icons.Outlined.Apps,
                    title = stringResource(R.string.trigger_shortcut),
                    subtitle = stringResource(R.string.trigger_shortcut_sub)
                )
            }

            // ---- Appearance ----
            SectionLabel(R.string.section_theme)
            SettingsGroup {
                PrefItem(
                    icon = Icons.Outlined.Palette,
                    title = stringResource(R.string.theme_palette_label),
                    subtitle = stringResource(paletteById(context, paletteId).nameRes)
                ) {
                    ThemePicker(selected = paletteId, onSelect = onPaletteChange)
                }
                val alwaysDark = paletteById(context, paletteId).alwaysDark
                AnimatedContent(
                    targetState = alwaysDark,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "brightness"
                ) { night ->
                    if (night) {
                        PrefItem(
                            icon = Icons.Outlined.DarkMode,
                            title = stringResource(R.string.theme_mode_label),
                            subtitle = stringResource(R.string.theme_midnight_note)
                        )
                    } else {
                        PrefItem(
                            icon = Icons.Outlined.BrightnessAuto,
                            title = stringResource(R.string.theme_mode_label)
                        ) {
                            Segmented(
                                options = listOf(
                                    Prefs.THEME_SYSTEM to stringResource(R.string.theme_system),
                                    Prefs.THEME_LIGHT to stringResource(R.string.theme_light),
                                    Prefs.THEME_DARK to stringResource(R.string.theme_dark)
                                ),
                                icons = listOf(
                                    Icons.Outlined.BrightnessAuto,
                                    Icons.Outlined.LightMode,
                                    Icons.Outlined.DarkMode
                                ),
                                selected = themeMode,
                                onSelected = onThemeModeChange
                            )
                        }
                    }
                }
            }

            // ---- Feedback & security ----
            SectionLabel(R.string.section_general)
            SettingsGroup {
                PrefItem(
                    icon = Icons.Outlined.Vibration,
                    title = stringResource(R.string.opt_haptics),
                    subtitle = stringResource(R.string.opt_haptics_sub),
                    trailing = { ThemedSwitch(haptics) { v -> prefs.haptics = v; haptics = v } },
                    onClick = { val v = !haptics; prefs.haptics = v; haptics = v }
                )
                PrefItem(
                    icon = Icons.Outlined.AdminPanelSettings,
                    title = stringResource(R.string.admin_item_title),
                    subtitle = stringResource(if (adminActive) R.string.admin_item_active else R.string.admin_item_inactive),
                    trailing = {
                        if (adminActive) {
                            TextButton(onClick = {
                                val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
                                dpm.removeActiveAdmin(LockController.adminComponent(context))
                                toast(R.string.msg_admin_revoked)
                                scope.launch { delay(400); refreshAdmin() }
                            }) { Text(stringResource(R.string.btn_deactivate)) }
                        } else {
                            TextButton(onClick = { requestAdmin() }) { Text(stringResource(R.string.btn_enable)) }
                        }
                    }
                )
            }

            PrivacyFooter()
            Spacer(Modifier.navigationBarsPadding().height(24.dp))
        }
    }

    helpTopic?.let { topic ->
        HelpSheet(topic = topic, onDismiss = { helpTopic = null })
    }
}

// ------------------------------------------------------------------ hero

@Composable
private fun HeroCard(adminActive: Boolean, lastLockAt: Long, onPrimary: () -> Unit) {
    val extras = QuickLockTheme.extras
    Box(
        Modifier
            .padding(top = 4.dp, bottom = 8.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(extras.gradient)
    ) {
        HeroDecoration()
        Column(Modifier.padding(22.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(R.drawable.logo),
                    contentDescription = null,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color.White.copy(alpha = 0.55f), CircleShape)
                )
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.hero_title),
                        color = Color.White,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        stringResource(R.string.hero_subtitle),
                        color = Color.White.copy(alpha = 0.82f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            Spacer(Modifier.height(18.dp))
            StatusPill(adminActive)
            Spacer(Modifier.height(18.dp))
            HeroButton(
                text = stringResource(if (adminActive) R.string.btn_test_lock else R.string.btn_activate),
                icon = if (adminActive) Icons.Filled.Lock else Icons.Outlined.Shield,
                onClick = onPrimary
            )
            Spacer(Modifier.height(10.dp))
            Text(
                if (lastLockAt > 0) {
                    stringResource(
                        R.string.last_locked_at,
                        DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(lastLockAt))
                    )
                } else {
                    stringResource(R.string.last_locked_none)
                },
                color = Color.White.copy(alpha = 0.75f),
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/** Soft translucent orbs for depth — pure drawing, no images. */
@Composable
private fun BoxScope.HeroDecoration() {
    Canvas(Modifier.matchParentSize()) {
        drawCircle(Color.White.copy(alpha = 0.10f), radius = size.width * 0.42f, center = Offset(size.width * 0.98f, -size.height * 0.05f))
        drawCircle(Color.White.copy(alpha = 0.07f), radius = size.width * 0.28f, center = Offset(size.width * 0.80f, size.height * 0.30f))
        drawCircle(Color.Black.copy(alpha = 0.06f), radius = size.width * 0.50f, center = Offset(-size.width * 0.10f, size.height * 1.15f))
    }
}

@Composable
private fun StatusPill(adminActive: Boolean) {
    val pulse = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by pulse.animateFloat(
        initialValue = 1f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "pulseAlpha"
    )
    Row(
        Modifier
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.18f))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(9.dp)
                .alpha(if (adminActive) 1f else pulseAlpha)
                .clip(CircleShape)
                .background(if (adminActive) Color(0xFF7CF2A4) else Color(0xFFFFD27A))
        )
        Spacer(Modifier.width(8.dp))
        Text(
            stringResource(if (adminActive) R.string.status_active else R.string.status_inactive),
            color = Color.White,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

/** White pill on the gradient hero — the most important action on the screen. */
@Composable
private fun HeroButton(text: String, icon: ImageVector, onClick: () -> Unit) {
    val extras = QuickLockTheme.extras
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = Color.White,
        contentColor = extras.gradientStart,
        shadowElevation = 6.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = extras.gradientStart, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Text(text, style = MaterialTheme.typography.labelLarge, color = Color(0xFF1D1A24))
        }
    }
}

// ------------------------------------------------------------------ setup

@Composable
private fun SetupCard(
    receiverOk: Boolean,
    onActivate: () -> Unit,
    onOpenList: () -> Unit,
    onAppInfo: () -> Unit,
    onCopyAdb: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(Icons.Outlined.AdminPanelSettings, MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.12f))
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(stringResource(R.string.setup_title), style = MaterialTheme.typography.titleMedium)
                    Text(
                        stringResource(R.string.setup_body),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
            if (!receiverOk) {
                Spacer(Modifier.height(10.dp))
                Text(
                    stringResource(R.string.admin_component_missing),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(Modifier.height(16.dp))
            GradientButton(text = stringResource(R.string.btn_activate), icon = Icons.Outlined.Shield, onClick = onActivate)
            Spacer(Modifier.height(12.dp))
            Text(
                stringResource(R.string.setup_fallback_title),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
            )
            SetupAction(Icons.AutoMirrored.Outlined.OpenInNew, R.string.btn_open_admin_settings, onOpenList)
            SetupAction(Icons.Outlined.AppSettingsAlt, R.string.btn_allow_restricted, onAppInfo)
            SetupAction(Icons.Outlined.ContentCopy, R.string.adb_copy_button, onCopyAdb)
        }
    }
}

@Composable
private fun SetupAction(icon: ImageVector, @StringRes label: Int, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Text(stringResource(label), modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelLarge)
    }
}

// ------------------------------------------------------------------ grouped list

private val GROUP_GAP = 3.dp

@Composable
private fun SectionLabel(@StringRes res: Int) {
    Text(
        stringResource(res),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 12.dp, top = 22.dp, bottom = 8.dp)
    )
}

/**
 * Android-16-style grouped list: items stacked with hairline gaps; the group is
 * clipped to large outer corners while inner corners stay small.
 */
@Composable
private fun SettingsGroup(content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp)),
        verticalArrangement = Arrangement.spacedBy(GROUP_GAP),
        content = content
    )
}

@Composable
private fun GroupTile(content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(16.dp),
        content = content
    )
}

@Composable
private fun PrefItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    trailing: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    below: (@Composable ColumnScope.() -> Unit)? = null
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(icon, MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                if (subtitle != null) {
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (trailing != null) {
                Spacer(Modifier.width(8.dp))
                trailing()
            }
        }
        if (below != null) {
            Spacer(Modifier.height(14.dp))
            below()
        }
    }
}

@Composable
private fun IconBadge(
    icon: ImageVector,
    container: Color,
    tint: Color = MaterialTheme.colorScheme.onSecondaryContainer
) {
    Box(
        Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(container),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun ChevronAction() {
    Icon(
        Icons.AutoMirrored.Outlined.OpenInNew,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.size(20.dp)
    )
}

@Composable
private fun ThemedSwitch(checked: Boolean, onChecked: (Boolean) -> Unit) {
    Switch(
        checked = checked,
        onCheckedChange = onChecked,
        thumbContent = if (checked) {
            { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(SwitchDefaults.IconSize)) }
        } else null
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> Segmented(
    options: List<Pair<T, String>>,
    selected: T,
    icons: List<ImageVector>? = null,
    onSelected: (T) -> Unit
) {
    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, (value, label) ->
            SegmentedButton(
                selected = value == selected,
                onClick = { onSelected(value) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                icon = {
                    SegmentedButtonDefaults.Icon(active = value == selected) {
                        val ic = icons?.getOrNull(index)
                        if (ic != null) Icon(ic, contentDescription = null, modifier = Modifier.size(SegmentedButtonDefaults.IconSize))
                    }
                }
            ) {
                Text(label, maxLines = 1, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun ColorSwatches(options: List<Pair<Int, Int>>, selected: Int, onSelected: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        options.forEach { (argb, nameRes) ->
            val isSel = argb == selected
            val ring by animateColorAsState(
                if (isSel) MaterialTheme.colorScheme.primary else Color.Transparent,
                label = "swatchRing"
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier
                        .size(48.dp)
                        .border(2.5.dp, ring, CircleShape)
                        .padding(5.dp)
                        .clip(CircleShape)
                        .background(Color(argb))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                        .clickable { onSelected(argb) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSel) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = null,
                            tint = if (argb == ICON_WHITE) Color(0xFF1D1A24) else Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(stringResource(nameRes), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// ------------------------------------------------------------------ theme picker

@Composable
private fun ThemePicker(selected: Int, onSelect: (Int) -> Unit) {
    val context = LocalContext.current
    val palettes = remember { availablePalettes(context) }
    Row(
        Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        palettes.forEach { p -> ThemeSwatch(p, p.id == selected) { onSelect(p.id) } }
    }
}

@Composable
private fun ThemeSwatch(palette: AppPalette, selected: Boolean, onClick: () -> Unit) {
    val scale by animateFloatAsState(if (selected) 1f else 0.92f, label = "swatchScale")
    val ring by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        label = "themeRing"
    )
    val preview = if (palette.alwaysDark || QuickLockTheme.extras.isDark) palette.dark else palette.light
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(76.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp)
    ) {
        Box(
            Modifier
                .scale(scale)
                .size(68.dp)
                .border(3.dp, ring, RoundedCornerShape(24.dp))
                .padding(6.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(preview.surfaceContainerHigh)
        ) {
            // Mini "app" preview: gradient hero + two list rows in the palette's colors.
            Column(Modifier.padding(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(20.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(Brush.linearGradient(listOf(palette.gradientStart, palette.gradientEnd)))
                )
                Box(Modifier.fillMaxWidth(0.8f).height(5.dp).clip(CircleShape).background(preview.primary))
                Box(Modifier.fillMaxWidth(0.55f).height(5.dp).clip(CircleShape).background(preview.onSurfaceVariant.copy(alpha = 0.5f)))
            }
            if (selected) {
                Box(
                    Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(preview.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null, tint = preview.onPrimary, modifier = Modifier.size(12.dp))
                }
            } else if (palette.alwaysDark || palette.id == Prefs.PALETTE_DYNAMIC) {
                Icon(
                    if (palette.alwaysDark) Icons.Outlined.DarkMode else Icons.Outlined.AutoAwesome,
                    contentDescription = null,
                    tint = preview.onSurfaceVariant,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                        .size(14.dp)
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            stringResource(palette.nameRes),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}

// ------------------------------------------------------------------ widget preview

/**
 * Live, interactive preview of the home-screen widget on a mock wallpaper. Tapping
 * it runs the same gesture logic as the real widget (without locking), so the user
 * can feel the chosen double-tap speed.
 */
@Composable
private fun WidgetPreview(
    showIcon: Boolean,
    iconColor: Color,
    bgStyle: Int,
    showLabel: Boolean,
    gestureMode: Int,
    tapWindowMs: Long
) {
    val extras = QuickLockTheme.extras
    var lastTap by remember { mutableLongStateOf(TapGestureLogic.NO_TAP) }
    var armed by remember { mutableStateOf(false) }
    var lockedFlash by remember { mutableIntStateOf(0) }

    LaunchedEffect(armed, lastTap) {
        if (armed) {
            delay(tapWindowMs)
            armed = false
        }
    }
    LaunchedEffect(lockedFlash) {
        if (lockedFlash > 0) {
            delay(1100)
            lockedFlash = 0
        }
    }

    val zoneColor by animateColorAsState(
        when {
            armed -> Violet.copy(alpha = 0.30f)
            !showIcon -> Color.Transparent
            bgStyle == Prefs.BG_SOLID -> Color.Black.copy(alpha = 0.87f)
            bgStyle == Prefs.BG_TRANSLUCENT -> Color.Black.copy(alpha = 0.40f)
            else -> Color.Transparent
        },
        tween(120),
        label = "zone"
    )

    Text(
        stringResource(R.string.preview_title),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface
    )
    Text(
        stringResource(R.string.preview_hint),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(Modifier.height(12.dp))
    Box(
        Modifier
            .fillMaxWidth()
            .height(170.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        extras.gradientStart.copy(alpha = 0.85f),
                        extras.gradientEnd.copy(alpha = 0.55f),
                        Color(0xFF1B1830)
                    )
                )
            )
    ) {
        // Mock app icons on the "home screen".
        Row(
            Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            repeat(4) {
                Box(
                    Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.28f))
                )
            }
        }
        // The lock zone itself.
        Box(
            Modifier
                .align(Alignment.Center)
                .padding(bottom = 30.dp)
                .width(170.dp)
                .height(72.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(zoneColor)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    val now = System.currentTimeMillis()
                    val lock = gestureMode == Prefs.MODE_SINGLE_TAP ||
                        TapGestureLogic.isDoubleTap(lastTap, now, tapWindowMs)
                    if (lock) {
                        lastTap = TapGestureLogic.NO_TAP
                        armed = false
                        lockedFlash++
                    } else {
                        lastTap = now
                        armed = true
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            if (!showIcon && !armed && lockedFlash == 0) {
                // Dashed outline is only drawn in the preview — on the real home
                // screen the zone is completely invisible.
                Canvas(Modifier.matchParentSize()) {
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.7f),
                        cornerRadius = CornerRadius(16.dp.toPx()),
                        style = Stroke(
                            width = 1.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))
                        )
                    )
                }
                Text(
                    stringResource(R.string.preview_invisible),
                    color = Color.White.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.labelMedium
                )
            }
            AnimatedContent(
                targetState = lockedFlash > 0,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "lockedFlash"
            ) { locked ->
                if (locked) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.preview_locked), color = Color.White, style = MaterialTheme.typography.labelLarge)
                    }
                } else if (showIcon) {
                    val tint = if (armed) Color.White else iconColor
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Lock, contentDescription = null, tint = tint, modifier = Modifier.size(26.dp))
                        if (showLabel) {
                            Spacer(Modifier.width(8.dp))
                            Text(
                                stringResource(if (armed) R.string.widget_label_again else R.string.widget_label),
                                color = tint,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                } else if (armed) {
                    Text(stringResource(R.string.preview_armed), color = Color.White, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

// ------------------------------------------------------------------ footer & help

@Composable
private fun PrivacyFooter() {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(top = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FooterPill(Icons.Outlined.WifiOff, R.string.footer_no_internet)
            FooterPill(Icons.Outlined.VisibilityOff, R.string.footer_no_a11y)
            FooterPill(Icons.Outlined.Eco, R.string.footer_battery)
        }
        Spacer(Modifier.height(12.dp))
        Text(
            stringResource(R.string.footer_version, BuildConfig.VERSION_NAME),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun FooterPill(icon: ImageVector, @StringRes label: Int) {
    Row(
        Modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(5.dp))
        Text(stringResource(label), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HelpSheet(topic: HelpTopic, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val body = when (topic) {
        HelpTopic.HOW_TO -> stringResource(R.string.how_to_text)
        HelpTopic.ADMIN -> stringResource(R.string.help_admin_body)
        HelpTopic.PRIVACY -> stringResource(R.string.privacy_text)
        HelpTopic.ABOUT -> stringResource(R.string.about_text, BuildConfig.VERSION_NAME)
    }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(topic.icon, MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
                Spacer(Modifier.width(14.dp))
                Text(stringResource(topic.title), style = MaterialTheme.typography.headlineSmall)
            }
            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(16.dp))
            Text(
                body,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ------------------------------------------------------------------ system helpers

/** Self-check: is our DeviceAdminReceiver registered with its meta-data AND the required intent-filter? */
private fun checkAdminReceiverRegistered(context: Context): Boolean {
    return try {
        val component = ComponentName(context, LockDeviceAdminReceiver::class.java)
        val info = context.packageManager.getReceiverInfo(component, PackageManager.GET_META_DATA)
        val hasMeta = info.metaData?.containsKey(DeviceAdminMetaKey) == true
        val resolves = context.packageManager.queryBroadcastReceivers(
            Intent(DeviceAdminEnabledAction).setPackage(context.packageName), 0
        ).any { it.activityInfo?.name == component.className }
        hasMeta && resolves
    } catch (_: Exception) {
        false
    }
}

private const val DeviceAdminMetaKey = DeviceAdminReceiver.DEVICE_ADMIN_META_DATA // "android.app.device_admin"
private const val DeviceAdminEnabledAction = DeviceAdminReceiver.ACTION_DEVICE_ADMIN_ENABLED

/**
 * Opens Settings → Security & privacy. (A direct deep link to the "Device admin apps"
 * list is not reliable: on Android 17 it silently lands on the Settings home page.)
 */
private fun openDeviceAdminList(context: Context, snackbar: SnackbarHostState, scope: CoroutineScope) {
    try {
        context.startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        scope.launch { snackbar.showSnackbar(context.getString(R.string.hint_admin_fallback)) }
    } catch (_: ActivityNotFoundException) {
        // ignore
    }
}

/** App info page, where "Allow restricted settings" lives (⋮ menu) for sideloaded apps. */
private fun openAppInfo(context: Context) {
    try {
        context.startActivity(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + context.packageName))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    } catch (_: ActivityNotFoundException) {
        // ignore
    }
}

private fun requestPinWidget(context: Context, snackbar: SnackbarHostState, scope: CoroutineScope) {
    val manager = AppWidgetManager.getInstance(context)
    val ok = manager.isRequestPinAppWidgetSupported &&
        manager.requestPinAppWidget(ComponentName(context, LockWidgetProvider::class.java), null, null)
    if (!ok) scope.launch { snackbar.showSnackbar(context.getString(R.string.trigger_widget_manual)) }
}

private fun requestAddTile(context: Context, snackbar: SnackbarHostState, scope: CoroutineScope) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        scope.launch { snackbar.showSnackbar(context.getString(R.string.trigger_tile_manual)) }
        return
    }
    val sbm = context.getSystemService(StatusBarManager::class.java) ?: return
    sbm.requestAddTileService(
        ComponentName(context, LockTileService::class.java),
        context.getString(R.string.tile_label),
        PlatformIcon.createWithResource(context, R.drawable.ic_lock_widget),
        context.mainExecutor
    ) { result ->
        val msg = when (result) {
            StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ADDED -> R.string.tile_added
            StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ALREADY_ADDED -> R.string.tile_already
            StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_NOT_ADDED -> null
            else -> R.string.trigger_tile_manual
        }
        if (msg != null) scope.launch { snackbar.showSnackbar(context.getString(msg)) }
    }
}
