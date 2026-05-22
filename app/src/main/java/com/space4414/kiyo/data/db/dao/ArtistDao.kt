package com.space4414.kiyo.data.db.dao

import androidx.room.*
import com.space4414.kiyo.data.db.entity.ArtistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ArtistDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(artists: List<ArtistEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(artist: ArtistEntity): Long

    @Query("SELECT * FROM artists ORDER BY name ASC")
    fun observeAll(): Flow<List<ArtistEntity>>

    @Query("SELECT * FROM artists WHERE id = :id")
    suspend fun getById(id: Long): ArtistEntity?

    @Query("SELECT * FROM artists WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): ArtistEntity?

    @Query("""
        UPDATE artists SET track_count = (
            SELECT COUNT(*) FROM track_artist_cross_ref WHERE artist_id = artists.id
        )
    """)
    suspend fun refreshTrackCounts()

    @Query("DELETE FROM artists WHERE track_count = 0")
    suspend fun deleteOrphaned()
}
