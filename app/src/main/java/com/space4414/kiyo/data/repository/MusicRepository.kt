package com.space4414.kiyo.data.repository

import android.util.Log
import com.space4414.kiyo.data.db.dao.ArtistDao
import com.space4414.kiyo.data.db.dao.TrackDao
import com.space4414.kiyo.data.db.entity.ArtistEntity
import com.space4414.kiyo.data.db.entity.TrackArtistCrossRef
import com.space4414.kiyo.data.db.entity.TrackEntity
import com.space4414.kiyo.data.parser.ArtistParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "MusicRepository"

@Singleton
class MusicRepository @Inject constructor(
    private val trackDao: TrackDao,
    private val artistDao: ArtistDao,
    private val scanner: MediaStoreScanner,
    private val artistParser: ArtistParser,
) {
    /** Observe all tracks sorted by title. */
    val allTracks: Flow<List<TrackEntity>> = trackDao.observeAll()

    /** Observe all artists sorted by name. */
    val allArtists: Flow<List<ArtistEntity>> = artistDao.observeAll()

    fun tracksForArtist(artistId: Long): Flow<List<TrackEntity>> =
        trackDao.observeByArtist(artistId)

    fun tracksForAlbum(albumId: Long): Flow<List<TrackEntity>> =
        trackDao.observeByAlbum(albumId)

    /**
     * Full library sync.
     *
     * Safely guarded — returns without throwing on:
     *  - [SecurityException]: storage permission not yet granted (Android 6+)
     *  - Any other scan / DB exception: logged, sync aborted cleanly
     *
     * Callers should retry after storage permission is granted.
     */
    suspend fun syncLibrary() = withContext(Dispatchers.IO) {
        val rawTracks = try {
            scanner.scan()
        } catch (e: SecurityException) {
            Log.w(TAG, "Storage permission not granted — scan skipped: ${e.message}")
            return@withContext
        } catch (e: Exception) {
            Log.e(TAG, "MediaStore scan failed", e)
            return@withContext
        }

        if (rawTracks.isEmpty()) return@withContext

        val liveIds = rawTracks.map { it.mediaStoreId }

        for (raw in rawTracks) {
            try {
                val trackId: Long = run {
                    val existing = trackDao.getByMediaStoreId(raw.mediaStoreId)
                    if (existing != null) {
                        existing.id
                    } else {
                        trackDao.insert(
                            TrackEntity(
                                mediaStoreId = raw.mediaStoreId,
                                title = raw.title,
                                rawArtist = raw.artist,
                                album = raw.album,
                                albumId = raw.albumId,
                                durationMs = raw.durationMs,
                                filePath = raw.filePath,
                                trackNumber = raw.trackNumber,
                                year = raw.year,
                            )
                        )
                    }
                }

                val parsedNames = artistParser.parse(raw.artist)
                val crossRefs = parsedNames.mapNotNull { name ->
                    val artistId: Long = run {
                        artistDao.getByName(name)?.id
                            ?: artistDao.insert(ArtistEntity(name = name))
                                .takeIf { it > 0 }
                            ?: artistDao.getByName(name)?.id
                            ?: return@mapNotNull null
                    }
                    TrackArtistCrossRef(trackId = trackId, artistId = artistId)
                }

                if (crossRefs.isNotEmpty()) trackDao.insertCrossRefs(crossRefs)
            } catch (e: Exception) {
                Log.w(TAG, "Skipping track '${raw.title}': ${e.message}")
            }
        }

        try {
            if (liveIds.isNotEmpty()) trackDao.pruneOrphaned(liveIds)
            artistDao.refreshTrackCounts()
            artistDao.deleteOrphaned()
        } catch (e: Exception) {
            Log.w(TAG, "Post-scan cleanup failed: ${e.message}")
        }
    }

    suspend fun incrementPlayCount(trackId: Long) =
        trackDao.incrementPlayCount(trackId, System.currentTimeMillis())
}
