# Quick Lock — private double-tap screen-lock widget for Android

> **Developing further?** See **[HANDOVER.md](HANDOVER.md)** — full architecture map, build/test/CI instructions, on-device ADB cheat sheet, and a ready-to-paste prompt for Codex/Claude Code.

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

- 🏠 **Invisible home-screen lock zone** — add the widget anywhere and resize it
  freely; by default it is **fully transparent with no icon** (the whole area is the
  tap target). Optionally show a lock icon/label with color & background choices.
  Add it multiple times for multiple zones.
- 👆 **Double-tap to lock** (configurable: single tap, or 300/450/600 ms double-tap
  speed). In invisible mode the zone flashes very subtly after the first tap
  ("armed — tap again").
- ⚡ **Quick Settings tile** and 📌 **app-icon shortcut** as extra triggers.
- 🎨 **Material 3 UI (Jetpack Compose, Material 3 1.4)** — gradient hero card with live
  admin status and a one-tap "Lock screen now", Android-16-style grouped settings,
  segmented buttons, an **interactive live widget preview** (feel your double-tap
  speed), one-tap "Add widget" / "Add Quick Settings tile", animated theme switching.
- 🌈 **Six themes** — Aurora (violet→magenta, default), Ocean, Sunset, Forest,
  **Midnight** (true-black OLED night theme, always dark) and Wallpaper (Material You
  colors, Android 12+). Each non-night theme supports System / Light / Dark.
  All help text (How to use / Device admin troubleshooting / Privacy & security /
  About) lives in the **Help** menu (top-right "?").
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
  exfiltrate anything. Android enforces this in the kernel: a process without
  the INTERNET permission cannot even open a network socket, so even code an
  attacker managed to run inside the app could not send data out. The manifest
  also strips network permissions with `tools:node="remove"` so no library can
  add them later. Verified by `tools/validate_project.py`, and CI checks the
  built APK.
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
  system** (a random per-install secret token inside the immutable PendingIntent,
  verified in `LockWidgetProvider`), so no other app can forge a
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

## Download & automatic updates

Every push to the working branch builds a signed APK and publishes it as a new
GitHub Release `v2.1.<run>` (always marked **Latest**). Every build is signed with
the same permanent key, so a new build installs **over** the old one — no uninstall,
settings and device admin are kept.

- **Latest release:** https://github.com/solerunner26/LockScreenWidget/releases/latest

### Get update notifications (recommended): Obtainium

Quick Lock has no internet permission on purpose, so it cannot check for updates
itself. [Obtainium](https://github.com/ImranR98/Obtainium) (free, open source)
does it instead:

1. Install Obtainium on the phone.
2. Obtainium → **Add app** → paste `https://github.com/solerunner26/LockScreenWidget` → **Add** → **Install**.
3. Allow Obtainium to "install unknown apps" when asked.

Obtainium then checks the repo in the background, notifies you when a new build is
out, and **Update** downloads the ~2 MB APK and installs it in one tap. Installing
through Obtainium also makes it the app's installer, so Android 12+ can apply later
updates with fewer prompts. The in-app Help menu has the same steps ("Getting updates").

> **One-time switch:** builds before 2.1 were signed with a throwaway key per CI run.
> To move to the permanent key, deactivate device admin, uninstall the old Quick
> Lock once, then install via Obtainium. After that, never again.

## Build from source

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

1. **Uninstall any old Quick Lock build first** — `adb uninstall com.solerunner26.lockscreenwidget` —
   then install the new APK fresh (`adb install app-release.apk`). Updating across builds fails with
   "App not installed" because each CI build is signed with a different key.
2. Open **Quick Lock** → tap **"Activate device admin"** → confirm the system dialog.
   If the dialog does not appear, or the app is not listed under
   **Settings → Security & privacy → More security & privacy → Device admin**, use:
   - the setup card's **App info → Allow restricted settings** button (sideloaded APKs), or
   - the reliable ADB bypass (phone connected to PC, USB debugging on):
     `adb shell dpm set-active-admin --user current com.solerunner26.lockscreenwidget/com.solerunner26.lockscreenwidget.LockDeviceAdminReceiver`
     then verify with `adb shell dumpsys device_policy | grep -A2 "Enabled Device Admins"` (the app's status turns green).
   - On Android 17 beta with **Device Protection (Advanced Protection) ON**, sideloaded apps can be
     blocked: temporarily turn it off (Settings → Security & privacy → Advanced Protection),
     install + activate, then re-enable it. Full steps also in the app: **Help → "Device admin not listed?"**.
3. Long-press your home screen → **Widgets** → add **"Quick Lock"**.
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
    LockWidgetProvider.kt                        widget + double-tap handling + secret-token guard
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
