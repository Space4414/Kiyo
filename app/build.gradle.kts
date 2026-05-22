plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.space4414.kiyo"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.space4414.kiyo"
        minSdk = 21
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }

        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        }
    }

    // ─── Distribution tracks ───────────────────────────────────────────────────
    // Each flavor gets a distinct applicationId so all three can be installed
    // on the same device simultaneously.
    flavorDimensions += "track"

    productFlavors {
        // Installed as: com.space4414.kiyo.alpha  |  Label: "Kiyo Alpha"
        create("alpha") {
            dimension = "track"
            applicationId = "com.space4414.kiyo.alpha"
            versionNameSuffix = "-alpha"
            resValue("string", "app_name", "Kiyo Alpha")
        }
        // Installed as: com.space4414.kiyo.beta   |  Label: "Kiyo Beta"
        create("beta") {
            dimension = "track"
            applicationId = "com.space4414.kiyo.beta"
            versionNameSuffix = "-beta"
            resValue("string", "app_name", "Kiyo Beta")
        }
        // Installed as: com.space4414.kiyo        |  Label: "Kiyo"
        create("stable") {
            dimension = "track"
            // applicationId intentionally not overridden — inherits
            // "com.space4414.kiyo" from defaultConfig
            resValue("string", "app_name", "Kiyo")
        }
    }

    // ─── ABI splits ────────────────────────────────────────────────────────────
    // Produces 5 APKs per variant:
    //   arm64-v8a (64-bit ARM), armeabi-v7a (32-bit ARM),
    //   x86_64, x86, and a fat universal APK.
    splits {
        abi {
            isEnable = true
            reset()
            include("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
            isUniversalApk = true
        }
    }

    // ─── Build types ───────────────────────────────────────────────────────────
    buildTypes {
        debug {
            isDebuggable = true
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Sign release builds with the debug keystore so the stable APK
            // is side-loadable without a Play Store keystore.
            // Replace with a proper signingConfig before Play Store submission.
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }

    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions { kotlinCompilerExtensionVersion = "1.5.11" }

    packaging {
        resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" }
    }
}

// ─── Per-ABI version codes ─────────────────────────────────────────────────────
// Ensures 64-bit builds always have a higher version code than 32-bit builds,
// so Android can upgrade from 32-bit to 64-bit without uninstalling.
//
//   universal    → versionCode * 10 + 0
//   armeabi-v7a  → versionCode * 10 + 1   (32-bit ARM)
//   arm64-v8a    → versionCode * 10 + 2   (64-bit ARM — preferred)
//   x86          → versionCode * 10 + 3
//   x86_64       → versionCode * 10 + 4
androidComponents {
    onVariants { variant ->
        variant.outputs.forEach { output ->
            val abiFilter = output.filters
                .find { it.filterType == com.android.build.api.variant.FilterConfiguration.FilterType.ABI }
                ?.identifier
            val abiCode = when (abiFilter) {
                "armeabi-v7a" -> 1
                "arm64-v8a"   -> 2
                "x86"         -> 3
                "x86_64"      -> 4
                else           -> 0
            }
            output.versionCode.set(android.defaultConfig.versionCode!! * 10 + abiCode)
        }
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.05.00")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.animation:animation")

    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0")
    implementation("androidx.activity:activity-compose:1.9.0")

    // Media3 / ExoPlayer
    implementation("androidx.media3:media3-exoplayer:1.3.1")
    implementation("androidx.media3:media3-session:1.3.1")
    implementation("androidx.media3:media3-ui:1.3.1")
    implementation("androidx.media3:media3-common:1.3.1")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.51.1")
    ksp("com.google.dagger:hilt-compiler:2.51.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // Networking (Last.fm)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Palette
    implementation("androidx.palette:palette-ktx:1.0.0")

    // Coil
    implementation("io.coil-kt:coil-compose:2.6.0")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // JSON
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
