package com.space4414.kiyo.data.db.dao

import androidx.room.*
import com.space4414.kiyo.data.db.entity.TrackArtistCrossRef
import com.space4414.kiyo.data.db.entity.TrackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tracks: List<TrackEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(track: TrackEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCrossRefs(refs: List<TrackArtistCrossRef>)

    @Query("SELECT * FROM tracks ORDER BY title ASC")
    fun observeAll(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE last_played_at > 0 ORDER BY last_played_at DESC LIMIT :limit")
    fun observeRecentlyPlayed(limit: Int = 20): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE id = :id")
    suspend fun getById(id: Long): TrackEntity?

    @Query("SELECT * FROM tracks WHERE media_store_id = :msId")
    suspend fun getByMediaStoreId(msId: Long): TrackEntity?

    @Query("UPDATE tracks SET play_count = play_count + 1, last_played_at = :timestamp WHERE id = :id")
    suspend fun incrementPlayCount(id: Long, timestamp: Long)

    @Query("SELECT * FROM tracks WHERE album_id = :albumId ORDER BY track_number ASC")
    fun observeByAlbum(albumId: Long): Flow<List<TrackEntity>>

    @Query("""
        SELECT t.* FROM tracks t
        INNER JOIN track_artist_cross_ref cr ON t.id = cr.track_id
        WHERE cr.artist_id = :artistId
        ORDER BY t.title ASC
    """)
    fun observeByArtist(artistId: Long): Flow<List<TrackEntity>>

    @Query("DELETE FROM tracks WHERE media_store_id NOT IN (:liveIds)")
    suspend fun pruneOrphaned(liveIds: List<Long>)

    @Query("DELETE FROM tracks")
    suspend fun deleteAll()
}
