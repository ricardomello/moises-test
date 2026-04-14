package com.ricardomello.moisestest.shared.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(songs: List<SongEntity>)

    @Query("SELECT * FROM songs WHERE trackId = :trackId")
    fun getById(trackId: Long): Flow<SongEntity?>

    @Query("SELECT * FROM songs WHERE collectionId = :collectionId ORDER BY COALESCE(trackNumber, 9999) ASC, trackId ASC")
    fun getByAlbum(collectionId: Long): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE lastPlayedAt IS NOT NULL ORDER BY lastPlayedAt DESC")
    fun getRecentlyPlayed(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE trackId IN (:trackIds)")
    suspend fun getByIds(trackIds: List<Long>): List<SongEntity>

    @Query("UPDATE songs SET lastPlayedAt = :timestamp WHERE trackId = :trackId")
    suspend fun updateLastPlayedAt(trackId: Long, timestamp: Long)

    @Query("DELETE FROM songs WHERE collectionId = :collectionId")
    suspend fun deleteByAlbum(collectionId: Long)
}
