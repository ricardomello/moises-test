package com.ricardomello.moisestest.home.data.mapper

import com.ricardomello.moisestest.shared.data.remote.RemoteSong
import com.ricardomello.moisestest.shared.data.local.SongEntity
import com.ricardomello.moisestest.shared.domain.Song

fun RemoteSong.toEntity(): SongEntity = SongEntity(
    trackId = id,
    trackName = title,
    artistName = artist,
    collectionId = albumId,
    collectionName = albumName,
    artworkUrl100 = artworkUrl,
    previewUrl = previewUrl,
    primaryGenreName = genre,
    trackTimeMillis = durationMs,
    releaseDate = releaseDate,
    trackNumber = trackNumber,
)

fun SongEntity.toDomain(): Song = Song(
    id = trackId.toInt(),
    title = trackName,
    artist = artistName,
    albumId = collectionId.toInt(),
    artworkUrl = artworkUrl100,
    previewUrl = previewUrl,
)
