package com.ricardomello.moisestest.album.domain

import com.ricardomello.moisestest.shared.domain.AlbumRepository
import javax.inject.Inject

sealed interface FetchAlbumError {
    val message: String

    data class RepositoryFailure(override val message: String) : FetchAlbumError
}

sealed interface FetchAlbumResult {
    data object Success : FetchAlbumResult
    data class Error(val error: FetchAlbumError) : FetchAlbumResult
}

class FetchAlbumUseCase @Inject constructor(
    private val repository: AlbumRepository,
) {
    suspend operator fun invoke(albumId: Int): FetchAlbumResult = try {
        repository.fetchAndSave(albumId)
        FetchAlbumResult.Success
    } catch (e: Exception) {
        val error = FetchAlbumError.RepositoryFailure(
            message = e.message ?: "Failed to fetch album",
        )
        FetchAlbumResult.Error(error)
    }
}
