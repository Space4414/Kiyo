package com.space4414.kiyo.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.space4414.kiyo.data.db.dao.ArtistDao
import com.space4414.kiyo.data.db.dao.TrackDao
import com.space4414.kiyo.data.db.entity.ArtistEntity
import com.space4414.kiyo.data.db.entity.TrackArtistCrossRef
import com.space4414.kiyo.data.db.entity.TrackEntity

@Database(
    entities = [TrackEntity::class, ArtistEntity::class, TrackArtistCrossRef::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao
    abstract fun artistDao(): ArtistDao

    companion object {
        const val DATABASE_NAME = "kiyo_library.db"

        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
                .fallbackToDestructiveMigration()
                .build()
    }
}
