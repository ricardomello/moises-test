package com.ricardomello.moisestest.song.presentation

import androidx.lifecycle.SavedStateHandle
import com.ricardomello.moisestest.album.domain.GetAlbumResult
import com.ricardomello.moisestest.album.domain.GetAlbumUseCase
import com.ricardomello.moisestest.navigation.Screen
import com.ricardomello.moisestest.player.SongPlayer
import com.ricardomello.moisestest.shared.domain.Album
import com.ricardomello.moisestest.shared.domain.Song
import com.ricardomello.moisestest.song.domain.GetSongError
import com.ricardomello.moisestest.song.domain.GetSongResult
import com.ricardomello.moisestest.song.domain.GetSongUseCase
import com.ricardomello.moisestest.song.domain.MarkSongPlayedError
import com.ricardomello.moisestest.song.domain.MarkSongPlayedResult
import com.ricardomello.moisestest.song.domain.MarkSongPlayedUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SongViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    private val getSong: GetSongUseCase = mockk()
    private val getAlbum: GetAlbumUseCase = mockk()
    private val markSongPlayed: MarkSongPlayedUseCase = mockk()

    private val currentSong = MutableStateFlow<Song?>(null)
    private val isPlaying = MutableStateFlow(false)
    private val positionMs = MutableStateFlow(0L)
    private val durationMs = MutableStateFlow(0L)
    private val songPlayer: SongPlayer = mockk(relaxed = true) {
        every { this@mockk.currentSong } returns this@SongViewModelTest.currentSong
        every { this@mockk.isPlaying } returns this@SongViewModelTest.isPlaying
        every { this@mockk.positionMs } returns this@SongViewModelTest.positionMs
        every { this@mockk.durationMs } returns this@SongViewModelTest.durationMs
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
    fun `init sets error when getSong fails`() = runTest {
        every { getSong(1) } returns GetSongResult.Error(GetSongError.RepositoryFailure("Load failed"))
        every { getAlbum(10) } returns GetAlbumResult.Success(flowOf(null))

        val viewModel = SongViewModel(
            savedStateHandle(songId = 1, albumId = 10),
            getSong,
            getAlbum,
            markSongPlayed,
            songPlayer,
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value as SongState.Error
        assertEquals("Load failed", state.message)
    }

    @Test
    fun `init loads playable song and marks as played`() = runTest {
        val song = song(id = 1, previewUrl = "preview")
        every { getSong(1) } returns GetSongResult.Success(flowOf(song))
        every { getAlbum(10) } returns GetAlbumResult.Success(flowOf(album(10, song)))
        coEvery { markSongPlayed(1) } returns MarkSongPlayedResult.Success

        val viewModel = SongViewModel(
            savedStateHandle(songId = 1, albumId = 10),
            getSong,
            getAlbum,
            markSongPlayed,
            songPlayer,
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value as SongState.Content
        assertEquals(1, state.song.id)
        verify(exactly = 1) { songPlayer.load("preview") }
        verify(exactly = 1) { songPlayer.setCurrentSong(song) }
        coVerify(exactly = 1) { markSongPlayed(1) }
    }

    @Test
    fun `mark as played failure sets error message`() = runTest {
        val song = song(id = 1, previewUrl = "preview")
        every { getSong(1) } returns GetSongResult.Success(flowOf(song))
        every { getAlbum(10) } returns GetAlbumResult.Success(flowOf(album(10, song)))
        coEvery { markSongPlayed(1) } returns MarkSongPlayedResult.Error(
            MarkSongPlayedError.RepositoryFailure("Mark failed"),
        )

        val viewModel = SongViewModel(
            savedStateHandle(songId = 1, albumId = 10),
            getSong,
            getAlbum,
            markSongPlayed,
            songPlayer,
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is SongState.Error)
        assertEquals("Mark failed", (state as SongState.Error).message)
    }

    @Test
    fun `onPlayPauseToggle delegates to player`() = runTest {
        val song = song(id = 1, previewUrl = "preview")
        every { getSong(1) } returns GetSongResult.Success(flowOf(song))
        every { getAlbum(any()) } returns GetAlbumResult.Success(flowOf(null))
        coEvery { markSongPlayed(1) } returns MarkSongPlayedResult.Success

        val viewModel = SongViewModel(
            savedStateHandle(songId = 1, albumId = null),
            getSong,
            getAlbum,
            markSongPlayed,
            songPlayer,
        )
        isPlaying.value = false
        advanceUntilIdle()

        viewModel.onPlayPauseToggle()

        verify(exactly = 1) { songPlayer.play() }
    }

    private fun savedStateHandle(songId: Int, albumId: Int?) = SavedStateHandle(
        buildMap {
            put(Screen.Song.ARG_SONG_ID, songId)
            if (albumId != null) put(Screen.Song.ARG_ALBUM_ID, albumId)
        },
    )

    private fun song(id: Int, previewUrl: String? = null) = Song(
        id = id,
        title = "Song $id",
        artist = "Artist",
        albumId = 10,
        previewUrl = previewUrl,
    )

    private fun album(id: Int, song: Song) = Album(
        id = id,
        title = "Album $id",
        artist = "Artist",
        songs = listOf(song),
    )
}
