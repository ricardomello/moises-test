package com.ricardomello.moisestest.album.presentation

import com.ricardomello.moisestest.shared.domain.Album

sealed interface AlbumState {
    data object Loading : AlbumState

    data class Content(
        val album: Album,
        val refreshError: String? = null,
    ) : AlbumState

    data class Error(val message: String) : AlbumState
}