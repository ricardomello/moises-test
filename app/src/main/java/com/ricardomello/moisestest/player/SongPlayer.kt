package com.ricardomello.moisestest.player

import com.ricardomello.moisestest.shared.domain.Song
import kotlinx.coroutines.flow.StateFlow

interface SongPlayer {
    val currentSong: StateFlow<Song?>
    val isPlaying: StateFlow<Boolean>
    val positionMs: StateFlow<Long>
    val durationMs: StateFlow<Long>
    fun setCurrentSong(song: Song?)
    fun load(url: String)
    fun play()
    fun pause()
    fun seekTo(ms: Long)
    fun setRepeat(on: Boolean)
}
