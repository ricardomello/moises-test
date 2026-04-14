package com.ricardomello.moisestest.album.data

import com.ricardomello.moisestest.album.data.mapper.toAlbum
import com.ricardomello.moisestest.shared.data.remote.MusicRemoteDataSource
import com.ricardomello.moisestest.shared.domain.Album
import com.ricardomello.moisestest.shared.domain.AlbumRepository
import com.ricardomello.moisestest.shared.data.local.SongDao
import com.ricardomello.moisestest.home.data.mapper.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AlbumRepositoryImpl @Inject constructor(
    private val remoteDataSource: MusicRemoteDataSource,
    private val dao: SongDao,
) : AlbumRepository {

    override fun getAlbum(albumId: Int): Flow<Album?> =
        dao.getByAlbum(albumId.toLong()).map { it.toAlbum() }

    // This will save the response of the endpoint to the database and the database
    // as the single source of truth will update the use case, with the get album function
    override suspend fun fetchAndSave(albumId: Int) {
        val tracks = remoteDataSource.fetchAlbumTracks(albumId).map { it.toEntity() }
        if (tracks.isNotEmpty()) {
            val existingTimestamps = dao.getByIds(tracks.map { it.trackId })
                .associate { it.trackId to it.lastPlayedAt }
            val tracksWithHistory = tracks.map { track ->
                track.copy(lastPlayedAt = existingTimestamps[track.trackId])
            }
            dao.deleteByAlbum(albumId.toLong())
            dao.insertAll(tracksWithHistory)
        }
    }
}
