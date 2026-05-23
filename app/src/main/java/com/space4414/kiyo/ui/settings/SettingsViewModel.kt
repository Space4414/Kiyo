package com.space4414.kiyo.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repo: SettingsRepository,
) : ViewModel() {

    val settings: StateFlow<AppSettings> = repo.settings.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        AppSettings(),
    )

    fun setPerformanceMode(v: Boolean)       = viewModelScope.launch { repo.setPerformanceMode(v) }
    fun setBlurEnabled(v: Boolean)           = viewModelScope.launch { repo.setBlurEnabled(v) }
    fun setBlurRadius(v: Int)                = viewModelScope.launch { repo.setBlurRadius(v) }
    fun setBlurQuality(v: BlurQuality)       = viewModelScope.launch { repo.setBlurQuality(v) }
    fun setAnimationsEnabled(v: Boolean)     = viewModelScope.launch { repo.setAnimationsEnabled(v) }
    fun setReducedMotion(v: Boolean)         = viewModelScope.launch { repo.setReducedMotion(v) }
    fun setAmbientGradients(v: Boolean)      = viewModelScope.launch { repo.setAmbientGradients(v) }
    fun setSolidBackground(v: Boolean)       = viewModelScope.launch { repo.setSolidBackground(v) }
    fun setCrossfadeEnabled(v: Boolean)      = viewModelScope.launch { repo.setCrossfadeEnabled(v) }
    fun setCustomDelimiters(v: String)       = viewModelScope.launch { repo.setCustomDelimiters(v) }
    fun setCustomUnsplitExceptions(v: String)= viewModelScope.launch { repo.setCustomUnsplitExceptions(v) }
    fun setLastFmApiKey(v: String)           = viewModelScope.launch { repo.setLastFmApiKey(v) }
    fun setLastFmApiSecret(v: String)        = viewModelScope.launch { repo.setLastFmApiSecret(v) }
    fun setLastFmUsername(v: String)         = viewModelScope.launch { repo.setLastFmUsername(v) }
    fun setLastFmSessionKey(v: String)       = viewModelScope.launch { repo.setLastFmSessionKey(v) }
    fun setDiscordRpcEnabled(v: Boolean)     = viewModelScope.launch { repo.setDiscordRpcEnabled(v) }
    fun setDiscordWebhookUrl(v: String)      = viewModelScope.launch { repo.setDiscordWebhookUrl(v) }
}
