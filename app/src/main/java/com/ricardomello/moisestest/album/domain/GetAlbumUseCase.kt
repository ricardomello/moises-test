package com.ricardomello.moisestest.album.domain

import com.ricardomello.moisestest.shared.domain.Album
import com.ricardomello.moisestest.shared.domain.AlbumRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

sealed interface GetAlbumError {
    val message: String

    data class RepositoryFailure(override val message: String) : GetAlbumError
}

sealed interface GetAlbumResult {
    data class Success(val flow: Flow<Album?>) : GetAlbumResult
    data class Error(val error: GetAlbumError) : GetAlbumResult
}

class GetAlbumUseCase @Inject constructor(
    private val repository: AlbumRepository,
) {
    operator fun invoke(albumId: Int): GetAlbumResult = try {
        GetAlbumResult.Success(repository.getAlbum(albumId))
    } catch (e: Exception) {
        val error = GetAlbumError.RepositoryFailure(
            message = e.message ?: "Failed to load album",
        )
        GetAlbumResult.Error(error)
    }
}
