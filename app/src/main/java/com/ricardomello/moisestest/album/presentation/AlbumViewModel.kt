package com.ricardomello.moisestest.album.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ricardomello.moisestest.album.domain.FetchAlbumResult
import com.ricardomello.moisestest.album.domain.FetchAlbumUseCase
import com.ricardomello.moisestest.album.domain.GetAlbumResult
import com.ricardomello.moisestest.album.domain.GetAlbumUseCase
import com.ricardomello.moisestest.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlbumViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getAlbum: GetAlbumUseCase,
    private val fetchAlbum: FetchAlbumUseCase,
) : ViewModel() {

    private val albumId: Int = checkNotNull(savedStateHandle[Screen.Album.ARG_ALBUM_ID]) {
        "AlbumViewModel requires an albumId nav argument"
    }

    private val _uiState = MutableStateFlow<AlbumState>(AlbumState.Loading)
    val uiState: StateFlow<AlbumState> = _uiState.asStateFlow()

    init {
        val result = getAlbum(albumId)
        when (result) {
            is GetAlbumResult.Success -> {
                result.flow.onEach { album ->
                    _uiState.value = if (album != null) {
                        AlbumState.Content(album)
                    } else {
                        AlbumState.Loading
                    }
                }.launchIn(viewModelScope)
            }

            is GetAlbumResult.Error -> {
                _uiState.value = AlbumState.Error(result.error.message)
            }
        }

        viewModelScope.launch {
            val result = fetchAlbum(albumId)

            if (result is FetchAlbumResult.Error) {
                val message = result.error.message
                _uiState.value = when (val current = _uiState.value) {
                    is AlbumState.Content -> current.copy(refreshError = message)
                    else -> AlbumState.Error(message)
                }
            }
        }
    }
}
