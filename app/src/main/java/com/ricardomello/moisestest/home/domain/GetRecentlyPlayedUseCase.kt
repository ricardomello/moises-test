package com.ricardomello.moisestest.home.domain

import com.ricardomello.moisestest.shared.domain.Song
import com.ricardomello.moisestest.shared.domain.SongRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

sealed interface GetRecentlyPlayedError {
    val message: String

    data class RepositoryFailure(override val message: String) : GetRecentlyPlayedError
}

sealed interface GetRecentlyPlayedResult {
    data class Success(val flow: Flow<List<Song>>) : GetRecentlyPlayedResult
    data class Error(val error: GetRecentlyPlayedError) : GetRecentlyPlayedResult
}

class GetRecentlyPlayedUseCase @Inject constructor(
    private val repository: SongRepository,
) {
    operator fun invoke(): GetRecentlyPlayedResult = try {
        GetRecentlyPlayedResult.Success(repository.getRecentlyPlayed())
    } catch (e: Exception) {
        val error = GetRecentlyPlayedError.RepositoryFailure(
            message = e.message ?: "Failed to load recently played songs",
        )
        GetRecentlyPlayedResult.Error(error)
    }
}
