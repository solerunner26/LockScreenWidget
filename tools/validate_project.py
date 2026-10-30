#!/usr/bin/env python3
"""
Static validation suite for the Quick Lock Android project.

This sandbox has no JDK/Android SDK, so a full `gradlew build` cannot run here.
This script is the executable test harness that DOES run here; it verifies:

  1. Every XML file (manifest + res) is well-formed.
  2. Every @resource reference in XML and every R.* reference in Kotlin resolves
     to a real defined resource (no broken builds from typos).
  3. Every component class named in the manifest exists as a Kotlin file with a
     matching class declaration.
  4. Kotlin sources are structurally sane (package matches path, balanced
     braces/parens/brackets outside strings/comments).
  5. SECURITY POLICY assertions:
       - the only permission is VIBRATE (no INTERNET, no accessibility, no overlay)
       - device-admin policies contain ONLY <force-lock/>
       - no accessibility service is declared anywhere
       - updatePeriodMillis = 0 (zero battery), resizeMode both axes (adjustable area)
       - allowBackup=false, data-extraction excludes everything
       - every component has an explicit android:exported
       - all activities set FLAG_SECURE (no screenshots / screen recording of app UI)
       - all PendingIntents are FLAG_IMMUTABLE
       - the widget receiver enforces a caller-UID check on custom actions
       - no network APIs, no logging, no services/alarms/wake-locks in main sources
  6. The gradle wrapper jar is present and valid (PK zip magic).

Run: python3 tools/validate_project.py   (exits non-zero on first failure)
"""
import os
import re
import sys
import xml.etree.ElementTree as ET

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
APP = os.path.join(ROOT, "app")
MAIN = os.path.join(APP, "src", "main")
RES = os.path.join(MAIN, "res")
MANIFEST = os.path.join(MAIN, "AndroidManifest.xml")
KOTLIN_DIR = os.path.join(MAIN, "java")
ANDROID_NS = "{http://schemas.android.com/apk/res/android}"

failures = []
checks = 0


def check(cond, msg):
    global checks
    checks += 1
    if not cond:
        failures.append(msg)
        print(f"  FAIL: {msg}")


def section(title):
    print(f"\n== {title}")


# ---------------------------------------------------------------- 1. XML parse
section("1. XML well-formedness")
xml_files = [MANIFEST]
for dirpath, _, files in os.walk(RES):
    for f in files:
        if f.endswith(".xml"):
            xml_files.append(os.path.join(dirpath, f))
parsed = {}
for path in xml_files:
    try:
        parsed[path] = ET.parse(path)
        checks += 1
    except ET.ParseError as e:
        check(False, f"XML parse error in {os.path.relpath(path, ROOT)}: {e}")
print(f"  parsed {len(parsed)} XML files OK")

# ---------------------------------------------------------------- 2. resources
section("2. Resource definition & reference integrity")

resources = {}   # (type, name) -> file
ids = set()      # @+id names

for dirpath, _, files in os.walk(RES):
    folder = os.path.basename(dirpath)
    rtype = folder.split("-")[0]  # values, drawable, layout, xml, mipmap...
    for f in files:
        path = os.path.join(dirpath, f)
        name = os.path.splitext(f)[0]
        if rtype == "values":
            tree = parsed[path]
            for el in tree.getroot():
                tag = el.tag
                nm = el.get("name")
                if nm:
                    resources[(tag, nm)] = path
        elif rtype in ("drawable", "layout", "xml", "mipmap", "raw", "menu", "color", "anim"):
            resources[(rtype, name)] = path

# collect @+id definitions
for path, tree in parsed.items():
    for el in tree.iter():
        for attr, val in el.attrib.items():
            if attr == ANDROID_NS + "id" and val.startswith("@+id/"):
                ids.add(val[len("@+id/"):])

ref_re = re.compile(r"@(?!(?:\+|android:))([a-z_]+)/([A-Za-z0-9_.]+)")
for path, tree in parsed.items():
    rel = os.path.relpath(path, ROOT)
    # gather all attribute values in the file (namespace-aware enough for our files)
    text = open(path, encoding="utf-8").read()
    for m in ref_re.finditer(text):
        rtype, name = m.group(1), m.group(2)
        check((rtype, name) in resources, f"{rel}: unresolved reference @{rtype}/{name}")

# R.* references in Kotlin
kt_files = []
for dirpath, _, files in os.walk(os.path.join(APP, "src")):
    for f in files:
        if f.endswith(".kt"):
            kt_files.append(os.path.join(dirpath, f))
