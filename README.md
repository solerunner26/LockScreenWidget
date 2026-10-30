# Quick Lock — private double-tap screen-lock widget for Android

A tiny, fully open-source Android app that puts a **resizable lock zone on your home
screen**: **double-tap it and the phone locks instantly**. Built for people who are
worried about apps that ask for the **accessibility service** (the permission that
lets an app *read everything on your screen* — OTPs, banking apps, messages).

> **This app never uses accessibility.** It cannot read your screen, your
> notifications, your OTPs or anything else. It has **no internet permission** and
> physically cannot send data anywhere. It uses **device admin with the
> "force-lock" policy only** — the single capability needed to lock the screen.

Works on Android 8.0+ (API 26+), including the latest Android beta releases
(e.g. Android 17 beta on a Pixel 10 Pro). On newer Android versions where
"Device Protection" blocks accessibility-based apps, this app keeps working because
it does not rely on accessibility at all.

---

## Features

- 🏠 **Home-screen widget lock zone** — add it once (or several times for multiple
  zones), **drag it anywhere** and **resize it freely** (long-press → resize handles).
  The whole area is the tap target.
- 👆 **Double-tap to lock** (configurable: single tap, or 300/450/600 ms double-tap
  speed). The widget briefly lights up after the first tap ("armed — tap again") so
  you always know it's listening.
- ⚡ **Quick Settings tile** — an optional "Lock screen" tile in the notification
  shade.
- 📌 **App shortcut** — long-press the app icon → "Lock screen".
- 🎨 **Customizable look** — icon color (white/dark/blue), background
  (translucent / invisible / solid), optional "Lock" label.
