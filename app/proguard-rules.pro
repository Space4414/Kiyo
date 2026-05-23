# ─── Kiyo application classes ─────────────────────────────────────────────────
-keep class com.space4414.kiyo.** { *; }

# ─── Room ─────────────────────────────────────────────────────────────────────
-keepclassmembers class * {
    @androidx.room.* <methods>;
}
-keep class * extends androidx.room.RoomDatabase { *; }

# ─── Media3 / ExoPlayer ───────────────────────────────────────────────────────
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**
-keep class com.google.android.exoplayer2.** { *; }

# ─── OkHttp (Last.fm + Discord RPC) ──────────────────────────────────────────
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**

# ─── Hilt / Dagger ────────────────────────────────────────────────────────────
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# ─── Kotlin coroutines ────────────────────────────────────────────────────────
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# ─── DataStore / Preferences ──────────────────────────────────────────────────
-keep class androidx.datastore.** { *; }

# ─── Coil image loader ────────────────────────────────────────────────────────
-keep class coil.** { *; }
-dontwarn coil.**

# ─── kotlinx.serialization (ScrobbleCache JSON) ───────────────────────────────
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keep @kotlinx.serialization.Serializable class * { *; }

# ─── Aggressive shrinking ─────────────────────────────────────────────────────
# Remove all logging in release builds
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
}
