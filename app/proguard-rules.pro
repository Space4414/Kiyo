-keep class com.space4414.kiyo.** { *; }
-keepclassmembers class * {
    @androidx.room.* <methods>;
}
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**
-keep class com.google.android.exoplayer2.** { *; }
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }
