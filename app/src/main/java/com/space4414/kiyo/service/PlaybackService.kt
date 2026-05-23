package com.space4414.kiyo.service

import android.content.Intent
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.space4414.kiyo.integration.discord.DiscordRpcClient
import com.space4414.kiyo.integration.lastfm.LastFmScrobbler
import com.space4414.kiyo.ui.settings.SettingsRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Lifecycle-aware foreground playback service backed by Media3 ExoPlayer.
 *
 * Crossfade:
 *   When [crossfadeEnabled] is true, every auto or manual track transition triggers a
 *   300 ms linear fade-in from silence (volume 0 → 1) so track changes feel smooth
 *   rather than abrupt. Note: ExoPlayer is a single-engine; this is a fade-IN effect
 *   rather than a true simultaneous crossfade. True crossfade would require two players.
 *
 * SDK branching:
 *  - Notification channel creation: API 26+ (O) → [NotificationHelper.createChannels]
 *  - POST_NOTIFICATIONS permission check: handled by the OS on API 33+ (T)
 *  - Foreground service type "mediaPlayback": declared in manifest, checked at OS level
 */
@AndroidEntryPoint
class PlaybackService : MediaSessionService() {

    @Inject lateinit var notificationHelper: NotificationHelper
    @Inject lateinit var discordRpc: DiscordRpcClient
    @Inject lateinit var scrobbler: LastFmScrobbler
    @Inject lateinit var settingsRepository: SettingsRepository

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var fadeJob: Job? = null
    private var exoPlayer: ExoPlayer? = null
    private var mediaSession: MediaSession? = null
    private var crossfadeEnabled = true

    override fun onCreate() {
        super.onCreate()

        notificationHelper.createChannels()

        // Read crossfade setting once on start; live updates not needed here
        serviceScope.launch {
            crossfadeEnabled = try {
                settingsRepository.settings.first().crossfadeEnabled
            } catch (e: Exception) {
                true // default on error
            }
        }

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()

        val player = ExoPlayer.Builder(this)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .build()

        exoPlayer = player

        mediaSession = MediaSession.Builder(this, player)
            .setCallback(SessionCallback())
            .build()

        // Scrobbling + Discord RPC listener
        player.addListener(PlaybackEventListener(discordRpc, scrobbler))

        // Crossfade listener (smooth fade-in on track transition)
        player.addListener(CrossfadeListener(player))
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? =
        mediaSession

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        if (player == null || !player.playWhenReady) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        exoPlayer = null
        serviceScope.cancel()
        super.onDestroy()
    }

    // ─── Inner classes ────────────────────────────────────────────────────────

    private inner class SessionCallback : MediaSession.Callback

    /**
     * Performs a 300 ms linear fade-in whenever a track transitions.
     * Sets player volume to 0 immediately, then ramps back to 1.0 over 300 ms.
     */
    private inner class CrossfadeListener(private val player: ExoPlayer) : Player.Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            if (!crossfadeEnabled || mediaItem == null) return
            fadeJob?.cancel()
            fadeJob = serviceScope.launch {
                player.volume = 0f
                val steps = 20
                val stepMs = 300L / steps
                for (i in 1..steps) {
                    delay(stepMs)
                    player.volume = i.toFloat() / steps.toFloat()
                }
                player.volume = 1f
            }
        }
    }

    private class PlaybackEventListener(
        private val discord: DiscordRpcClient,
        private val scrobbler: LastFmScrobbler,
    ) : Player.Listener {

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            discord.updateState(isPlaying = isPlaying)
        }

        override fun onMediaItemTransition(
            mediaItem: MediaItem?,
            reason: Int,
        ) {
            mediaItem ?: return
            val title = mediaItem.mediaMetadata.title?.toString() ?: return
            val artist = mediaItem.mediaMetadata.artist?.toString() ?: ""
            val durationMs = mediaItem.mediaMetadata.extras
                ?.getLong("duration_ms", 0L) ?: 0L

            discord.updateTrack(title = title, artist = artist, durationMs = durationMs)
            scrobbler.onTrackStart(title = title, artist = artist, durationMs = durationMs)
        }
    }
}
