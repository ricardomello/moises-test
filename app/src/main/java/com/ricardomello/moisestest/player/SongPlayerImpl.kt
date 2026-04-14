package com.ricardomello.moisestest.player

import com.ricardomello.moisestest.shared.domain.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class SongPlayerImpl @Inject constructor(
    private val controller: MusicController,
) : SongPlayer {

    private val _currentSong = MutableStateFlow<Song?>(null)
    override val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    override val isPlaying: StateFlow<Boolean> = controller.isPlaying
    override val positionMs: StateFlow<Long> = controller.positionMs
    override val durationMs: StateFlow<Long> = controller.durationMs

    override fun setCurrentSong(song: Song?) { _currentSong.value = song }
    override fun load(url: String) = controller.load(url)
    override fun play() = controller.play()
    override fun pause() = controller.pause()
    override fun seekTo(ms: Long) = controller.seekTo(ms)
    override fun setRepeat(on: Boolean) = controller.setRepeat(on)
    fun release() = controller.release()
}
