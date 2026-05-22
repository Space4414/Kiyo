package com.space4414.kiyo.ui.viewmodel

import android.app.Application
import android.content.ComponentName
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.space4414.kiyo.data.db.entity.TrackEntity
import com.space4414.kiyo.data.repository.MusicRepository
import com.space4414.kiyo.service.PlaybackService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "PlayerViewModel"

data class PlayerUiState(
    val isPlaying: Boolean = false,
    val currentTrack: TrackEntity? = null,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val queue: List<TrackEntity> = emptyList(),
)

@HiltViewModel
class PlayerViewModel @Inject constructor(
    application: Application,
    private val repository: MusicRepository,
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    val allTracks: StateFlow<List<TrackEntity>> = repository.allTracks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** True once the user has granted storage permission. */
    private val _storagePermissionGranted = MutableStateFlow(false)
    val storagePermissionGranted: StateFlow<Boolean> = _storagePermissionGranted.asStateFlow()

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null

    init {
        connectToService()
        // Initial scan — MusicRepository.syncLibrary() silently returns on
        // SecurityException so this is safe even before permission is granted.
        refreshLibrary()
    }

    // ─── Public API ───────────────────────────────────────────────────────────

    /**
     * Called by MainActivity when the storage permission result is known.
     * Triggers a library scan whenever the permission transitions to granted.
     */
    fun updateStoragePermission(granted: Boolean) {
        val wasGranted = _storagePermissionGranted.value
        _storagePermissionGranted.value = granted
        if (granted && !wasGranted) {
            refreshLibrary()
        }
    }

    /** Trigger a MediaStore library scan. Safe to call at any time. */
    fun refreshLibrary() {
        viewModelScope.launch {
            try {
                repository.syncLibrary()
            } catch (e: Exception) {
                Log.e(TAG, "Library sync error", e)
            }
        }
    }

    fun playTrack(track: TrackEntity) {
        val ctrl = controller ?: return
        val mediaItem = MediaItem.Builder()
            .setUri(track.filePath)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(track.title)
                    .setArtist(track.rawArtist)
                    .setAlbumTitle(track.album)
                    .build()
            )
            .build()
        ctrl.setMediaItem(mediaItem)
        ctrl.prepare()
        ctrl.play()
        _uiState.update { it.copy(currentTrack = track, durationMs = track.durationMs) }
        viewModelScope.launch {
            try { repository.incrementPlayCount(track.id) }
            catch (e: Exception) { Log.w(TAG, "incrementPlayCount failed", e) }
        }
    }

    fun playAll(tracks: List<TrackEntity>, startIndex: Int = 0) {
        val ctrl = controller ?: return
        val items = tracks.map { track ->
            MediaItem.Builder()
                .setUri(track.filePath)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(track.title)
                        .setArtist(track.rawArtist)
                        .setAlbumTitle(track.album)
                        .build()
                )
                .build()
        }
        ctrl.setMediaItems(items, startIndex, 0L)
        ctrl.prepare()
        ctrl.play()
        _uiState.update { it.copy(queue = tracks, currentTrack = tracks.getOrNull(startIndex)) }
    }

    fun togglePlayPause() {
        val ctrl = controller ?: return
        if (ctrl.isPlaying) ctrl.pause() else ctrl.play()
    }

    fun seekTo(positionMs: Long) { controller?.seekTo(positionMs) }
    fun skipNext() { controller?.seekToNextMediaItem() }
    fun skipPrev() { controller?.seekToPreviousMediaItem() }

    // ─── MediaController lifecycle ────────────────────────────────────────────

    private fun connectToService() {
        val ctx = getApplication<Application>()
        try {
            val token = SessionToken(ctx, ComponentName(ctx, PlaybackService::class.java))
            controllerFuture = MediaController.Builder(ctx, token).buildAsync()
            controllerFuture?.addListener({
                try {
                    controller = controllerFuture?.get()
                    controller?.addListener(playerListener)
                } catch (e: Exception) {
                    Log.w(TAG, "MediaController connection failed: ${e.message}")
                }
            }, MoreExecutors.directExecutor())
        } catch (e: Exception) {
            Log.e(TAG, "Failed to build MediaController", e)
        }
    }

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _uiState.update { it.copy(isPlaying = isPlaying) }
        }
        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int,
        ) {
            _uiState.update { it.copy(positionMs = newPosition.positionMs) }
        }
    }

    override fun onCleared() {
        controller?.removeListener(playerListener)
        controllerFuture?.let {
            try { MediaController.releaseFuture(it) }
            catch (e: Exception) { Log.w(TAG, "releaseFuture error", e) }
        }
        super.onCleared()
    }
}
