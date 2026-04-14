package com.ricardomello.moisestest.home.domain

import com.ricardomello.moisestest.shared.domain.Song
import com.ricardomello.moisestest.shared.domain.SongRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchSongsUseCaseTest {
    private val repository: SongRepository = mockk()
    private val useCase = SearchSongsUseCase(repository)

    @Test
    fun `returns success with repository songs`() = runTest {
        val songs = listOf(Song(1, "Title", "Artist", 10))
        coEvery { repository.search("rock", 20) } returns songs

        val result = useCase("rock", 20)

        assertTrue(result is SearchSongsResult.Success)
        assertEquals(songs, (result as SearchSongsResult.Success).songs)
        coVerify(exactly = 1) { repository.search("rock", 20) }
    }

    @Test
    fun `uses default offset when omitted`() = runTest {
        coEvery { repository.search("jazz", 0) } returns emptyList()

        val result = useCase("jazz")

        assertTrue(result is SearchSongsResult.Success)
        coVerify(exactly = 1) { repository.search("jazz", 0) }
    }

    @Test
    fun `returns failure with repository message`() = runTest {
        coEvery { repository.search("pop", 0) } throws RuntimeException("Network down")

        val result = useCase("pop")

        assertTrue(result is SearchSongsResult.Error)
        assertEquals("Network down", (result as SearchSongsResult.Error).error.message)
    }
}
