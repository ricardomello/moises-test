package com.ricardomello.moisestest.home.presentation

import com.ricardomello.moisestest.home.domain.GetRecentlyPlayedError
import com.ricardomello.moisestest.home.domain.GetRecentlyPlayedResult
import com.ricardomello.moisestest.home.domain.GetRecentlyPlayedUseCase
import com.ricardomello.moisestest.home.domain.SearchSongsError
import com.ricardomello.moisestest.home.domain.SearchSongsResult
import com.ricardomello.moisestest.home.domain.SearchSongsUseCase
import com.ricardomello.moisestest.shared.domain.Song
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
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
class HomeViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    private val getRecentlyPlayed: GetRecentlyPlayedUseCase = mockk()
    private val searchSongs: SearchSongsUseCase = mockk()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init sets content from recently played flow`() = runTest {
        val songsFlow = MutableStateFlow(listOf(song(1)))
        every { getRecentlyPlayed() } returns GetRecentlyPlayedResult.Success(songsFlow)

        val viewModel = HomeViewModel(getRecentlyPlayed, searchSongs)
        advanceUntilIdle()

        val state = viewModel.uiState.value as HomeState.Content
        assertEquals(1, state.songs.size)
        assertEquals("", state.searchQuery)
    }

    @Test
    fun `init sets error when recently played fails`() = runTest {
        every { getRecentlyPlayed() } returns GetRecentlyPlayedResult.Error(
            GetRecentlyPlayedError.RepositoryFailure("Failed"),
        )

        val viewModel = HomeViewModel(getRecentlyPlayed, searchSongs)
        advanceUntilIdle()

        val state = viewModel.uiState.value as HomeState.Error
        assertEquals("Failed", state.message)
    }

    @Test
    fun `onSearchQueryChanged updates results`() = runTest {
        every { getRecentlyPlayed() } returns GetRecentlyPlayedResult.Success(MutableStateFlow(emptyList()))
        coEvery { searchSongs("rock", 0) } returns SearchSongsResult.Success(
            songs = List(20) { idx -> song(idx + 1) },
        )
        val viewModel = HomeViewModel(getRecentlyPlayed, searchSongs)
        advanceUntilIdle()

        viewModel.onSearchQueryChanged("rock")
        advanceTimeBy(300)
        advanceUntilIdle()

        val state = viewModel.uiState.value as HomeState.Content
        assertEquals(20, state.songs.size)
        assertEquals("rock", state.searchQuery)
        assertTrue(state.canLoadMore)
    }

    @Test
    fun `onSearchQueryChanged sets error on failure`() = runTest {
        every { getRecentlyPlayed() } returns GetRecentlyPlayedResult.Success(MutableStateFlow(emptyList()))
        coEvery { searchSongs("rock", 0) } returns SearchSongsResult.Error(
            SearchSongsError.RepositoryFailure("Search failed"),
        )
        val viewModel = HomeViewModel(getRecentlyPlayed, searchSongs)
        advanceUntilIdle()

        viewModel.onSearchQueryChanged("rock")
        advanceTimeBy(300)
        advanceUntilIdle()

        val state = viewModel.uiState.value as HomeState.Error
        assertEquals("Search failed", state.message)
    }

    private fun song(id: Int) = Song(
        id = id,
        title = "Song $id",
        artist = "Artist",
        albumId = 1,
    )
}
