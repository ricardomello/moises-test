package com.ricardomello.moisestest.player

import com.ricardomello.moisestest.shared.domain.Song
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SongPlayerImplTest {
    private val isPlaying = MutableStateFlow(false)
    private val positionMs = MutableStateFlow(1000L)
    private val durationMs = MutableStateFlow(5000L)
    private val controller: MusicController = mockk(relaxed = true) {
        every { this@mockk.isPlaying } returns this@SongPlayerImplTest.isPlaying
        every { this@mockk.positionMs } returns this@SongPlayerImplTest.positionMs
        every { this@mockk.durationMs } returns this@SongPlayerImplTest.durationMs
    }

    private val songPlayer = SongPlayerImpl(controller)

    @Test
    fun `state flows are exposed from controller`() {
        assertEquals(false, songPlayer.isPlaying.value)
        assertEquals(1000L, songPlayer.positionMs.value)
        assertEquals(5000L, songPlayer.durationMs.value)
    }

    @Test
    fun `setCurrentSong updates current song state`() {
        val song = Song(id = 1, title = "Song", artist = "Artist", albumId = 10)

        songPlayer.setCurrentSong(song)
        assertEquals(song, songPlayer.currentSong.value)

        songPlayer.setCurrentSong(null)
        assertNull(songPlayer.currentSong.value)
    }

    @Test
    fun `methods delegate to controller`() {
        songPlayer.load("url")
        songPlayer.play()
        songPlayer.pause()
        songPlayer.seekTo(1234L)
        songPlayer.setRepeat(true)

        verify(exactly = 1) { controller.load("url") }
        verify(exactly = 1) { controller.play() }
        verify(exactly = 1) { controller.pause() }
        verify(exactly = 1) { controller.seekTo(1234L) }
        verify(exactly = 1) { controller.setRepeat(true) }
    }
}

