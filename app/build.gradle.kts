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

    // ─── Release keystore ─────────────────────────────────────────────────────
    signingConfigs {
        create("release") {
            val keystorePath  = System.getenv("SIGNING_KEYSTORE_PATH")
            val storePass     = System.getenv("SIGNING_STORE_PASSWORD")
            val kAlias        = System.getenv("SIGNING_KEY_ALIAS")
            val kPass         = System.getenv("SIGNING_KEY_PASSWORD")
            if (!keystorePath.isNullOrBlank() && !storePass.isNullOrBlank()) {
                storeFile     = file(keystorePath)
                storeType     = "PKCS12"
                storePassword = storePass
                keyAlias      = kAlias
                keyPassword   = kPass
            }
        }
    }

    // ─── Distribution tracks ───────────────────────────────────────────────────
    flavorDimensions += "track"

    productFlavors {
        create("alpha") {
            dimension = "track"
            applicationId = "com.space4414.kiyo.alpha"
            versionNameSuffix = "-alpha"
            resValue("string", "app_name", "Kiyo Alpha")
        }
        create("beta") {
            dimension = "track"
            applicationId = "com.space4414.kiyo.beta"
            versionNameSuffix = "-beta"
            resValue("string", "app_name", "Kiyo Beta")
        }
        create("stable") {
            dimension = "track"
            resValue("string", "app_name", "Kiyo")
        }
    }

    // ─── ABI splits — per-ABI APKs reduce download size per device ────────────
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
            val releaseConfig = signingConfigs.getByName("release")
            signingConfig = if (releaseConfig.storeFile?.exists() == true)
                releaseConfig
            else
                signingConfigs.getByName("debug")
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
    // ── Compose BOM ────────────────────────────────────────────────────────────
    val composeBom = platform("androidx.compose:compose-bom:2024.05.00")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    // NOTE: material-icons-extended removed (was ~5 MB in debug APK).
    // Custom icons are now lightweight XML vector drawables in res/drawable/.
    // material-icons-core added explicitly (NOT transitively included by material3).
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.compose.animation:animation")

    // ── AndroidX core ──────────────────────────────────────────────────────────
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0")
    implementation("androidx.activity:activity-compose:1.9.0")

    // ── Media3 / ExoPlayer ─────────────────────────────────────────────────────
    implementation("androidx.media3:media3-exoplayer:1.3.1")
    implementation("androidx.media3:media3-session:1.3.1")
    implementation("androidx.media3:media3-ui:1.3.1")
    implementation("androidx.media3:media3-common:1.3.1")

    // ── Room ───────────────────────────────────────────────────────────────────
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // ── Hilt ───────────────────────────────────────────────────────────────────
    implementation("com.google.dagger:hilt-android:2.51.1")
    ksp("com.google.dagger:hilt-compiler:2.51.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // ── Coroutines ─────────────────────────────────────────────────────────────
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // ── Network (Last.fm scrobbling + Discord RPC) ─────────────────────────────
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // ── Image loading ──────────────────────────────────────────────────────────
    // Coil handles MediaStore album art URIs with in-memory + disk caching.
    // palette-ktx removed (not wired to any live UI — saves ~200 KB from debug APK).
    implementation("io.coil-kt:coil-compose:2.6.0")

    // ── Settings persistence ────────────────────────────────────────────────────
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // ── Last.fm scrobble cache serialisation ───────────────────────────────────
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // ── Debug tooling ──────────────────────────────────────────────────────────
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
