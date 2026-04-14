package com.ricardomello.moisestest.home.data.remote

import com.ricardomello.moisestest.shared.data.remote.MusicRemoteDataSource
import com.ricardomello.moisestest.shared.data.remote.RemoteSong
import com.ricardomello.moisestest.home.data.ItunesSearchApi
import com.ricardomello.moisestest.home.data.dto.ItunesSearchResult
import javax.inject.Inject

class ItunesRemoteDataSource @Inject constructor(
    private val api: ItunesSearchApi,
) : MusicRemoteDataSource {

    override suspend fun searchSongs(term: String, offset: Int, limit: Int): List<RemoteSong> =
        api.search(term = term, entity = "song", offset = offset, limit = limit)
            .results
            .filter { it.trackId != null }
            .map { it.toRemoteSong() }

    override suspend fun fetchAlbumTracks(albumId: Int): List<RemoteSong> =
        api.lookup(id = albumId.toLong(), entity = "song")
            .results
            .filter { it.wrapperType == "track" && it.trackId != null }
            .map { it.toRemoteSong() }

    private fun ItunesSearchResult.toRemoteSong() = RemoteSong(
        id = trackId!!,
        title = trackName.orEmpty(),
        artist = artistName.orEmpty(),
        albumId = collectionId ?: 0L,
        albumName = collectionName,
        artworkUrl = artworkUrl100,
        previewUrl = previewUrl,
        genre = primaryGenreName,
        durationMs = trackTimeMillis,
        releaseDate = releaseDate,
        trackNumber = trackNumber,
    )
}
