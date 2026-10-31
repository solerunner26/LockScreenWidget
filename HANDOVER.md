# Quick Lock — Agent Handover (Codex / Claude Code)

This document is the complete handover for continuing development of **Quick Lock**.
Everything below is verified against the current branch. A copy-paste prompt for another
agent is in the last section.

- **Repo:** https://github.com/solerunner26/LockScreenWidget
- **Working branch:** `arena/8ef1f1ea-lockscreenwidget` (push only to this branch)
- **Release:** https://github.com/solerunner26/LockScreenWidget/releases/tag/v1.0.0
  - Direct APK: `https://github.com/solerunner26/LockScreenWidget/releases/download/v1.0.0/app-release.apk`
  - Debug APK: `https://github.com/solerunner26/LockScreenWidget/releases/download/v1.0.0/app-debug.apk`
- **Package:** `com.solerunner26.lockscreenwidget` · Admin receiver: `com.solerunner26.lockscreenwidget.LockDeviceAdminReceiver`
- **Stack:** Kotlin 2.0.21, AGP 8.9.0, Gradle wrapper 8.11.1, compileSdk/targetSdk 36, minSdk 26, Jetpack Compose + Material 3 (BOM 2024.12.01), activity-compose 1.9.3, lifecycle-runtime-compose 2.8.7.

---

## 1. What the app is

A privacy-first **double-tap screen-lock home-screen widget** for Android (built for a
Google Pixel 10 Pro on Android 17 beta):

- User adds a freely positionable/resizable home-screen widget; **double-tap inside it locks the phone instantly**.
- Locking is done **only** via `DevicePolicyManager.lockNow()` with device-admin **force-lock** policy.
- **No accessibility service, ever** (Android 17 strips it from non-accessibility apps anyway).
- UI: Compose Material 3, violet→magenta gradient two-tone theme, dark mode (System/Light/Dark), uniform gradient buttons, **no help text on the main screen** (Help menu top-right: How to use / Device-admin troubleshooting / Privacy & security / About), user logo as app icon.
- Widget is **invisible by default** (fully transparent; brief violet "armed" flash on first tap of a double-tap; optional icon/label in settings).

## 2. Non-negotiable constraints (do not break — enforced by CI + validator)

1. **Permissions: only `android.permission.VIBRATE`.** No INTERNET, no accessibility, no SYSTEM_ALERT_WINDOW, no WAKE_LOCK / FOREGROUND_SERVICE / RECEIVE_BOOT_COMPLETED. CI hard-fails the build on any other permission (`aapt2 dump permissions` allowlist).
2. **Device admin policies: only `<force-lock />`** in `res/xml/device_admin_policies.xml`.
3. **Never read the screen**: no accessibility service, no content URIs of other apps, no notifications access.
4. **Screen-recording/screenshot resistant**: all activities keep `FLAG_SECURE`.
5. **Zero battery**: `updatePeriodMillis="0"`, no services, alarms, receivers that wake, no wake locks. Verify on device with `dumpsys power | grep -i wakelock` (should list none from our package).
6. **Minimal attack surface**: every component has explicit `android:exported`; all PendingIntents are `FLAG_IMMUTABLE`; the widget's custom tap action checks the caller UID (own app or SYSTEM only); `allowBackup=false` + full data-extraction excludes.
7. **Resource shrinking stays OFF** in release (`isShrinkResources = false`) — the shrinker can drop manifest-meta-data-only resources such as `device_admin_policies.xml`. R8/minify stays on.
8. Widget meta: `resizeMode="horizontal|vertical"`, min/max resize bounds, `widgetCategory="home_screen"`.
9. Keep `tools/validate_project.py` (317 checks) and `tools/logic_spec_test.py` green after every change.

## 3. Architecture map

