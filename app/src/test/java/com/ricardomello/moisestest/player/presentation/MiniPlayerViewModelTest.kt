package com.ricardomello.moisestest.player.presentation

import com.ricardomello.moisestest.player.SongPlayer
import com.ricardomello.moisestest.shared.domain.Song
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MiniPlayerViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    private val currentSong = MutableStateFlow<Song?>(null)
    private val isPlaying = MutableStateFlow(false)
    private val positionMs = MutableStateFlow(50L)
    private val durationMs = MutableStateFlow(100L)
    private val songPlayer: SongPlayer = mockk(relaxed = true) {
        every { this@mockk.currentSong } returns this@MiniPlayerViewModelTest.currentSong
        every { this@mockk.isPlaying } returns this@MiniPlayerViewModelTest.isPlaying
        every { this@mockk.positionMs } returns this@MiniPlayerViewModelTest.positionMs
        every { this@mockk.durationMs } returns this@MiniPlayerViewModelTest.durationMs
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `progress is derived from position and duration`() = runTest {
        val viewModel = MiniPlayerViewModel(songPlayer)
        advanceUntilIdle()

        assertEquals(0.5f, viewModel.progress.value)
    }

    @Test
    fun `onPlayPause plays when currently paused`() = runTest {
        val viewModel = MiniPlayerViewModel(songPlayer)
        isPlaying.value = false

        viewModel.onPlayPause()

        verify(exactly = 1) { songPlayer.play() }
    }

    @Test
    fun `onClose pauses and clears current song`() = runTest {
        val viewModel = MiniPlayerViewModel(songPlayer)

        viewModel.onClose()

        verify(exactly = 1) { songPlayer.pause() }
        verify(exactly = 1) { songPlayer.setCurrentSong(null) }
    }
}
