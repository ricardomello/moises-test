package com.ricardomello.moisestest.album.data.mapper

import com.ricardomello.moisestest.shared.domain.Album
import com.ricardomello.moisestest.shared.data.local.SongEntity
import com.ricardomello.moisestest.home.data.mapper.toDomain

fun List<SongEntity>.toAlbum(): Album? {
    if (isEmpty()) return null
    val first = first()
    return Album(
        id = first.collectionId.toInt(),
        title = first.collectionName ?: first.trackName,
        artist = first.artistName,
        songs = map { it.toDomain() },
    )
}
