package com.ricardomello.moisestest.song.domain

import com.ricardomello.moisestest.shared.domain.SongRepository
import javax.inject.Inject

sealed interface MarkSongPlayedError {
    val message: String

    data class RepositoryFailure(override val message: String) : MarkSongPlayedError
}

sealed interface MarkSongPlayedResult {
    data object Success : MarkSongPlayedResult
    data class Error(val error: MarkSongPlayedError) : MarkSongPlayedResult
}

class MarkSongPlayedUseCase @Inject constructor(
    private val repository: SongRepository,
) {
    suspend operator fun invoke(id: Int): MarkSongPlayedResult = try {
        repository.markAsPlayed(id)
        MarkSongPlayedResult.Success
    } catch (e: Exception) {
        val error = MarkSongPlayedError.RepositoryFailure(
            message = e.message ?: "Failed to mark song as played",
        )
        MarkSongPlayedResult.Error(error)
    }
}