| File | Purpose |
|---|---|
| `app/src/main/AndroidManifest.xml` | Only VIBRATE permission; application `allowBackup=false`; `SettingsActivity` (FLAG_SECURE via theme/window), `LockNowActivity` (translucent, FLAG_SECURE), `LockWidgetProvider` receiver (custom action + uid check), `LockDeviceAdminReceiver` (label+description, meta-data `android.app.admin.device_admin` → `@xml/device_admin_policies`), `LockTileService` (QS tile, requires BIND_QUICK_SETTINGS_TILE... declared properly), static shortcuts. |
| `LockDeviceAdminReceiver.kt` | Empty `DeviceAdminReceiver` subclass; capability comes from the XML policy. |
| `LockController.kt` | The single privileged call site: `isAdminActive()` + `tryLock()` → `dpm.lockNow()`, haptic buzz, result enum `LOCKED / ADMIN_MISSING / ERROR`. |
| `LockWidgetProvider.kt` | `AppWidgetProvider`: renders the widget per prefs (invisible default; armed flash), detects double-tap from two taps within `tapWindowMs`, then `LockController.tryLock()`; PendingIntent with `FLAG_IMMUTABLE`, own-uid/SYSTEM_UID caller guard on the custom action. |
| `TapGestureLogic.kt` | Pure, unit-tested double-tap logic (`isDoubleTap(lastTapAt, now, windowMs)`; `NO_TAP`). Keep it pure — it is property-tested. |
| `Prefs.kt` | SharedPreferences wrapper: `gestureMode` (single/double), `tapWindowMs`, `backgroundStyle` (transparent default), `showIcon` (default false), `showLabel`, `themeMode` (system/light/dark), `haptics`, `iconColor`, `lastTapAt`, `lastLockAt`. |
| `SettingsActivity.kt` / `ui/SettingsScreen.kt` | Compose M3 settings UI: status card (admin self-check + Activate + "Open Device admin apps in Settings" + "Copy ADB activate command" fallbacks), gesture mode, widget look, appearance/theme, test lock, Help dialog. `FLAG_SECURE` + `enableEdgeToEdge()` in the activity. |
| `ui/theme/Color.kt`, `ui/theme/Theme.kt`, `ui/components/GradientButton.kt` | Violet `#6D4AC8` → magenta `#C94FA0` gradient theme, dark bg `#14121C`, dark mode, uniform gradient buttons. |
| `LockNowActivity.kt` | Translucent activity launched from the QS tile / shortcut; locks immediately, finishes. |
| `LockTileService.kt` | Quick-settings tile that locks on tap (same admin path). |
| `res/xml/lock_widget_info.xml` | Widget provider info (see constraints: updatePeriodMillis=0, resizeMode, bounds). |
| `res/xml/device_admin_policies.xml` | `<uses-policies><force-lock /></uses-policies>` — the complete capability list. |
| `res/xml/shortcuts.xml`, `res/layout/widget_lock.xml`, drawables, mipmaps | Static shortcut "Lock screen", widget layout, widget backgrounds, launcher icons (user logo at all densities + `drawable-nodpi/logo.png`). |
| `app/src/test/.../TapGestureLogicTest.kt`, `app/src/androidTest/.../SmokeTest.kt` | JVM unit tests + instrumented smoke test. |
| `tools/validate_project.py` | 317 static checks enforcing the whole constraint list above. |
| `tools/logic_spec_test.py` | Property test of the tap-gesture spec incl. 20k randomized sequences. |
| `.github/workflows/build-apk.yml` | CI: JDK17 + SDK 36 → unit tests → `assembleDebug` + `assembleRelease` (release signed with a throwaway keystore generated in CI, never committed) → per-APK sanity checks (apksigner verify, `aapt2 dump permissions` allowlist = VIBRATE only, manifest xmltree contains receiver + admin meta-data, `aapt2 dump resources` contains `device_admin_policies`) → upload artifact `quicklock-apks` → best-effort `gh release create v1.0.0 --clobber` uploading both APKs. Compile/test failures are surfaced as `::error::` annotations. |

## 4. Build, test, release

Local (Android Studio / any machine with JDK 17 + Android SDK):

```bash
git clone https://github.com/solerunner26/LockScreenWidget.git
cd LockScreenWidget && git checkout arena/8ef1f1ea-lockscreenwidget
./gradlew test                 # JVM unit tests
./gradlew assembleDebug        # app-debug.apk
./gradlew assembleRelease      # app-release.apk (needs signing; use debug locally or configure signing)
python3 tools/validate_project.py   # must print ALL CHECKS PASSED
python3 tools/logic_spec_test.py    # must print ALL PASSED
```

CI (the only build path in some sandboxes; takes ~8–10 min):

- Trigger: any `git push` to `arena/8ef1f1ea-lockscreenwidget`.
- Watch: `gh run list -L 5`, `gh run view <id>`, annotations via
  `gh api repos/solerunner26/LockScreenWidget/check-runs/<jobId>/annotations`.
- Result: green run ⇒ both APKs re-uploaded to release **v1.0.0** (same URLs as above).
- **Every CI build uses a fresh signing key → on the phone always `adb uninstall com.solerunner26.lockscreenwidget` before installing a new APK**, otherwise "App not installed".

## 5. On-device testing & debugging cheat sheet (Pixel 10 Pro / Android 17 beta)

```bash
# install / clean reinstall (signature changes between CI builds!)
adb uninstall com.solerunner26.lockscreenwidget
adb install app-release.apk

# device admin — the reliable bypass if the Settings list/dialog fails:
adb shell dpm set-active-admin --user current com.solerunner26.lockscreenwidget/com.solerunner26.lockscreenwidget.LockDeviceAdminReceiver
adb shell dpm list-active-admins                       # verify Quick Lock is listed
adb shell pm dump com.solerunner26.lockscreenwidget | grep -i -A2 admin   # receiver registration

# logs
adb logcat -s LockWidgetProvider LockController SettingsActivity ActivityManager:I | grep -i quicklock

# widget lock path end-to-end: double-tap the widget area on the home screen

# FLAG_SECURE check: try screenshot / screen record -> must be black/blocked
# battery check: adb shell dumpsys power | grep -i wakelock   (no Quick Lock entries)
#            adb shell dumpsys batterystats | grep -i lockscreenwidget
```

