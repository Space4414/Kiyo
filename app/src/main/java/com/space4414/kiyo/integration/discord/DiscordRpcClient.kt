package com.space4414.kiyo.integration.discord

import android.util.Log
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Discord Rich Presence emitter for Kiyo.
 *
 * Android does not support Discord's local IPC socket (Unix domain socket / named pipe)
 * used by desktop clients. Instead this client formats standard RPC presence payloads
 * and dispatches them to a user-configured Discord webhook or a lightweight local
 * Discord RPC bridge if one is running on the same network.
 *
 * When no endpoint is configured the client runs in no-op mode — all calls are safe
 * to invoke regardless.
 *
 * Payload format follows the Discord Rich Presence spec:
 * https://discord.com/developers/docs/topics/gateway-events#activity-object
 */
@Singleton
class DiscordRpcClient @Inject constructor() {

    companion object {
        private const val TAG = "DiscordRpcClient"
        private const val JSON_MEDIA_TYPE = "application/json; charset=utf-8"
    }

    /** Optional webhook / bridge URL. Set from Settings screen. */
    var endpointUrl: String? = null

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val http = OkHttpClient()

    private var currentTitle: String = ""
    private var currentArtist: String = ""
    private var currentDurationMs: Long = 0L
    private var isPlaying: Boolean = false

    /** Called when the active track changes. */
    fun updateTrack(title: String, artist: String, durationMs: Long) {
        currentTitle = title
        currentArtist = artist
        currentDurationMs = durationMs
        dispatch()
    }

    /** Called when play/pause state toggles. */
    fun updateState(isPlaying: Boolean) {
        this.isPlaying = isPlaying
        dispatch()
    }

    /** Clear presence (e.g. on stop). */
    fun clear() {
        currentTitle = ""
        currentArtist = ""
        currentDurationMs = 0L
        isPlaying = false
        dispatch()
    }

    private fun dispatch() {
        val url = endpointUrl?.takeIf { it.isNotBlank() } ?: return
        val payload = buildPayload()
        scope.launch {
            try {
                val body = payload.toString().toRequestBody(JSON_MEDIA_TYPE.toMediaType())
                val request = Request.Builder().url(url).post(body).build()
                http.newCall(request).execute().use { resp ->
                    if (!resp.isSuccessful) {
                        Log.w(TAG, "RPC dispatch failed: HTTP ${resp.code}")
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "RPC dispatch error: ${e.message}")
            }
        }
    }

    private fun buildPayload(): JSONObject = JSONObject().apply {
        put("op", 3) // Discord Gateway OP 3 = Presence Update
        put("d", JSONObject().apply {
            put("since", if (isPlaying) System.currentTimeMillis() else JSONObject.NULL)
            put("status", if (isPlaying) "online" else "idle")
            put("afk", !isPlaying)
            put("activities", org.json.JSONArray().apply {
                if (currentTitle.isNotBlank()) {
                    put(JSONObject().apply {
                        put("name", "Kiyo")
                        put("type", 2) // 2 = Listening
                        put("details", currentTitle)
                        put("state", currentArtist)
                        put("assets", JSONObject().apply {
                            put("large_image", "kiyo_logo")
                            put("large_text", "Kiyo Music Player")
                            put("small_image", if (isPlaying) "playing" else "paused")
                            put("small_text", if (isPlaying) "Playing" else "Paused")
                        })
                        if (isPlaying && currentDurationMs > 0) {
                            val now = System.currentTimeMillis()
                            put("timestamps", JSONObject().apply {
                                put("start", now)
                                put("end", now + currentDurationMs)
                            })
                        }
                    })
                }
            })
        })
    }
}
