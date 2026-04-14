package com.ricardomello.moisestest.shared.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey
    val trackId: Long,
    val trackName: String,
    val artistName: String,
    val collectionId: Long,
    val collectionName: String?,
    val artworkUrl100: String?,
    val previewUrl: String?,
    val primaryGenreName: String?,
    val trackTimeMillis: Long?,
    val releaseDate: String?,
    val lastPlayedAt: Long? = null,
    val trackNumber: Int? = null,
)
