# Quick Lock — Agent Handover (Codex / Claude Code)

This document is the complete handover for continuing development of **Quick Lock**.
Everything below is verified against the current branch. A copy-paste prompt for another
agent is in the last section.

- **Repo:** https://github.com/solerunner26/LockScreenWidget
- **Working branch:** `arena/8ef1f1ea-lockscreenwidget` (push only to this branch)
- **Releases:** https://github.com/solerunner26/LockScreenWidget/releases/latest — one release per CI run,
  tag `v2.1.<run>`, single asset `QuickLock-2.1.<run>.apk`. The user updates via Obtainium (in-app Help →
  "Getting updates"). The old `v1.0.0` release (throwaway-key builds) is obsolete.
- **Package:** `com.solerunner26.lockscreenwidget` · Admin receiver: `com.solerunner26.lockscreenwidget.LockDeviceAdminReceiver`
- **Stack (v2.0.0):** Kotlin 2.2.21, AGP 8.13.2, Gradle wrapper 8.13, compileSdk/targetSdk 36, minSdk 26, Jetpack Compose BOM 2026.06.01 (Material 3 1.4.0), activity-compose 1.13.0, lifecycle-runtime-compose 2.10.0. (BOM 2026.08+ needs compileSdk 37 + AGP 9.1 — a separate migration.)

---

## 1. What the app is

A privacy-first **double-tap screen-lock home-screen widget** for Android (built for a
Google Pixel 10 Pro on Android 17 beta):

- User adds a freely positionable/resizable home-screen widget; **double-tap inside it locks the phone instantly**.
- Locking: device-admin `DevicePolicyManager.lockNow()` (force-lock policy), or — optional, user-enabled —
  the **blind** `LockAccessibilityService` (`GLOBAL_ACTION_LOCK_SCREEN`), which keeps fingerprint unlock.
  `lockNow()` always forces the PIN (strongAuthRequired=0x2, "locked by work policy"); the a11y lock does not.
- UI: Compose Material 3, violet→magenta gradient two-tone theme, dark mode (System/Light/Dark), uniform gradient buttons, **no help text on the main screen** (Help menu top-right: How to use / Device-admin troubleshooting / Privacy & security / About), user logo as app icon.
- Widget is **invisible by default** (fully transparent; brief violet "armed" flash on first tap of a double-tap; optional icon/label in settings).

## 2. Non-negotiable constraints (do not break — enforced by CI + validator)

1. **Permissions: only `android.permission.VIBRATE`.** No INTERNET, no SYSTEM_ALERT_WINDOW, no WAKE_LOCK / FOREGROUND_SERVICE / RECEIVE_BOOT_COMPLETED. CI hard-fails the build on any other permission (`aapt2 dump permissions` allowlist).
2. **Device admin policies: only `<force-lock />`** in `res/xml/device_admin_policies.xml`.
3. **Never read the screen**: the ONLY accessibility code is the optional blind `LockAccessibilityService`
   (`canRetrieveWindowContent=false`, no `accessibilityEventTypes`, `packageNames` = own package, flagDefault,
   no gesture/key/screenshot capabilities, code uses only `GLOBAL_ACTION_LOCK_SCREEN`; validator enforces all of
   it). No content URIs of other apps, no notifications access.
4. **Screen-recording/screenshot resistant**: all activities keep `FLAG_SECURE`.
5. **Zero battery**: `updatePeriodMillis="0"`, no services, alarms, receivers that wake, no wake locks. Verify on device with `dumpsys power | grep -i wakelock` (should list none from our package).
6. **Minimal attack surface**: every component has explicit `android:exported`; all PendingIntents are `FLAG_IMMUTABLE`; the widget's custom tap action carries a per-install secret token (`Prefs.tapToken`) that `LockWidgetProvider` verifies — note `Binder.getCallingUid()` inside `onReceive` always returns our own UID, so a UID check there protects nothing; `allowBackup=false` + full data-extraction excludes.
7. **Resource shrinking stays OFF** in release (`isShrinkResources = false`) — the shrinker can drop manifest-meta-data-only resources such as `device_admin_policies.xml`. R8/minify stays on.
8. Widget meta: `resizeMode="horizontal|vertical"`, min/max resize bounds, `widgetCategory="home_screen"`.
9. Keep `tools/validate_project.py` (317 checks) and `tools/logic_spec_test.py` green after every change.

## 3. Architecture map

