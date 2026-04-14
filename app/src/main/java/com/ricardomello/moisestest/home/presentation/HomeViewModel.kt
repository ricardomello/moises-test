package com.ricardomello.moisestest.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ricardomello.moisestest.shared.domain.Song
import com.ricardomello.moisestest.home.domain.GetRecentlyPlayedResult
import com.ricardomello.moisestest.home.domain.GetRecentlyPlayedUseCase
import com.ricardomello.moisestest.home.domain.SearchSongsResult
import com.ricardomello.moisestest.home.domain.SearchSongsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getRecentlyPlayed: GetRecentlyPlayedUseCase,
    private val searchSongs: SearchSongsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeState>(HomeState.Loading)
    val uiState: StateFlow<HomeState> = _uiState.asStateFlow()

    private var recentlyPlayed: List<Song> = emptyList()
    private var searchOffset = 0
    private var searchJob: Job? = null
    private var loadMoreJob: Job? = null

    init {
        when (val result = getRecentlyPlayed()) {
            is GetRecentlyPlayedResult.Success -> {
                result.flow.onEach { songs ->
                    recentlyPlayed = songs
                    val current = _uiState.value
                    if (current !is HomeState.Content || current.searchQuery.isBlank()) {
                        _uiState.value = HomeState.Content(songs = songs, searchQuery = "")
                    }
                }
                .launchIn(viewModelScope)
            }
            is GetRecentlyPlayedResult.Error -> {
                _uiState.value = HomeState.Error(result.error.message)
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        if (query.isBlank()) {
            searchJob?.cancel()
            loadMoreJob?.cancel()
            searchOffset = 0
            _uiState.value = HomeState.Content(songs = recentlyPlayed, searchQuery = "")
            return
        }

        searchJob?.cancel()
        loadMoreJob?.cancel()
        searchOffset = 0
        _uiState.value = HomeState.Content(songs = emptyList(), searchQuery = query, isSearching = true)

        searchJob = viewModelScope.launch {
            delay(300)
            when (val result = searchSongs(query, offset = 0)) {
                is SearchSongsResult.Success -> {
                    val results = result.songs
                    searchOffset = results.size
                    _uiState.value = HomeState.Content(
                        songs = results,
                        searchQuery = query,
                        canLoadMore = results.size >= PAGE_SIZE,
                    )
                }
                is SearchSongsResult.Error -> {
                    _uiState.value = HomeState.Error(result.error.message)
                }
            }
        }
    }

    fun onSongMoreClick(song: Song) {
        val current = _uiState.value as? HomeState.Content ?: return
        _uiState.value = current.copy(selectedSong = song)
    }

    fun onDismissBottomSheet() {
        val current = _uiState.value as? HomeState.Content ?: return
        _uiState.value = current.copy(selectedSong = null)
    }

    fun onLoadMore() {
        val current = _uiState.value as? HomeState.Content ?: return
        if (!current.canLoadMore || current.isLoadingMore || current.searchQuery.isBlank()) return

        val query = current.searchQuery
        val existingSongs = current.songs
        _uiState.value = current.copy(isLoadingMore = true)

        loadMoreJob = viewModelScope.launch {
            when (val result = searchSongs(query, offset = searchOffset)) {
                is SearchSongsResult.Success -> {
                    val results = result.songs
                    searchOffset += results.size
                    _uiState.value = HomeState.Content(
                        songs = existingSongs + results,
                        searchQuery = query,
                        canLoadMore = results.size >= PAGE_SIZE,
                    )
                }
                is SearchSongsResult.Error -> {
                    _uiState.value = current.copy(isLoadingMore = false)
                }
            }
        }
    }

    private companion object {
        const val PAGE_SIZE = 20
    }
}
