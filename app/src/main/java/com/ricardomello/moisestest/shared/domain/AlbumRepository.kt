package com.ricardomello.moisestest.shared.domain

import kotlinx.coroutines.flow.Flow

interface AlbumRepository {
    fun getAlbum(albumId: Int): Flow<Album?>
    suspend fun fetchAndSave(albumId: Int)
}