| File | Purpose |
|---|---|
| `app/src/main/AndroidManifest.xml` | Only VIBRATE permission; application `allowBackup=false`; `SettingsActivity` (FLAG_SECURE via theme/window), `LockNowActivity` (translucent, FLAG_SECURE), `LockWidgetProvider` receiver (custom action + uid check), `LockDeviceAdminReceiver` (label+description, meta-data `android.app.device_admin` → `@xml/device_admin_policies`), `LockTileService` (QS tile, requires BIND_QUICK_SETTINGS_TILE... declared properly), static shortcuts. |
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
| `.github/workflows/build-apk.yml` | CI: JDK17 + SDK 36 → unit tests → `assembleDebug` + `assembleRelease` (release signed with the PERMANENT key restored from secrets `RELEASE_KEYSTORE_B64` / `RELEASE_KEYSTORE_PASSWORD` / `RELEASE_KEY_ALIAS`; without them a throwaway key is used and no release is published) → per-APK sanity checks (apksigner verify, `aapt2 dump permissions` allowlist = VIBRATE only, manifest xmltree contains receiver + admin meta-data, `aapt2 dump resources` contains `device_admin_policies`) → upload artifact `quicklock-apks` → `gh release create v<versionName> --latest` with the release APK only. Compile/test failures are surfaced as `::error::` annotations. |

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
- Result: green run ⇒ new release `v2.1.<run>` (versionCode `100 + run`, from `GITHUB_RUN_NUMBER` in
  `app/build.gradle.kts`; local builds are `2.1.0-local`, code 3). Bump `BASE_VERSION` for big releases.
- Signing key: permanent, stored only in repo secrets + the user's offline backup (outside the repo).
  **Never rotate it** — a new key forces every user to uninstall. Updates install in place.

## 5. On-device testing & debugging cheat sheet (Pixel 10 Pro / Android 17 beta)

```bash
# install (updates install in place since 2.1 — same permanent key)
adb uninstall com.solerunner26.lockscreenwidget
adb install app-release.apk

# device admin — the reliable bypass if the Settings list/dialog fails:
adb shell dpm set-active-admin --user current com.solerunner26.lockscreenwidget/com.solerunner26.lockscreenwidget.LockDeviceAdminReceiver
adb shell dumpsys device_policy | grep -A2 "Enabled Device Admins"   # verify (dpm list-active-admins no longer exists on Android 17)
adb shell pm dump com.solerunner26.lockscreenwidget | grep -i -A2 admin   # receiver registration

# logs
adb logcat -s LockWidgetProvider LockController SettingsActivity ActivityManager:I | grep -i quicklock

# widget lock path end-to-end: double-tap the widget area on the home screen

# FLAG_SECURE check: try screenshot / screen record -> must be black/blocked
# battery check: adb shell dumpsys power | grep -i wakelock   (no Quick Lock entries)
#            adb shell dumpsys batterystats | grep -i lockscreenwidget
```

Settings UI path for admin list on Pixel: **Settings → Security & privacy → More security & privacy → Device admin**.
If the app is missing there on the Android 17 beta: check **Device Protection (Advanced Protection)** —
Settings → Security & privacy → Advanced Protection. If ON, it can block sideloaded apps from
admin activation: turn it OFF, install + activate, then turn it back ON. (Documented in-app under Help → "Device admin not listed?".)

## 6. Current state & open issues

**v2.2 (verified on the API 37 emulator with a PIN set):**
- *"Locked by work policy" + no fingerprint*: platform behaviour of `lockNow()` (strongAuthRequired=0x2).
  New optional blind `LockAccessibilityService` locks via `GLOBAL_ACTION_LOCK_SCREEN` → "Going to sleep due to
  accessibility", strongAuthRequired=0x0, keyguard shown, `dumpsys accessibility` = capabilities=0, eventTypes=
  (none). LockController prefers it when enabled, else device admin. UI: "Lock method" card.
- *"Add lock zone" looked broken*: the pin dialog worked but the new zone was invisible. Now the button turns on
  a dashed "Show zone outline" mode (with a snackbar on how to resize); turning it off = 100% transparent.
  Picker preview uses `widget_preview.xml`.
- *Transparency*: invisible mode removes the background entirely (resource 0); initial layout is transparent;
  optional "Tap-again flash" toggle.
- Version chip `v<versionName>` in the hero card.

**v2.1 — updates without uninstalling.** The real reason every update needed an uninstall: CI generated
a new throwaway signing key on every run. CI now signs with one permanent key (repo secrets), derives
versionCode/versionName from the run number, and publishes a versioned `--latest` release per push. Update
notifications + one-tap install come from Obtainium; the app stays offline (user chose this over an in-app
updater that would need INTERNET + REQUEST_INSTALL_PACKAGES). In-app Help has a "Getting updates" topic.

**RESOLVED (v2.0.1, verified on the API 37 emulator): double tap on the widget never locked.**
Root cause: the v2.0.0 "disarm" redraw held the first tap's broadcast open with `goAsync()` for
`tapWindow + 60 ms`. A manifest receiver gets its broadcasts one at a time, so the second tap was queued
until the window had already closed and always counted as a new first tap (single-tap mode was fine).
Fix: plain main-thread `postDelayed` redraw (no `goAsync`), tap timestamps on `SystemClock.elapsedRealtime()`,
tap PendingIntent uses `FLAG_RECEIVER_FOREGROUND` (new request code 1003). The validator now rejects `goAsync(`
in the provider. Emulator: one tap and two slow taps do not lock; 5/5 double taps lock.

