package com.ricardomello.moisestest.player

import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import com.google.common.util.concurrent.SettableFuture
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MusicControllerImplTest {
    private val testDispatcher = StandardTestDispatcher()
    private val controllerFuture = SettableFuture.create<MediaController>()
    private val controller: MediaController = mockk(relaxed = true)
    private val mediaItemFactory: MediaItemFactory = mockk()
    private val mediaItem: MediaItem = mockk(relaxed = true)
    private lateinit var sut: MusicControllerImpl
    private lateinit var listenerSlot: CapturingSlot<Player.Listener>

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        listenerSlot = slot()
        every { controller.addListener(capture(listenerSlot)) } just runs
        every { mediaItemFactory.fromUrl(any()) } returns mediaItem
        sut = MusicControllerImpl(controllerFuture, mediaItemFactory, shouldPollProgress = false)
    }

    @After
    fun tearDown() {
        sut.release()
        Dispatchers.resetMain()
    }

    @Test
    fun `load before connection is executed when controller connects`() = runTest {
        sut.load("url-1")

        controllerFuture.set(controller)
        runCurrent()

        verify(exactly = 1) { mediaItemFactory.fromUrl("url-1") }
        verify(exactly = 1) { controller.setMediaItem(mediaItem) }
        verify(exactly = 1) { controller.prepare() }
        verify(exactly = 1) { controller.play() }
    }

    @Test
    fun `listener updates playing and duration on ready state`() = runTest {
        every { controller.duration } returns 1200L
        controllerFuture.set(controller)
        runCurrent()

        listenerSlot.captured.onIsPlayingChanged(true)
        listenerSlot.captured.onPlaybackStateChanged(Player.STATE_READY)

        assertTrue(sut.isPlaying.value)
        assertEquals(1200L, sut.durationMs.value)
    }

    @Test
    fun `control methods delegate to connected controller`() = runTest {
        controllerFuture.set(controller)
        runCurrent()

        sut.play()
        sut.pause()
        sut.seekTo(123L)
        sut.setRepeat(true)

        verify(exactly = 1) { controller.play() }
        verify(exactly = 1) { controller.pause() }
        verify(exactly = 1) { controller.seekTo(123L) }
        verify(exactly = 1) { controller.repeatMode = Player.REPEAT_MODE_ONE }
    }
}
