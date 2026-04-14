package com.ricardomello.moisestest.song.domain

import com.ricardomello.moisestest.shared.domain.Song
import com.ricardomello.moisestest.shared.domain.SongRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

sealed interface GetSongError {
    val message: String

    data class RepositoryFailure(override val message: String) : GetSongError
}

sealed interface GetSongResult {
    data class Success(val flow: Flow<Song?>) : GetSongResult
    data class Error(val error: GetSongError) : GetSongResult
}

class GetSongUseCase @Inject constructor(
    private val repository: SongRepository,
) {
    operator fun invoke(id: Int): GetSongResult = try {
        GetSongResult.Success(repository.getSongById(id))
    } catch (e: Exception) {
        val error = GetSongError.RepositoryFailure(
            message = e.message ?: "Failed to load song",
        )
        GetSongResult.Error(error)
    }
}
