package com.ricardomello.moisestest.home.domain

import com.ricardomello.moisestest.shared.domain.Song
import com.ricardomello.moisestest.shared.domain.SongRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetRecentlyPlayedUseCaseTest {
    private val repository: SongRepository = mockk()
    private val useCase = GetRecentlyPlayedUseCase(repository)

    @Test
    fun `returns success with repository flow`() {
        val flow: Flow<List<Song>> = flowOf(listOf(Song(1, "Title", "Artist", 10)))
        every { repository.getRecentlyPlayed() } returns flow

        val result = useCase()

        assertTrue(result is GetRecentlyPlayedResult.Success)
        assertEquals(flow, (result as GetRecentlyPlayedResult.Success).flow)
    }

    @Test
    fun `returns failure with repository message`() {
        every { repository.getRecentlyPlayed() } throws IllegalStateException("DB unavailable")

        val result = useCase()

        assertTrue(result is GetRecentlyPlayedResult.Error)
        assertEquals("DB unavailable", (result as GetRecentlyPlayedResult.Error).error.message)
    }
}