Also in v2.0.1: the manifest strips INTERNET / network-state / Wi-Fi / Bluetooth-connect / nearby-Wi-Fi
permissions with `tools:node="remove"`, so no library can merge them in later (enforced by the validator;
CI already rejects any built APK requesting them).

**RESOLVED (v2.0.0, verified on an Android 17 / API 37 emulator): app missing from "Device admin apps".**
Root cause: the manifest meta-data key was `android.app.admin.device_admin`; the platform key is
`android.app.device_admin` (`DeviceAdminReceiver.DEVICE_ADMIN_META_DATA`). Settings logged
`DeviceAdminListPrefCtrl: Skipping ... XmlPullParserException: No android.app.device_admin meta-data`,
and the old validator + CI were checking for the *wrong* key, so every build "passed". Also added the
required `DEVICE_ADMIN_ENABLED` intent-filter. Validator and CI now enforce the correct key, reject the
wrong one, and require the intent-filter. Verified on the emulator: app listed → toggle → system dialog
("Lock the screen" only) → activated → `lockNow()` turned the screen off.

Other v2.0.0 fixes:
- Widget "armed" flash never cleared → one-shot `Handler.postDelayed(tapWindow + 60 ms)` redraw
  (v2.0.0 wrapped this in `goAsync()`, which broke double tap — see v2.0.1 above).
- Widget/tile taps without admin now open setup through activity PendingIntents
  (`startActivityAndCollapse(PendingIntent)` on API 34+); background activity starts were blocked.
- Caller-UID guard replaced with a secret token (see constraint 6). Custom actions no longer have
  intent-filters; `ACTION_REFRESH` removed — widgets refresh in-process via `LockWidgetProvider.updateAll()`.
- The direct `Settings$DeviceAdminSettingsActivity` deep link lands on the Settings home on Android 17, so
  the app opens `ACTION_SECURITY_SETTINGS` and shows the path instead.
- Sideloaded APKs on Android 13+ may hit "Restricted settings": App info → ⋮ → Allow restricted settings
  (button on the setup card; documented under Help).
- androidx injects a signature-level `<pkg>.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION` into the merged
  manifest. It grants nothing to anyone else; the CI denylist allows it.

Local build without CI: JDK 21 (`~/.jdks/jbr-21.0.11`) + `./gradlew assembleRelease`, then zipalign +
apksigner with the debug keystore gives a stable signature across local builds (no uninstall needed).

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
1. The ONLY permission is android.permission.VIBRATE. No INTERNET, no SYSTEM_ALERT_WINDOW,
   no WAKE_LOCK/FOREGROUND_SERVICE/RECEIVE_BOOT_COMPLETED. Never read the screen: the only
   accessibility code is the optional BLIND LockAccessibilityService (lock-screen action only).
2. Device admin policies XML must contain ONLY <force-lock />; locking happens ONLY via
   DevicePolicyManager.lockNow() from LockController.
3. All activities keep FLAG_SECURE (screenshot/screen-record resistant).
4. Widget: updatePeriodMillis=0, resizeMode horizontal|vertical, freely positionable;
   invisible/transparent by default.
5. Every component: explicit android:exported; PendingIntents FLAG_IMMUTABLE; the
   widget's custom broadcast verifies a per-install secret token. allowBackup=false.
6. Release build: isMinifyEnabled=true but isShrinkResources=false (the shrinker drops
   device_admin_policies.xml). UI: Jetpack Compose + Material 3, violet #6D4AC8 ->
   magenta #C94FA0 gradient, dark mode, uniform gradient buttons, Help menu instead of
   on-screen help text.

BUILD/TEST: ./gradlew test; ./gradlew assembleDebug; python3 tools/validate_project.py
(must print ALL CHECKS PASSED); python3 tools/logic_spec_test.py (ALL PASSED).
CI: pushing to the branch publishes release v2.1.<run>, signed with the permanent key from
repo secrets, so it installs as an update (the user gets it through Obtainium). Never change
the signing key and never add INTERNET for an in-app updater (privacy guarantee).
On-device admin activation fallback (Pixel/Android 17 beta):
adb shell dpm set-active-admin --user current com.solerunner26.lockscreenwidget/com.solerunner26.lockscreenwidget.LockDeviceAdminReceiver
verify: adb shell dumpsys device_policy | grep -A2 "Enabled Device Admins"

CURRENT OPEN ISSUE: on the user's Pixel 10 Pro (Android 17 beta) the app previously did not
show in Settings -> Security & privacy -> More security & privacy -> Device admin.
Current APK is CI-verified to contain the admin receiver + meta-data + policies resource.
If you work on this: run the §5 diagnostics on-device, consider Device Protection
(Advanced Protection) blocking sideloaded apps, and keep the in-app fallbacks working.

TASK: <describe the next feature/fix here>
Definition of done: validator + logic tests green locally, CI green after push, README and
HANDOVER.md updated, release APK re-uploaded automatically by CI.
```
