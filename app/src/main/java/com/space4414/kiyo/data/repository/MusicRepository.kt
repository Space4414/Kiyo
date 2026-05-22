package com.space4414.kiyo.data.repository

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
     * Full library sync:
     * 1. Scans MediaStore for audio files.
     * 2. Parses each track's raw artist tag into individual artist names
     *    (using the Poweramp-style [ArtistParser]).
     * 3. Upserts tracks, upserts artists, writes cross-ref rows.
     * 4. Prunes stale tracks no longer present on disk.
     */
    suspend fun syncLibrary() = withContext(Dispatchers.IO) {
        val rawTracks = scanner.scan()
        val liveIds = rawTracks.map { it.mediaStoreId }

        for (raw in rawTracks) {
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

            // Parse multi-artist tag → individual artist names
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

            if (crossRefs.isNotEmpty()) {
                trackDao.insertCrossRefs(crossRefs)
            }
        }

        // Remove stale tracks
        if (liveIds.isNotEmpty()) {
            trackDao.pruneOrphaned(liveIds)
        }

        artistDao.refreshTrackCounts()
        artistDao.deleteOrphaned()
    }

    suspend fun incrementPlayCount(trackId: Long) =
        trackDao.incrementPlayCount(trackId, System.currentTimeMillis())
}
