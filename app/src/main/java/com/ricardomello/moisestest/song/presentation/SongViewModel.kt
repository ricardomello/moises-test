package com.ricardomello.moisestest.song.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ricardomello.moisestest.album.domain.GetAlbumResult
import com.ricardomello.moisestest.navigation.Screen
import com.ricardomello.moisestest.album.domain.GetAlbumUseCase
import com.ricardomello.moisestest.song.domain.GetSongResult
import com.ricardomello.moisestest.song.domain.GetSongUseCase
import com.ricardomello.moisestest.song.domain.MarkSongPlayedResult
import com.ricardomello.moisestest.song.domain.MarkSongPlayedUseCase
import com.ricardomello.moisestest.player.SongPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SongViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getSong: GetSongUseCase,
    getAlbum: GetAlbumUseCase,
    private val markSongPlayed: MarkSongPlayedUseCase,
    private val songPlayer: SongPlayer,
) : ViewModel() {

    private val songId: Int = checkNotNull(savedStateHandle[Screen.Song.ARG_SONG_ID]) {
        "SongViewModel requires a songId nav argument"
    }
    private val albumId: Int? = savedStateHandle.get<Int>(Screen.Song.ARG_ALBUM_ID)?.takeIf { it != -1 }

    private val _uiState = MutableStateFlow<SongState>(SongState.Loading)
    val uiState: StateFlow<SongState> = _uiState.asStateFlow()

    private var songLoaded = false

    init {
        when (val result = getSong(songId)) {
            is GetSongResult.Success -> {
                result.flow.onEach { song ->
                    if (song == null) {
                        if (_uiState.value is SongState.Loading) {
                            _uiState.value = SongState.Error("Song not found")
                        }
                        return@onEach
                    }
                    if (!songLoaded && song.previewUrl != null) {
                        songLoaded = true
                        songPlayer.load(song.previewUrl)
                        songPlayer.setCurrentSong(song)
                        viewModelScope.launch {
                            when (val markResult = markSongPlayed(songId)) {
                                is MarkSongPlayedResult.Success -> Unit
                                is MarkSongPlayedResult.Error -> {
                                    _uiState.value = SongState.Error(markResult.error.message)
                                }
                            }
                        }
                    }
                    val current = _uiState.value
                    _uiState.value = SongState.Content(
                        song = song,
                        isPlaying = songPlayer.isPlaying.value,
                        isRepeatOn = (current as? SongState.Content)?.isRepeatOn ?: false,
                        progressSeconds = (songPlayer.positionMs.value / 1000).toInt(),
                        durationSeconds = (songPlayer.durationMs.value / 1000).toInt(),
                        prevSongId = (current as? SongState.Content)?.prevSongId,
                        nextSongId = (current as? SongState.Content)?.nextSongId,
                    )
                }
                .launchIn(viewModelScope)
            }
            is GetSongResult.Error -> {
                _uiState.value = SongState.Error(result.error.message)
            }
        }

        if (albumId != null) {
            when (val result = getAlbum(albumId)) {
                is GetAlbumResult.Success -> {
                    result.flow.onEach { album ->
                        val songs = album?.songs ?: return@onEach
                        val idx = songs.indexOfFirst { it.id == songId }
                        val prevSongId = if (idx > 0) songs[idx - 1].id else null
                        val nextSongId = if (idx < songs.lastIndex) songs[idx + 1].id else null
                        val current = _uiState.value as? SongState.Content ?: return@onEach
                        _uiState.value = current.copy(prevSongId = prevSongId, nextSongId = nextSongId)
                    }
                    .launchIn(viewModelScope)
                }
                is GetAlbumResult.Error -> {
                    _uiState.value = SongState.Error(result.error.message)
                }
            }
        }

        songPlayer.isPlaying
            .onEach { playing ->
                val current = _uiState.value as? SongState.Content ?: return@onEach
                _uiState.value = current.copy(isPlaying = playing)
            }
            .launchIn(viewModelScope)

        combine(songPlayer.positionMs, songPlayer.durationMs) { pos, dur -> pos to dur }
            .onEach { (pos, dur) ->
                val current = _uiState.value as? SongState.Content ?: return@onEach
                _uiState.value = current.copy(
                    progressSeconds = (pos / 1000).toInt(),
                    durationSeconds = (dur / 1000).toInt().coerceAtLeast(1),
                )
            }
            .launchIn(viewModelScope)
    }

    fun onMoreClick() {
        val current = _uiState.value as? SongState.Content ?: return
        _uiState.value = current.copy(showOptions = true)
    }

    fun onDismissOptions() {
        val current = _uiState.value as? SongState.Content ?: return
        _uiState.value = current.copy(showOptions = false)
    }

    fun onPlayPauseToggle() {
        val current = _uiState.value as? SongState.Content ?: return
        if (current.isPlaying) songPlayer.pause() else songPlayer.play()
    }

    fun onRepeatToggle() {
        val current = _uiState.value as? SongState.Content ?: return
        val newRepeat = !current.isRepeatOn
        _uiState.value = current.copy(isRepeatOn = newRepeat)
        songPlayer.setRepeat(newRepeat)
    }

    fun onSeek(seconds: Int) {
        songPlayer.seekTo(seconds * 1000L)
    }
}
