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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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

    val recentlyPlayed: StateFlow<List<TrackEntity>> = repository.recentlyPlayed
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _storagePermissionGranted = MutableStateFlow(false)
    val storagePermissionGranted: StateFlow<Boolean> = _storagePermissionGranted.asStateFlow()

    // ── EQ State ──────────────────────────────────────────────────────────────
    private val _eqBands = MutableStateFlow(FloatArray(31) { 0f })
    val eqBands: StateFlow<FloatArray> = _eqBands.asStateFlow()

    private val _preAmpGain = MutableStateFlow(0f)
    val preAmpGain: StateFlow<Float> = _preAmpGain.asStateFlow()

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null
    private var positionPollingJob: Job? = null

    init {
        connectToService()
        refreshLibrary()
    }

    // ─── Public API ───────────────────────────────────────────────────────────

    fun updateStoragePermission(granted: Boolean) {
        val wasGranted = _storagePermissionGranted.value
        _storagePermissionGranted.value = granted
        if (granted && !wasGranted) {
            refreshLibrary()
        }
    }

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
        val mediaItem = buildMediaItem(track)
        ctrl.setMediaItem(mediaItem)
        ctrl.prepare()
        ctrl.play()
        _uiState.update { it.copy(currentTrack = track, durationMs = track.durationMs, positionMs = 0L) }
        viewModelScope.launch {
            try { repository.incrementPlayCount(track.id) }
            catch (e: Exception) { Log.w(TAG, "incrementPlayCount failed", e) }
        }
    }

    fun playAll(tracks: List<TrackEntity>, startIndex: Int = 0) {
        val ctrl = controller ?: return
        val items = tracks.map { buildMediaItem(it) }
        ctrl.setMediaItems(items, startIndex, 0L)
        ctrl.prepare()
        ctrl.play()
        _uiState.update {
            it.copy(
                queue = tracks,
                currentTrack = tracks.getOrNull(startIndex),
                durationMs = tracks.getOrNull(startIndex)?.durationMs ?: 0L,
                positionMs = 0L,
            )
        }
        tracks.getOrNull(startIndex)?.let { track ->
            viewModelScope.launch {
                try { repository.incrementPlayCount(track.id) }
                catch (e: Exception) { Log.w(TAG, "incrementPlayCount failed", e) }
            }
        }
    }

    fun togglePlayPause() {
        val ctrl = controller ?: return
        if (ctrl.isPlaying) ctrl.pause() else ctrl.play()
    }

    fun seekTo(positionMs: Long) {
        controller?.seekTo(positionMs)
        _uiState.update { it.copy(positionMs = positionMs) }
    }

    fun skipNext() { controller?.seekToNextMediaItem() }
    fun skipPrev() { controller?.seekToPreviousMediaItem() }

    // ── EQ API ────────────────────────────────────────────────────────────────

    fun setEqBand(index: Int, gainDb: Float) {
        val current = _eqBands.value.copyOf()
        current[index] = gainDb.coerceIn(-12f, 12f)
        _eqBands.value = current
    }

    fun setPreAmpGain(gainDb: Float) {
        _preAmpGain.value = gainDb.coerceIn(-12f, 12f)
    }

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
                    startPositionPolling()
                } catch (e: Exception) {
                    Log.w(TAG, "MediaController connection failed: ${e.message}")
                }
            }, MoreExecutors.directExecutor())
        } catch (e: Exception) {
            Log.e(TAG, "Failed to build MediaController", e)
        }
    }

    /**
     * Polls ExoPlayer's current position every second while playing.
     * This drives the seek bar's smooth real-time progress.
     */
    private fun startPositionPolling() {
        positionPollingJob?.cancel()
        positionPollingJob = viewModelScope.launch {
            while (true) {
                delay(1_000L)
                val ctrl = controller ?: continue
                if (ctrl.isPlaying) {
                    val pos = ctrl.currentPosition
                    val dur = ctrl.duration.takeIf { it > 0L }
                    _uiState.update { s ->
                        s.copy(
                            positionMs = pos,
                            durationMs = if (dur != null && dur > 0L) dur else s.durationMs,
                        )
                    }
                }
            }
        }
    }

    private val playerListener = object : Player.Listener {

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _uiState.update { it.copy(isPlaying = isPlaying) }
        }

        /**
         * Fires whenever ExoPlayer auto-advances to the next/previous track,
         * or when a new item is set manually. This is the critical fix for the
         * frozen UI bug: we sync currentTrack from the queue by media index.
         */
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            val ctrl = controller ?: return
            val idx = ctrl.currentMediaItemIndex
            val track = _uiState.value.queue.getOrNull(idx)
            _uiState.update { s ->
                s.copy(
                    currentTrack = track ?: s.currentTrack,
                    durationMs = track?.durationMs ?: s.durationMs,
                    positionMs = 0L,
                )
            }
            // Increment play count for auto-advanced tracks
            if (track != null && reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO) {
                viewModelScope.launch {
                    try { repository.incrementPlayCount(track.id) }
                    catch (e: Exception) { Log.w(TAG, "incrementPlayCount on auto-advance failed", e) }
                }
            }
        }

        /**
         * Called when the player's state changes (IDLE, BUFFERING, READY, ENDED).
         * READY state is when ExoPlayer knows the real duration — we capture it here.
         */
        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_READY) {
                val dur = controller?.duration?.takeIf { it > 0L }
                if (dur != null) {
                    _uiState.update { it.copy(durationMs = dur) }
                }
            }
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
        positionPollingJob?.cancel()
        controller?.removeListener(playerListener)
        controllerFuture?.let {
            try { MediaController.releaseFuture(it) }
            catch (e: Exception) { Log.w(TAG, "releaseFuture error", e) }
        }
        super.onCleared()
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private fun buildMediaItem(track: TrackEntity): MediaItem =
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