Settings UI path for admin list on Pixel: **Settings → Security & privacy → More security settings → Device admin apps**.
If the app is missing there on the Android 17 beta: check **Device Protection (Advanced Protection)** —
Settings → Security & privacy → Advanced Protection. If ON, it can block sideloaded apps from
admin activation: turn it OFF, install + activate, then turn it back ON. (Documented in-app under Help → "Device admin not listed?".)

## 6. Current state & open issues

- CI green at HEAD (see `gh run list -L 1`); release v1.0.0 assets are current.
- **Open issue (user's device):** on the Pixel 10 Pro / Android 17 beta, the app did not appear in
  "Device admin apps" and the Activate button seemed to do nothing with earlier builds. The current APK is
  CI-verified to contain receiver + meta-data + policies resource, and the app ships ADB fallback + diagnostics.
  Next diagnostics on-device (see §5 commands). If `pm dump ... | grep admin` shows the receiver but Settings
  still hides it, it is almost certainly Device Protection / OS-level policy on the beta — document it.
- Known environment quirk: CI uses `--stop` between debug/release builds and 6g heaps (Compose release builds crashed the Kotlin daemon otherwise).

## 7. Ideas for further development (pick with the user)

- Triple-tap / long-press gesture modes; per-widget-instance settings.
- Lock on widget single-tap with confirmation dialog.
- "Lock + timeout" (re-lock N seconds after unlock — needs admin, still zero-battery only if done via receiver events; careful with battery constraint).
- Wear OS / Android Auto companion trigger.
- Play Store distribution (would need a real signing key + developer verification; solves the sideload/Device Protection friction on Android 17).
- More languages (strings.xml is the single source).

---

## 8. Copy-paste prompt for Codex / Claude Code

Paste everything inside the fence below into a fresh Codex or Claude Code session with this repo checked out:

```
You are continuing development of "Quick Lock", a privacy-first double-tap screen-lock
home-screen widget for Android. Repo: https://github.com/solerunner26/LockScreenWidget,
branch: arena/8ef1f1ea-lockscreenwidget (push ONLY to this branch).

READ FIRST: HANDOVER.md (in repo root) — it has the full architecture map, build/test
commands, CI behavior and an on-device ADB cheat sheet. Also read README.md.

HARD CONSTRAINTS (CI + tools/validate_project.py enforce these; never violate):
1. The ONLY permission is android.permission.VIBRATE. No INTERNET, no accessibility
   service (never read the screen/OTPs/notifications), no SYSTEM_ALERT_WINDOW, no
   WAKE_LOCK/FOREGROUND_SERVICE/RECEIVE_BOOT_COMPLETED.
2. Device admin policies XML must contain ONLY <force-lock />; locking happens ONLY via
   DevicePolicyManager.lockNow() from LockController.
3. All activities keep FLAG_SECURE (screenshot/screen-record resistant).
4. Widget: updatePeriodMillis=0, resizeMode horizontal|vertical, freely positionable;
   invisible/transparent by default.
5. Every component: explicit android:exported; PendingIntents FLAG_IMMUTABLE; the
   widget's custom broadcast checks caller UID (own app or SYSTEM). allowBackup=false.
6. Release build: isMinifyEnabled=true but isShrinkResources=false (the shrinker drops
   device_admin_policies.xml). UI: Jetpack Compose + Material 3, violet #6D4AC8 ->
   magenta #C94FA0 gradient, dark mode, uniform gradient buttons, Help menu instead of
   on-screen help text.

BUILD/TEST: ./gradlew test; ./gradlew assembleDebug; python3 tools/validate_project.py
(must print ALL CHECKS PASSED); python3 tools/logic_spec_test.py (ALL PASSED).
CI: pushing to the branch builds both APKs and re-uploads them to release v1.0.0
(direct: .../releases/download/v1.0.0/app-release.apk). Each CI build has a NEW signing
key, so on a phone always: adb uninstall com.solerunner26.lockscreenwidget && adb install.
On-device admin activation fallback (Pixel/Android 17 beta):
adb shell dpm set-active-admin --user current com.solerunner26.lockscreenwidget/com.solerunner26.lockscreenwidget.LockDeviceAdminReceiver
verify: adb shell dpm list-active-admins

CURRENT OPEN ISSUE: on the user's Pixel 10 Pro (Android 17 beta) the app previously did not
show in Settings -> Security & privacy -> More security settings -> Device admin apps.
Current APK is CI-verified to contain the admin receiver + meta-data + policies resource.
If you work on this: run the §5 diagnostics on-device, consider Device Protection
(Advanced Protection) blocking sideloaded apps, and keep the in-app fallbacks working.

TASK: <describe the next feature/fix here>
Definition of done: validator + logic tests green locally, CI green after push, README and
HANDOVER.md updated, release APK re-uploaded automatically by CI.
```
