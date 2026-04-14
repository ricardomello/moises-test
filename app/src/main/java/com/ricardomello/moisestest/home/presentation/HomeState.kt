package com.ricardomello.moisestest.home.presentation

import com.ricardomello.moisestest.shared.domain.Song

sealed interface HomeState {
    data object Loading : HomeState

    data class Content(
        val songs: List<Song>,
        val searchQuery: String,
        val isSearching: Boolean = false,
        val isLoadingMore: Boolean = false,
        val canLoadMore: Boolean = false,
        val selectedSong: Song? = null,
    ) : HomeState

    data class Error(val message: String) : HomeState
}