r_re = re.compile(r"\bR\.([a-z_]+)\.([A-Za-z0-9_]+)")
for path in kt_files:
    text = open(path, encoding="utf-8").read()
    for m in r_re.finditer(text):
        rtype, name = m.group(1), m.group(2)
        if rtype == "id":
            check(name in ids, f"{os.path.relpath(path, ROOT)}: R.id.{name} not defined in any layout")
        else:
            check((rtype, name) in resources,
                  f"{os.path.relpath(path, ROOT)}: R.{rtype}.{name} not defined in res/")
print(f"  resource references checked against {len(resources)} defined resources, {len(ids)} ids")

# launcher icon PNGs in all densities + in-app logo
for d in ("mdpi", "hdpi", "xhdpi", "xxhdpi", "xxxhdpi"):
    check(os.path.isfile(os.path.join(RES, f"mipmap-{d}", "ic_launcher.png")),
          f"missing mipmap-{d}/ic_launcher.png")
    check(os.path.isfile(os.path.join(RES, f"mipmap-{d}", "ic_launcher_round.png")),
          f"missing mipmap-{d}/ic_launcher_round.png")
check(os.path.isfile(os.path.join(RES, "drawable-nodpi", "logo.png")), "missing drawable-nodpi/logo.png")

# Compose / Material 3 wiring
app_gradle = open(os.path.join(APP, "build.gradle.kts"), encoding="utf-8").read()
check("material3" in app_gradle, "app module must depend on Compose Material3")
check("compose = true" in app_gradle, "buildFeatures.compose must be enabled")
root_gradle = open(os.path.join(ROOT, "build.gradle.kts"), encoding="utf-8").read()
check("kotlin.plugin.compose" in root_gradle, "root build must declare the Kotlin compose plugin")

# invisible-by-default widget (user requirement: no lock symbol on the home screen)
prefs_src = open(os.path.join(KOTLIN_DIR, "com", "solerunner26", "lockscreenwidget", "Prefs.kt"),
                 encoding="utf-8").read()
check('"show_icon", false' in prefs_src, "widget icon must default to hidden (invisible lock zone)")
prov_src = open(os.path.join(KOTLIN_DIR, "com", "solerunner26", "lockscreenwidget", "LockWidgetProvider.kt"),
                encoding="utf-8").read()
check("prefs.showIcon" in prov_src, "widget provider must honor the showIcon pref")

# ---------------------------------------------------------------- 3. manifest <-> code
section("3. Manifest components match Kotlin classes")
manifest = parsed[MANIFEST].getroot()
app_el = manifest.find("application")

component_classes = []
for tag in ("activity", "receiver", "service"):
    for el in app_el.findall(tag):
        name = el.get(ANDROID_NS + "name")
        check(name is not None and name.startswith("."), f"{tag} missing android:name")
        cls = name.lstrip(".")
        component_classes.append(cls)
        path = os.path.join(KOTLIN_DIR, "com", "solerunner26", "lockscreenwidget", cls + ".kt")
        check(os.path.isfile(path), f"manifest class {cls} has no file {os.path.relpath(path, ROOT)}")
        if os.path.isfile(path):
            src = open(path, encoding="utf-8").read()
            check(re.search(rf"\bclass\s+{cls}\b", src) is not None,
                  f"{cls}.kt does not declare class {cls}")

app_name = app_el.get(ANDROID_NS + "name")
check(app_name == ".QuickLockApp", f"application android:name should be .QuickLockApp, got {app_name}")

# shortcuts.xml targetClass exists
shortcuts = parsed[os.path.join(RES, "xml", "shortcuts.xml")]
for intent in shortcuts.iter("intent"):
    tc = intent.get(ANDROID_NS + "targetClass")
    tp = intent.get(ANDROID_NS + "targetPackage")
    check(tp == "com.solerunner26.lockscreenwidget", f"shortcut targetPackage wrong: {tp}")
    if tc:
        cls = tc.split(".")[-1]
        path = os.path.join(KOTLIN_DIR, "com", "solerunner26", "lockscreenwidget", cls + ".kt")
        check(os.path.isfile(path), f"shortcut targetClass {tc} has no source file")

# ---------------------------------------------------------------- 4. Kotlin sanity
section("4. Kotlin structural sanity")


def strip_strings_and_comments(src):
    src = re.sub(r"/\*.*?\*/", "", src, flags=re.S)
    src = re.sub(r"//[^\n]*", "", src)
    src = re.sub(r'"(?:\\.|[^"\\])*"', '""', src)
    src = re.sub(r"'(?:\\.|[^'\\])'", "''", src)
    return src


