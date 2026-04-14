package com.ricardomello.moisestest.song.presentation

import com.ricardomello.moisestest.shared.domain.Song

sealed interface SongState {
    data object Loading : SongState
    data class Content(
        val song: Song,
        val isPlaying: Boolean,
        val isRepeatOn: Boolean,
        val progressSeconds: Int,
        val durationSeconds: Int,
        val prevSongId: Int? = null,
        val nextSongId: Int? = null,
        val showOptions: Boolean = false,
    ) : SongState
    data class Error(val message: String) : SongState
}
