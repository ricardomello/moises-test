package com.ricardomello.moisestest.song.domain

import com.ricardomello.moisestest.shared.domain.Song
import com.ricardomello.moisestest.shared.domain.SongRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetSongUseCaseTest {
    private val repository: SongRepository = mockk()
    private val useCase = GetSongUseCase(repository)

    @Test
    fun `returns success with repository flow`() {
        val flow: Flow<Song?> = flowOf(Song(1, "Title", "Artist", 10))
        every { repository.getSongById(1) } returns flow

        val result = useCase(1)

        assertTrue(result is GetSongResult.Success)
        assertEquals(flow, (result as GetSongResult.Success).flow)
    }

    @Test
    fun `returns success when repository emits null song`() {
        val flow: Flow<Song?> = flowOf(null)
        every { repository.getSongById(99) } returns flow

        val result = useCase(99)

        assertTrue(result is GetSongResult.Success)
        assertEquals(flow, (result as GetSongResult.Success).flow)
    }

    @Test
    fun `returns failure with repository message`() {
        every { repository.getSongById(2) } throws RuntimeException("Song DAO error")

        val result = useCase(2)

        assertTrue(result is GetSongResult.Error)
        assertEquals("Song DAO error", (result as GetSongResult.Error).error.message)
    }
}