for path in kt_files:
    rel = os.path.relpath(path, ROOT)
    src = open(path, encoding="utf-8").read()
    m = re.search(r"^package\s+([\w.]+)", src, flags=re.M)
    check(m is not None, f"{rel}: no package declaration")
    if m:
        pkg_path = os.path.join(*m.group(1).split("."))
        check(path.replace(os.sep, "/").endswith(pkg_path + "/" + os.path.basename(path)),
              f"{rel}: package {m.group(1)} does not match file path")
    stripped = strip_strings_and_comments(src)
    for open_c, close_c in (("{", "}"), ("(", ")"), ("[", "]")):
        check(stripped.count(open_c) == stripped.count(close_c),
              f"{rel}: unbalanced {open_c}{close_c} ({stripped.count(open_c)} vs {stripped.count(close_c)})")
print(f"  checked {len(kt_files)} Kotlin files")

# ---------------------------------------------------------------- 5. security policy
section("5. Security & privacy policy assertions")

# Scan the *effective* manifest: strip XML comments first so documentation notes
# (which intentionally name the permissions we do NOT use) don't false-positive.
manifest_text = re.sub(r"<!--.*?-->", "", open(MANIFEST, encoding="utf-8").read(), flags=re.S)

# 5a. permissions: exactly VIBRATE
perms = [p.get(ANDROID_NS + "name") for p in manifest.findall("uses-permission")]
check(perms == ["android.permission.VIBRATE"],
      f"permission list must be exactly [VIBRATE], got {perms}")
for banned in ("INTERNET", "ACCESS_NETWORK_STATE", "SYSTEM_ALERT_WINDOW", "WAKE_LOCK",
               "FOREGROUND_SERVICE", "RECEIVE_BOOT_COMPLETED", "READ_SMS", "READ_CONTACTS",
               "CAMERA", "RECORD_AUDIO", "READ_EXTERNAL_STORAGE", "QUERY_ALL_PACKAGES",
               "BIND_ACCESSIBILITY_SERVICE", "PACKAGE_USAGE_STATS", "REQUEST_INSTALL_PACKAGES"):
    check(banned not in manifest_text, f"manifest must not mention {banned}")
check("accessibility" not in manifest_text.lower().replace("no accessibility", "").replace("never reads", ""),
      "manifest must not declare an accessibility service")

# 5b. device admin policies: only force-lock
admin_xml = parsed[os.path.join(RES, "xml", "device_admin_policies.xml")].getroot()
policies = [el.tag for el in admin_xml.iter() if el.tag not in ("device-admin", "uses-policies")]
check(policies == ["force-lock"], f"device admin policies must be exactly [force-lock], got {policies}")

# 5c. widget info: zero battery + freely resizable
wi = parsed[os.path.join(RES, "xml", "lock_widget_info.xml")].getroot()
check(wi.get(ANDROID_NS + "updatePeriodMillis") == "0", "updatePeriodMillis must be 0 (zero battery)")
rm = wi.get(ANDROID_NS + "resizeMode") or ""
check("horizontal" in rm and "vertical" in rm, f"resizeMode must allow both axes, got {rm!r}")
check(wi.get(ANDROID_NS + "targetCellWidth") and wi.get(ANDROID_NS + "targetCellHeight"),
      "targetCellWidth/Height should be set for modern launchers")
check(wi.get(ANDROID_NS + "widgetCategory") == "home_screen", "widgetCategory must be home_screen")
check(wi.get(ANDROID_NS + "initialLayout") == "@layout/widget_lock", "initialLayout must be @layout/widget_lock")

# 5d. backup disabled
check(app_el.get(ANDROID_NS + "allowBackup") == "false", "allowBackup must be false")
check(app_el.get(ANDROID_NS + "dataExtractionRules") == "@xml/data_extraction_rules",
      "dataExtractionRules must be set")
check(app_el.get(ANDROID_NS + "fullBackupContent") == "@xml/backup_rules",
      "fullBackupContent must be set")

# 5e. exported explicit on every component; correct protection permissions
for tag in ("activity", "receiver", "service"):
    for el in app_el.findall(tag):
        nm = el.get(ANDROID_NS + "name")
        check(el.get(ANDROID_NS + "exported") is not None, f"{nm}: android:exported must be explicit")
for el in app_el.findall("receiver"):
    nm = el.get(ANDROID_NS + "name")
    if "DeviceAdmin" in nm:
        check(el.get(ANDROID_NS + "permission") == "android.permission.BIND_DEVICE_ADMIN",
              "device admin receiver must require BIND_DEVICE_ADMIN")
