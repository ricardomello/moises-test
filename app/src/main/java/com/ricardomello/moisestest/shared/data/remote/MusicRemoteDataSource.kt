package com.ricardomello.moisestest.shared.data.remote

interface MusicRemoteDataSource {
    suspend fun searchSongs(term: String, offset: Int = 0, limit: Int = 20): List<RemoteSong>
    suspend fun fetchAlbumTracks(albumId: Int): List<RemoteSong>
}
