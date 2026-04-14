package com.ricardomello.moisestest.player

import kotlinx.coroutines.flow.StateFlow

interface MusicController {
    val isPlaying: StateFlow<Boolean>
    val positionMs: StateFlow<Long>
    val durationMs: StateFlow<Long>
    fun load(url: String)
    fun play()
    fun pause()
    fun seekTo(ms: Long)
    fun setRepeat(on: Boolean)
    fun release()
}