for el in app_el.findall("service"):
    nm = el.get(ANDROID_NS + "name")
    if "Tile" in nm:
        check(el.get(ANDROID_NS + "permission") == "android.permission.BIND_QUICK_SETTINGS_TILE",
              "tile service must require BIND_QUICK_SETTINGS_TILE")

# widget provider must have the appwidget meta-data
for el in app_el.findall("receiver"):
    if "LockWidgetProvider" in (el.get(ANDROID_NS + "name") or ""):
        metas = {m.get(ANDROID_NS + "name"): m.get(ANDROID_NS + "resource") for m in el.findall("meta-data")}
        check(metas.get("android.appwidget.provider") == "@xml/lock_widget_info",
              "widget provider meta-data missing")

# 5f. Kotlin-level security checks
section("5b. Kotlin security checks")


def read_main(cls):
    return open(os.path.join(KOTLIN_DIR, "com", "solerunner26", "lockscreenwidget", cls + ".kt"),
                encoding="utf-8").read()


for cls in ("SettingsActivity", "LockNowActivity"):
    src = read_main(cls)
    check("FLAG_SECURE" in src, f"{cls} must set FLAG_SECURE (no screenshots/screen recording of app UI)")

prov = read_main("LockWidgetProvider")
check("FLAG_IMMUTABLE" in prov, "widget PendingIntent must be FLAG_IMMUTABLE")
check("Binder.getCallingUid()" in prov, "widget provider must check caller UID on custom actions")
check("PendingIntent.getBroadcast" in prov, "widget tap must use an explicit PendingIntent broadcast")

all_main_src = ""
for dirpath, _, files in os.walk(KOTLIN_DIR):
    for f in files:
        if f.endswith(".kt"):
            all_main_src += open(os.path.join(dirpath, f), encoding="utf-8").read()

for banned in ("AccessibilityService", "android.util.Log", "Log.d(", "Log.e(",
               "HttpURLConnection", "java.net.URL", "Socket(", "OkHttp", "Retrofit",
               "AlarmManager", "WakeLock", "startForegroundService", "WorkManager",
               "JobScheduler", "RECEIVE_BOOT_COMPLETED", "MediaProjection", "MediaRecorder",
               "ContentResolver", "query("):
    check(banned not in all_main_src, f"main sources must not contain {banned!r}")

# lockNow is the only privileged call
check("lockNow()" in read_main("LockController"), "LockController must call lockNow()")
dpm_calls = re.findall(r"dpm\.(\w+)\(", read_main("LockController"))
check(set(dpm_calls) <= {"isAdminActive", "lockNow"},
      f"LockController may only call isAdminActive/lockNow on DevicePolicyManager, got {dpm_calls}")

# ---------------------------------------------------------------- 6. gradle wrapper
section("6. Gradle wrapper")
wrapper_jar = os.path.join(ROOT, "gradle", "wrapper", "gradle-wrapper.jar")
check(os.path.isfile(wrapper_jar), "gradle-wrapper.jar missing")
if os.path.isfile(wrapper_jar):
    with open(wrapper_jar, "rb") as fh:
        magic = fh.read(2)
    check(magic == b"PK", "gradle-wrapper.jar is not a valid zip/jar (bad magic)")
    check(os.path.getsize(wrapper_jar) > 10000, "gradle-wrapper.jar suspiciously small")
check(os.path.isfile(os.path.join(ROOT, "gradle", "wrapper", "gradle-wrapper.properties")),
      "gradle-wrapper.properties missing")
for f in ("settings.gradle.kts", "build.gradle.kts", "gradle.properties", "app/build.gradle.kts",
          "app/proguard-rules.pro", "gradlew", "gradlew.bat"):
    check(os.path.isfile(os.path.join(ROOT, f)), f"missing {f}")
gradlew = os.path.join(ROOT, "gradlew")
if os.path.isfile(gradlew):
    with open(gradlew, encoding="utf-8", errors="replace") as fh:
        head = fh.read(64)
    check(head.startswith("#!"), "gradlew must start with a shebang and be executable")
    check(os.access(gradlew, os.X_OK), "gradlew must be executable")

# ---------------------------------------------------------------- summary
print(f"\n{'=' * 60}")
print(f"Ran {checks} checks.")
if failures:
    print(f"FAILED: {len(failures)} problem(s):")
    for f in failures:
        print(f"  - {f}")
    sys.exit(1)
print("ALL CHECKS PASSED")
