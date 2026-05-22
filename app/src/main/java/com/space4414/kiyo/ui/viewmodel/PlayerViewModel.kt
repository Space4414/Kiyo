package com.space4414.kiyo.ui.viewmodel

import android.app.Application
import android.content.ComponentName
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

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null

    init {
        connectToService()
        viewModelScope.launch { repository.syncLibrary() }
    }

    private fun connectToService() {
        val ctx = getApplication<Application>()
        val token = SessionToken(
            ctx,
            ComponentName(ctx, PlaybackService::class.java)
        )
        controllerFuture = MediaController.Builder(ctx, token).buildAsync()
        controllerFuture?.addListener({
            controller = controllerFuture?.get()
            controller?.addListener(playerListener)
        }, MoreExecutors.directExecutor())
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
        viewModelScope.launch { repository.incrementPlayCount(track.id) }
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
        controllerFuture?.let { MediaController.releaseFuture(it) }
        super.onCleared()
    }
}
