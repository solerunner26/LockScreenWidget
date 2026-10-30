plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

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
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
        }
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
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    // The main source set intentionally has NO runtime dependencies:
    // no AndroidX, no network libs, no analytics — nothing that could phone home.
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test:core:1.6.1")
    androidTestImplementation("androidx.test:runner:1.6.2")
}
