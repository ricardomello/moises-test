package com.ricardomello.moisestest.home.data

import com.ricardomello.moisestest.shared.data.remote.MusicRemoteDataSource
import com.ricardomello.moisestest.shared.data.local.SongDao
import com.ricardomello.moisestest.home.data.mapper.toEntity
import com.ricardomello.moisestest.home.data.mapper.toDomain
import com.ricardomello.moisestest.shared.domain.Song
import com.ricardomello.moisestest.shared.domain.SongRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SongRepositoryImpl @Inject constructor(
    private val remoteDataSource: MusicRemoteDataSource,
    private val dao: SongDao,
) : SongRepository {

    override fun getSongById(id: Int): Flow<Song?> =
        dao.getById(id.toLong()).map { it?.toDomain() }

    override fun getRecentlyPlayed(): Flow<List<Song>> =
        dao.getRecentlyPlayed().map { entities -> entities.map { it.toDomain() } }

    override suspend fun search(term: String, offset: Int): List<Song> {
        val entities = remoteDataSource.searchSongs(term, offset = offset).map { it.toEntity() }
        if (entities.isEmpty()) return emptyList()
        val existingTimestamps = dao.getByIds(entities.map { it.trackId })
            .associate { it.trackId to it.lastPlayedAt }
        val entitiesWithHistory = entities.map { entity ->
            entity.copy(lastPlayedAt = existingTimestamps[entity.trackId])
        }
        dao.insertAll(entitiesWithHistory)
        return entitiesWithHistory.map { it.toDomain() }
    }

    override suspend fun markAsPlayed(id: Int) {
        dao.updateLastPlayedAt(id.toLong(), System.currentTimeMillis())
    }
}
