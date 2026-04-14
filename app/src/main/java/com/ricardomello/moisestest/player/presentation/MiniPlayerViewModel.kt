package com.ricardomello.moisestest.player.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ricardomello.moisestest.shared.domain.Song
import com.ricardomello.moisestest.player.SongPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class MiniPlayerViewModel @Inject constructor(
    private val songPlayer: SongPlayer,
) : ViewModel() {

    val currentSong: StateFlow<Song?> = songPlayer.currentSong
    val isPlaying: StateFlow<Boolean> = songPlayer.isPlaying

    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()

    init {
        combine(songPlayer.positionMs, songPlayer.durationMs) { pos, dur ->
            if (dur > 0) pos / dur.toFloat() else 0f
        }
            .onEach { _progress.value = it }
            .launchIn(viewModelScope)
    }

    fun onPlayPause() {
        if (songPlayer.isPlaying.value) songPlayer.pause() else songPlayer.play()
    }

    fun onClose() {
        songPlayer.pause()
        songPlayer.setCurrentSong(null)
    }
}
