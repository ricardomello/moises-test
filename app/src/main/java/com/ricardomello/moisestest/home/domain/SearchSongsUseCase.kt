package com.ricardomello.moisestest.home.domain

import com.ricardomello.moisestest.shared.domain.Song
import com.ricardomello.moisestest.shared.domain.SongRepository
import javax.inject.Inject

sealed interface SearchSongsError {
    val message: String

    data class RepositoryFailure(override val message: String) : SearchSongsError
}

sealed interface SearchSongsResult {
    data class Success(val songs: List<Song>) : SearchSongsResult
    data class Error(val error: SearchSongsError) : SearchSongsResult
}

class SearchSongsUseCase @Inject constructor(
    private val repository: SongRepository,
) {
    suspend operator fun invoke(term: String, offset: Int = 0): SearchSongsResult = try {
        SearchSongsResult.Success(repository.search(term, offset))
    } catch (e: Exception) {
        val error = SearchSongsError.RepositoryFailure(
            message = e.message ?: "Failed to search songs",
        )
        SearchSongsResult.Error(error)
    }
}
