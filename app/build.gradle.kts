plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

// Bump the major/minor here for big releases; CI appends the run number.
val BASE_VERSION = "2.2"
val VERSION_CODE_OFFSET = 100 // keeps CI codes above every earlier release (max 3)

android {
    namespace = "com.solerunner26.lockscreenwidget"
    // compileSdk 36 (Android 16). The app is forward-compatible and runs unchanged
    // on newer releases (Android 17 beta included); bump compileSdk/targetSdk when
    // the newer SDK ships if you want to use newer APIs.
    compileSdk = 36

    defaultConfig {
        applicationId = "com.solerunner26.lockscreenwidget"
        minSdk = 26          // Android 8.0 — covers every modern Pixel
        targetSdk = 36
        // Every CI run is a new, installable update: versionCode/versionName come
        // from the GitHub Actions run number (e.g. run 26 -> 2.1.26 / code 126), and
        // the release is tagged v<versionName> so Obtainium detects it.
        // Local builds keep a low code and a "-local" suffix.
        val ciRun = System.getenv("GITHUB_RUN_NUMBER")?.toIntOrNull()
        versionCode = if (ciRun != null) VERSION_CODE_OFFSET + ciRun else 3
        versionName = if (ciRun != null) "$BASE_VERSION.$ciRun" else "$BASE_VERSION.0-local"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // CI signing: when these env vars are present, the release APK is signed. CI
    // restores the PERMANENT release key from repository secrets, so every build has
    // the same signature and installs as an update. Locally, without the env vars,
    // the release build stays unsigned. No keystore or password is ever committed.
    signingConfigs {
        create("ci") {
            val ksPath = System.getenv("KEYSTORE_PATH")
            if (ksPath != null) {
                storeFile = file(ksPath)
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            // Resource shrinking DISABLED on purpose: AGP's shrinker can drop
            // resources that are only referenced from manifest meta-data (e.g. the
            // device-admin policies XML), which breaks device-admin activation.
            // The app is tiny, so shrinking buys nothing.
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (System.getenv("KEYSTORE_PATH") != null) {
                signingConfig = signingConfigs.getByName("ci")
            }
        }
        debug {
            isMinifyEnabled = false
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true // version name shown in the footer / About sheet
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    lint {
        // These components are exported BY DESIGN and guarded in code:
        //  - LockWidgetProvider: the widget host must be able to reach it; its custom
        //    actions are accepted only from this app's own immutable PendingIntent
        //    or the system (caller-UID check in onReceive).
        //  - LockNowActivity: shortcut target; verifies the caller is this app or a
        //    real home launcher before locking.
        //  - LockTileService / LockDeviceAdminReceiver: bound only by the system
        //    (BIND_QUICK_SETTINGS_TILE / BIND_DEVICE_ADMIN).
        //  - SettingsActivity: the normal launcher entry point.
        // Disable the corresponding lint checks so lintVitalRelease doesn't fail.
        disable += setOf("ExportedActivity", "ExportedReceiver", "ExportedService")
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    // UI only: Jetpack Compose + Material 3. Pure UI libraries — no network code,
    // no analytics, nothing that could phone home.
    val composeBom = platform("androidx.compose:compose-bom:2026.06.01")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.10.0")
    debugImplementation("androidx.compose.ui:ui-tooling")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test:core:1.6.1")
    androidTestImplementation("androidx.test:runner:1.6.2")
}
