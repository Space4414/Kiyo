package com.space4414.kiyo.service

import android.content.Intent
import android.os.Build
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.space4414.kiyo.integration.discord.DiscordRpcClient
import com.space4414.kiyo.integration.lastfm.LastFmScrobbler
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Lifecycle-aware foreground playback service backed by Media3 ExoPlayer.
 *
 * Media3's [MediaSessionService] manages the foreground service notification automatically
 * via [DefaultMediaNotificationProvider]. All we need to do is configure the player and
 * session, and the framework handles foreground promotion / demotion correctly on every
 * API level from 21 to 35.
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

    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()

        // Ensure notification channel exists before Media3 posts its notification (API 26+)
        notificationHelper.createChannels()

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()

        val player = ExoPlayer.Builder(this)
            .setAudioAttributes(audioAttributes, /* handleAudioFocus= */ true)
            .setHandleAudioBecomingNoisy(true)
            .build()

        mediaSession = MediaSession.Builder(this, player)
            .setCallback(SessionCallback())
            .build()

        // Attach listeners for RPC + scrobbling
        player.addListener(PlaybackEventListener(discordRpc, scrobbler))
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
        super.onDestroy()
    }

    // ─── Inner classes ────────────────────────────────────────────────────────

    private inner class SessionCallback : MediaSession.Callback

    private class PlaybackEventListener(
        private val discord: DiscordRpcClient,
        private val scrobbler: LastFmScrobbler,
    ) : androidx.media3.common.Player.Listener {

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            discord.updateState(isPlaying = isPlaying)
        }

        override fun onMediaItemTransition(
            mediaItem: androidx.media3.common.MediaItem?,
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
