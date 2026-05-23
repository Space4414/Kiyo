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
    val PERFORMANCE_MODE       = booleanPreferencesKey("performance_mode")
    val BLUR_ENABLED           = booleanPreferencesKey("blur_enabled")
    val BLUR_RADIUS            = intPreferencesKey("blur_radius")
    val BLUR_QUALITY           = stringPreferencesKey("blur_quality")
    val ANIMATIONS_ENABLED     = booleanPreferencesKey("animations_enabled")
    val REDUCED_MOTION         = booleanPreferencesKey("reduced_motion")
    val AMBIENT_GRADIENTS      = booleanPreferencesKey("ambient_gradients")
    val SOLID_BACKGROUND       = booleanPreferencesKey("solid_background")
    val CROSSFADE_ENABLED      = booleanPreferencesKey("crossfade_enabled")
    val CUSTOM_DELIMITERS      = stringPreferencesKey("custom_delimiters")
    val CUSTOM_UNSPLIT_EX      = stringPreferencesKey("custom_unsplit_exceptions")
    val LASTFM_API_KEY         = stringPreferencesKey("lastfm_api_key")
    val LASTFM_API_SECRET      = stringPreferencesKey("lastfm_api_secret")
    val LASTFM_USERNAME        = stringPreferencesKey("lastfm_username")
    val LASTFM_SESSION_KEY     = stringPreferencesKey("lastfm_session_key")
    val DISCORD_RPC_ENABLED    = booleanPreferencesKey("discord_rpc_enabled")
    val DISCORD_WEBHOOK_URL    = stringPreferencesKey("discord_webhook_url")
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
            crossfadeEnabled        = prefs[Keys.CROSSFADE_ENABLED] ?: true,
            customDelimiters        = prefs[Keys.CUSTOM_DELIMITERS] ?: "",
            customUnsplitExceptions = prefs[Keys.CUSTOM_UNSPLIT_EX] ?: "",
            lastFmApiKey            = prefs[Keys.LASTFM_API_KEY] ?: "",
            lastFmApiSecret         = prefs[Keys.LASTFM_API_SECRET] ?: "",
            lastFmUsername          = prefs[Keys.LASTFM_USERNAME] ?: "",
            lastFmSessionKey        = prefs[Keys.LASTFM_SESSION_KEY] ?: "",
            discordRpcEnabled       = prefs[Keys.DISCORD_RPC_ENABLED] ?: false,
            discordWebhookUrl       = prefs[Keys.DISCORD_WEBHOOK_URL] ?: "",
        )
    }

    suspend fun setPerformanceMode(v: Boolean)  = context.settingsDataStore.edit { it[Keys.PERFORMANCE_MODE] = v }
    suspend fun setBlurEnabled(v: Boolean)       = context.settingsDataStore.edit { it[Keys.BLUR_ENABLED] = v }
    suspend fun setBlurRadius(v: Int)            = context.settingsDataStore.edit { it[Keys.BLUR_RADIUS] = v }
    suspend fun setBlurQuality(v: BlurQuality)   = context.settingsDataStore.edit { it[Keys.BLUR_QUALITY] = v.name }
    suspend fun setAnimationsEnabled(v: Boolean) = context.settingsDataStore.edit { it[Keys.ANIMATIONS_ENABLED] = v }
    suspend fun setReducedMotion(v: Boolean)     = context.settingsDataStore.edit { it[Keys.REDUCED_MOTION] = v }
    suspend fun setAmbientGradients(v: Boolean)  = context.settingsDataStore.edit { it[Keys.AMBIENT_GRADIENTS] = v }
    suspend fun setSolidBackground(v: Boolean)   = context.settingsDataStore.edit { it[Keys.SOLID_BACKGROUND] = v }
    suspend fun setCrossfadeEnabled(v: Boolean)  = context.settingsDataStore.edit { it[Keys.CROSSFADE_ENABLED] = v }
    suspend fun setCustomDelimiters(v: String)   = context.settingsDataStore.edit { it[Keys.CUSTOM_DELIMITERS] = v }
    suspend fun setCustomUnsplitExceptions(v: String) = context.settingsDataStore.edit { it[Keys.CUSTOM_UNSPLIT_EX] = v }
    suspend fun setLastFmApiKey(v: String)       = context.settingsDataStore.edit { it[Keys.LASTFM_API_KEY] = v }
    suspend fun setLastFmApiSecret(v: String)    = context.settingsDataStore.edit { it[Keys.LASTFM_API_SECRET] = v }
    suspend fun setLastFmUsername(v: String)     = context.settingsDataStore.edit { it[Keys.LASTFM_USERNAME] = v }
    suspend fun setLastFmSessionKey(v: String)   = context.settingsDataStore.edit { it[Keys.LASTFM_SESSION_KEY] = v }
    suspend fun setDiscordRpcEnabled(v: Boolean) = context.settingsDataStore.edit { it[Keys.DISCORD_RPC_ENABLED] = v }
    suspend fun setDiscordWebhookUrl(v: String)  = context.settingsDataStore.edit { it[Keys.DISCORD_WEBHOOK_URL] = v }
}
