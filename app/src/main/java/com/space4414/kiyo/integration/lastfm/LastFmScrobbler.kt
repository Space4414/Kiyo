package com.space4414.kiyo.integration.lastfm

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.util.Log
import androidx.core.content.getSystemService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.FormBody
import java.math.BigInteger
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Last.fm Scrobbler & NowPlaying client.
 *
 * Behaviour:
 * - When a track starts, [onTrackStart] fires a NowPlaying update.
 * - If the track plays ≥50% or ≥4 minutes (Last.fm spec), [onTrackComplete] queues a scrobble.
 * - If the device is offline, the scrobble is written to [ScrobbleCache].
 * - A [ConnectivityManager.NetworkCallback] watches for network restoration and triggers
 *   an automatic cache flush on reconnect.
 *
 * Configuration: set [apiKey], [apiSecret], and [sessionKey] from Settings before use.
 * Session key is obtained via Last.fm auth flow (out of scope here — store after auth handshake).
 */
@Singleton
class LastFmScrobbler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val cache: ScrobbleCache,
) {
    companion object {
        private const val TAG = "LastFmScrobbler"
        private const val API_URL = "https://ws.audioscrobbler.com/2.0/"
        private const val SCROBBLE_THRESHOLD_MS = 4 * 60 * 1000L
        private const val SCROBBLE_MIN_DURATION_MS = 30_000L
    }

    var apiKey: String = ""
    var apiSecret: String = ""
    var sessionKey: String = ""

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val http = OkHttpClient()
    private var isOnline = isNetworkAvailable()

    // Track the currently playing item to respect the 50% rule
    private var currentTrack: String = ""
    private var currentArtist: String = ""
    private var currentDurationMs: Long = 0L
    private var trackStartedAt: Long = 0L

    init {
        registerNetworkCallback()
    }

    /** Call when a new track begins. Sends NowPlaying if credentials are set. */
    fun onTrackStart(title: String, artist: String, durationMs: Long) {
        maybeScrobbleCurrent()
        currentTrack = title
        currentArtist = artist
        currentDurationMs = durationMs
        trackStartedAt = System.currentTimeMillis()
        if (credentialsValid()) {
            scope.launch { sendNowPlaying(title, artist) }
        }
    }

    /** Call when a track finishes naturally. Scrobbles if threshold is met. */
    fun onTrackComplete(title: String, artist: String, durationMs: Long, timestamp: Long) {
        if (durationMs < SCROBBLE_MIN_DURATION_MS) return
        scope.launch { submitScrobble(PendingScrobble(title, artist, timestamp)) }
    }

    // ─── Internal ─────────────────────────────────────────────────────────────

    private fun maybeScrobbleCurrent() {
        if (currentTrack.isBlank() || currentDurationMs <= 0L) return
        val elapsed = System.currentTimeMillis() - trackStartedAt
        val threshold = minOf(SCROBBLE_THRESHOLD_MS, currentDurationMs / 2)
        if (elapsed >= threshold && currentDurationMs >= SCROBBLE_MIN_DURATION_MS) {
            val s = PendingScrobble(currentTrack, currentArtist, trackStartedAt / 1000)
            scope.launch { submitScrobble(s) }
        }
    }

    private suspend fun sendNowPlaying(title: String, artist: String) {
        if (!isOnline) return
        val params = sortedMapOf(
            "method" to "track.updateNowPlaying",
            "track" to title,
            "artist" to artist,
            "api_key" to apiKey,
            "sk" to sessionKey,
        )
        params["api_sig"] = sign(params)
        post(params + ("format" to "json"))
    }

    private suspend fun submitScrobble(scrobble: PendingScrobble) {
        if (!isOnline) {
            cache.enqueue(scrobble)
            Log.d(TAG, "Offline — queued: ${scrobble.track}")
            return
        }
        val params = sortedMapOf(
            "method" to "track.scrobble",
            "track[0]" to scrobble.track,
            "artist[0]" to scrobble.artist,
            "timestamp[0]" to scrobble.timestamp.toString(),
            "api_key" to apiKey,
            "sk" to sessionKey,
        )
        params["api_sig"] = sign(params)
        val resp = post(params + ("format" to "json"))
        if (resp == null) {
            cache.enqueue(scrobble)
        }
    }

    private suspend fun flushCache() {
        val pending = cache.peek(50)
        if (pending.isEmpty()) return
        Log.d(TAG, "Flushing ${pending.size} cached scrobbles")
        for (s in pending) submitScrobble(s)
        cache.remove(pending)
    }

    private fun post(params: Map<String, String>): String? {
        return try {
            val body = FormBody.Builder().apply {
                params.forEach { (k, v) -> add(k, v) }
            }.build()
            val request = Request.Builder().url(API_URL).post(body).build()
            http.newCall(request).execute().use { resp ->
                if (resp.isSuccessful) resp.body?.string() else null
            }
        } catch (e: Exception) {
            Log.w(TAG, "Last.fm request failed: ${e.message}")
            null
        }
    }

    /** HMAC-MD5 API signature per Last.fm spec. */
    private fun sign(params: Map<String, String>): String {
        val raw = params.entries.sortedBy { it.key }.joinToString("") { "${it.key}${it.value}" } + apiSecret
        val md = MessageDigest.getInstance("MD5")
        val hash = md.digest(raw.toByteArray())
        return BigInteger(1, hash).toString(16).padStart(32, '0')
    }

    private fun credentialsValid() =
        apiKey.isNotBlank() && apiSecret.isNotBlank() && sessionKey.isNotBlank()

    @Suppress("DEPRECATION")
    private fun isNetworkAvailable(): Boolean {
        val cm = context.getSystemService<ConnectivityManager>() ?: return false
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val net = cm.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(net) ?: return false
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            cm.activeNetworkInfo?.isConnected == true
        }
    }

    private fun registerNetworkCallback() {
        val cm = context.getSystemService<ConnectivityManager>() ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            cm.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    isOnline = true
                    scope.launch { flushCache() }
                }
                override fun onLost(network: Network) {
                    isOnline = false
                }
            })
        }
    }
}
