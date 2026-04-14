package com.ricardomello.moisestest.shared.domain

import kotlinx.coroutines.flow.Flow

interface SongRepository {
    fun getSongById(id: Int): Flow<Song?>
    fun getRecentlyPlayed(): Flow<List<Song>>
    suspend fun search(term: String, offset: Int = 0): List<Song>
    suspend fun markAsPlayed(id: Int)
}
