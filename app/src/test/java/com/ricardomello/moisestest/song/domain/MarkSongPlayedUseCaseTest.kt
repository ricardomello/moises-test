package com.ricardomello.moisestest.song.domain

import com.ricardomello.moisestest.shared.domain.SongRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MarkSongPlayedUseCaseTest {
    private val repository: SongRepository = mockk(relaxed = true)
    private val useCase = MarkSongPlayedUseCase(repository)

    @Test
    fun `returns success and calls repository`() = runTest {
        val result = useCase(1)

        assertTrue(result is MarkSongPlayedResult.Success)
        coVerify(exactly = 1) { repository.markAsPlayed(1) }
    }

    @Test
    fun `returns failure with repository message`() = runTest {
        coEvery { repository.markAsPlayed(1) } throws RuntimeException("Database error")

        val result = useCase(1)

        assertTrue(result is MarkSongPlayedResult.Error)
        assertEquals("Database error", (result as MarkSongPlayedResult.Error).error.message)
    }
}