- 🔔 Short vibration + toast when the screen locks (can be disabled).
- 🔒 **Private by design** — see [Security model](#security-model).
- 🪶 **Zero battery impact** — see [Battery](#battery).

## How it locks the screen (and why it's safe)

| Approach | Can the app read your screen / OTPs? | Used by this app? |
|---|---|---|
| Accessibility service (used by e.g. "Screen Off Widget" `com.jordigordillo.dtswidget`) | **Yes — full screen reading** | ❌ No |
| Root / `goToSleep` | No, but requires root | ❌ No |
| **Device admin → `DevicePolicyManager.lockNow()` (force-lock policy only)** | **No — device admin cannot read the screen** | ✅ **Yes** |

The app requests device admin with **only** `<force-lock />`
(`app/src/main/res/xml/device_admin_policies.xml`). That grants exactly one power:
locking the screen. No wipe, no password changes, no camera/mic, no screen reading.
You can revoke it at any time under **Settings → Security → Device admin apps**
(or from the app's own settings screen) — one tap, no confirmation needed.

After locking, your normal unlock works as usual (fingerprint/face/PIN on Pixel).

## Security model

- **No accessibility service** — the app has no way to read the screen, OTPs or
  notifications. There is no `AccessibilityService` anywhere in the codebase.
- **No internet permission** — no network stack is even imported; the app cannot
  exfiltrate anything. Verified by `tools/validate_project.py`.
- **Only permission requested: `VIBRATE`** (install-time, no prompt) — used solely
  for the lock-confirmation buzz.
- **Device admin: force-lock only** — the entire policy is one line in
  `res/xml/device_admin_policies.xml`.
- **`FLAG_SECURE` on every app screen** — the settings UI and the lock activity can
  **never appear in screenshots or screen recordings**. An eavesdropper recording
  your screen gets nothing from this app's UI.
- **Explicit, minimal exported surface** — every component declares
  `android:exported`; the device-admin receiver and QS tile are bound only by the
  system (`BIND_DEVICE_ADMIN` / `BIND_QUICK_SETTINGS_TILE`); the widget's custom
  tap broadcast is accepted **only from this app's own immutable PendingIntent or the
  system** (caller-UID check in `LockWidgetProvider`), so no other app can forge a
  lock; the shortcut activity verifies the caller is this app or a real home
  launcher before locking.
- **All PendingIntents are `FLAG_IMMUTABLE`** and explicitly targeted.
- **No data collection** — settings are stored in a `MODE_PRIVATE` prefs file that is
  excluded from backup, cloud backup and device transfer (`allowBackup="false"` +
  `res/xml/backup_rules.xml` + `res/xml/data_extraction_rules.xml`).
- **No logging** of any kind; no analytics, no ads, no SDKs. The main source set has
  **zero runtime dependencies** — the whole app is 8 small Kotlin files you can audit
  in minutes.

## Battery

The widget provider runs **only** when you tap the widget or the system sends a
widget event — `updatePeriodMillis="0"` means the system never wakes the app on a
timer. There are **no services running in the background, no alarms, no boot
receivers, no wake locks**. Battery impact: **zero**.

## Build

Requirements: JDK 17, Android SDK with platform **android-36** and build-tools
(easiest: Android Studio Hedgehog+ → open this folder → it will fetch the SDK).

```bash
./gradlew :app:assembleRelease          # APK in app/build/outputs/apk/release/
./gradlew :app:testDebugUnitTest         # JVM unit tests (tap-gesture logic)
./gradlew :app:connectedDebugAndroidTest # instrumented tests (needs a device/emulator)
python3 tools/validate_project.py        # static security/consistency checks (no SDK needed)
python3 tools/logic_spec_test.py         # gesture-logic spec test (no SDK needed)
```

Install on your Pixel 10 Pro:

```bash
adb install app/build/outputs/apk/release/app-release.apk
```

(or open the APK from the phone / use Android Studio's Run button).

## Setup & usage

1. Open **Quick Lock** → tap **"Activate device admin"** → confirm the system dialog.
   (This is a one-time, offline system prompt. Read the explanation — it grants
   force-lock only.)
2. Long-press your home screen → **Widgets** → add **"Quick Lock"**.
3. **Drag the widget anywhere** you want quick access and **resize it** to the
   area you want (the whole area becomes the lock zone). Add it multiple times for
   multiple zones.
4. **Double-tap the zone** → the phone locks instantly (short buzz + "Screen
   locked" toast).
5. Optional: add the **"Lock screen" Quick Settings tile**, or long-press the app
   icon → **"Lock screen"** shortcut.
6. Tune everything in the app: gesture, double-tap speed, widget look, vibration.
   There's also a **"Lock screen now (test)"** button.

## On-device test checklist

- [ ] Widget appears in the widget picker; can be placed and freely resized/moved.
- [ ] First tap → widget shows the blue "armed" state; second tap within the
      window → screen locks, toast + vibration shown.
- [ ] Two slow taps (> window) → no lock.
- [ ] Single-tap mode → every tap locks.
- [ ] With device admin revoked → tapping the widget opens the app's setup screen
      instead of locking (and nothing crashes).
- [ ] Quick Settings tile and app-icon shortcut lock the screen.
- [ ] `adb shell screencap` / any screen recorder → the app's settings screen is
      black/protected (FLAG_SECURE).
- [ ] `adb shell dumpsys battery` / wakelock stats → this app holds no wakelocks and
      has no background activity.
- [ ] `adb shell pm list permissions com.solerunner26.lockscreenwidget` → only
      `android.permission.VIBRATE`.
- [ ] After locking, fingerprint/face unlock still works on the Pixel.

## Project layout

```
app/src/main/AndroidManifest.xml                 permissions & components (audited)
app/src/main/java/com/solerunner26/lockscreenwidget/
    QuickLockApp.kt                              minimal Application (nothing to init)
    Prefs.kt                                     private local settings
    TapGestureLogic.kt                           pure double-tap logic (unit-tested)
    LockController.kt                            the ONLY privileged call: lockNow()
    LockWidgetProvider.kt                        widget + double-tap handling + UID guard
    LockDeviceAdminReceiver.kt                   device admin (force-lock only)
    SettingsActivity.kt                          setup & settings (FLAG_SECURE)
    LockNowActivity.kt                           shortcut target (FLAG_SECURE, caller-checked)
    LockTileService.kt                           Quick Settings tile
app/src/main/res/                                layouts, widget info, admin policy, ...
app/src/test/.../TapGestureLogicTest.kt          JVM unit tests
app/src/androidTest/.../SmokeTest.kt             instrumented tests
tools/validate_project.py                        300+ static security checks (runs anywhere)
tools/logic_spec_test.py                         gesture-logic property test (runs anywhere)
```

## References

- Play Store app used as feature reference: "Screen Off Widget"
  (`com.jordigordillo.dtswidget`) — it locks via double-tap on an invisible resizable
  widget plus a QS tile, but it requires the **accessibility service**. Quick Lock
  offers the same UX with **device admin force-lock instead**, so it never gains
  screen-reading ability. (The GitHub repo `alextnetto/res` was checked too — it is a
  web-SaaS reverse-engineering skill kit, unrelated to Android lock widgets.)

## License

MIT — audit it, build it, use it with confidence.
