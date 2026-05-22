package com.space4414.kiyo.integration.lastfm

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

private val Context.scrobbleDataStore by preferencesDataStore(name = "scrobble_cache")

data class PendingScrobble(
    val track: String,
    val artist: String,
    val timestamp: Long,
)

/**
 * Offline scrobble queue backed by DataStore.
 *
 * When the device loses connectivity, scrobble events are serialised as JSON
 * and appended here. On network reconnection [LastFmScrobbler] flushes the
 * entire queue to Last.fm's batch scrobble endpoint (up to 50 per call).
 */
@Singleton
class ScrobbleCache @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        private val CACHE_KEY = stringPreferencesKey("pending_scrobbles")
        private const val MAX_CACHED = 500
    }

    suspend fun enqueue(scrobble: PendingScrobble) {
        context.scrobbleDataStore.edit { prefs ->
            val current = parseList(prefs[CACHE_KEY])
            if (current.size < MAX_CACHED) {
                current.add(scrobble)
                prefs[CACHE_KEY] = serialise(current)
            }
        }
    }

    suspend fun peek(limit: Int = 50): List<PendingScrobble> {
        val raw = context.scrobbleDataStore.data
            .map { it[CACHE_KEY] }
            .first()
        return parseList(raw).take(limit)
    }

    suspend fun remove(scrobbles: List<PendingScrobble>) {
        context.scrobbleDataStore.edit { prefs ->
            val current = parseList(prefs[CACHE_KEY])
            current.removeAll(scrobbles.toSet())
            prefs[CACHE_KEY] = serialise(current)
        }
    }

    suspend fun size(): Int =
        context.scrobbleDataStore.data
            .map { parseList(it[CACHE_KEY]).size }
            .first()

    // ─── Serialisation ────────────────────────────────────────────────────────

    private fun serialise(list: List<PendingScrobble>): String {
        val arr = JSONArray()
        list.forEach { s ->
            arr.put(JSONObject().apply {
                put("track", s.track)
                put("artist", s.artist)
                put("ts", s.timestamp)
            })
        }
        return arr.toString()
    }

    private fun parseList(raw: String?): MutableList<PendingScrobble> {
        if (raw.isNullOrBlank()) return mutableListOf()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                PendingScrobble(
                    track = obj.getString("track"),
                    artist = obj.getString("artist"),
                    timestamp = obj.getLong("ts"),
                )
            }.toMutableList()
        } catch (e: Exception) {
            mutableListOf()
        }
    }
}
