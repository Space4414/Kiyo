package com.space4414.kiyo.ui.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore by preferencesDataStore(name = "kiyo_settings")

private object Keys {
    val PERFORMANCE_MODE    = booleanPreferencesKey("performance_mode")
    val BLUR_ENABLED        = booleanPreferencesKey("blur_enabled")
    val BLUR_RADIUS         = intPreferencesKey("blur_radius")
    val BLUR_QUALITY        = stringPreferencesKey("blur_quality")
    val ANIMATIONS_ENABLED  = booleanPreferencesKey("animations_enabled")
    val REDUCED_MOTION      = booleanPreferencesKey("reduced_motion")
    val AMBIENT_GRADIENTS   = booleanPreferencesKey("ambient_gradients")
    val SOLID_BACKGROUND    = booleanPreferencesKey("solid_background")
}

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    val settings: Flow<AppSettings> = context.settingsDataStore.data.map { prefs ->
        AppSettings(
            performanceModeEnabled  = prefs[Keys.PERFORMANCE_MODE] ?: false,
            blurEnabled             = prefs[Keys.BLUR_ENABLED] ?: true,
            blurRadiusDp            = prefs[Keys.BLUR_RADIUS] ?: 20,
            blurQuality             = prefs[Keys.BLUR_QUALITY]
                                          ?.let { runCatching { BlurQuality.valueOf(it) }.getOrNull() }
                                          ?: BlurQuality.MEDIUM,
            animationsEnabled       = prefs[Keys.ANIMATIONS_ENABLED] ?: true,
            reducedMotion           = prefs[Keys.REDUCED_MOTION] ?: false,
            ambientGradientsEnabled = prefs[Keys.AMBIENT_GRADIENTS] ?: true,
            solidBackgroundEnabled  = prefs[Keys.SOLID_BACKGROUND] ?: false,
        )
    }

    suspend fun setPerformanceMode(enabled: Boolean) =
        context.settingsDataStore.edit { it[Keys.PERFORMANCE_MODE] = enabled }

    suspend fun setBlurEnabled(enabled: Boolean) =
        context.settingsDataStore.edit { it[Keys.BLUR_ENABLED] = enabled }

    suspend fun setBlurRadius(radiusDp: Int) =
        context.settingsDataStore.edit { it[Keys.BLUR_RADIUS] = radiusDp }

    suspend fun setBlurQuality(quality: BlurQuality) =
        context.settingsDataStore.edit { it[Keys.BLUR_QUALITY] = quality.name }

    suspend fun setAnimationsEnabled(enabled: Boolean) =
        context.settingsDataStore.edit { it[Keys.ANIMATIONS_ENABLED] = enabled }

    suspend fun setReducedMotion(enabled: Boolean) =
        context.settingsDataStore.edit { it[Keys.REDUCED_MOTION] = enabled }

    suspend fun setAmbientGradients(enabled: Boolean) =
        context.settingsDataStore.edit { it[Keys.AMBIENT_GRADIENTS] = enabled }

    suspend fun setSolidBackground(enabled: Boolean) =
        context.settingsDataStore.edit { it[Keys.SOLID_BACKGROUND] = enabled }
}
