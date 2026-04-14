package com.ricardomello.moisestest.album.presentation

import androidx.lifecycle.SavedStateHandle
import com.ricardomello.moisestest.album.domain.FetchAlbumError
import com.ricardomello.moisestest.album.domain.FetchAlbumResult
import com.ricardomello.moisestest.album.domain.FetchAlbumUseCase
import com.ricardomello.moisestest.album.domain.GetAlbumError
import com.ricardomello.moisestest.album.domain.GetAlbumResult
import com.ricardomello.moisestest.album.domain.GetAlbumUseCase
import com.ricardomello.moisestest.navigation.Screen
import com.ricardomello.moisestest.shared.domain.Album
import com.ricardomello.moisestest.shared.domain.Song
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
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
class AlbumViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    private val getAlbum: GetAlbumUseCase = mockk()
    private val fetchAlbum: FetchAlbumUseCase = mockk()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init sets content when album is available`() = runTest {
        every { getAlbum(1) } returns GetAlbumResult.Success(flowOf(album(1)))
        coEvery { fetchAlbum(1) } returns FetchAlbumResult.Success

        val viewModel = AlbumViewModel(savedStateHandle(1), getAlbum, fetchAlbum)
        advanceUntilIdle()

        val state = viewModel.uiState.value as AlbumState.Content
        assertEquals(1, state.album.id)
    }

    @Test
    fun `init sets error when getAlbum fails`() = runTest {
        every { getAlbum(1) } returns GetAlbumResult.Error(GetAlbumError.RepositoryFailure("Load error"))
        coEvery { fetchAlbum(1) } returns FetchAlbumResult.Success

        val viewModel = AlbumViewModel(savedStateHandle(1), getAlbum, fetchAlbum)
        advanceUntilIdle()

        val state = viewModel.uiState.value as AlbumState.Error
        assertEquals("Load error", state.message)
    }

    @Test
    fun `fetch error is exposed as refreshError when content exists`() = runTest {
        every { getAlbum(1) } returns GetAlbumResult.Success(flowOf(album(1)))
        coEvery { fetchAlbum(1) } returns FetchAlbumResult.Error(
            FetchAlbumError.RepositoryFailure("Refresh failed"),
        )

        val viewModel = AlbumViewModel(savedStateHandle(1), getAlbum, fetchAlbum)
        advanceUntilIdle()

        val state = viewModel.uiState.value as AlbumState.Content
        assertEquals("Refresh failed", state.refreshError)
    }

    private fun savedStateHandle(albumId: Int) = SavedStateHandle(
        mapOf(Screen.Album.ARG_ALBUM_ID to albumId),
    )

    private fun album(id: Int) = Album(
        id = id,
        title = "Album $id",
        artist = "Artist",
        songs = listOf(
            Song(id = 1, title = "Song 1", artist = "Artist", albumId = id),
        ),
    )
}
