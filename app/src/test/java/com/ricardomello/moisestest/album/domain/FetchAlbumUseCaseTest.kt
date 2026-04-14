package com.ricardomello.moisestest.album.domain

import com.ricardomello.moisestest.shared.domain.AlbumRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FetchAlbumUseCaseTest {
    private val repository: AlbumRepository = mockk(relaxed = true)
    private val useCase = FetchAlbumUseCase(repository)

    @Test
    fun `returns success and calls repository`() = runTest {
        val result = useCase(1)

        assertTrue(result is FetchAlbumResult.Success)
        coVerify(exactly = 1) { repository.fetchAndSave(1) }
    }

    @Test
    fun `returns failure with repository message`() = runTest {
        coEvery { repository.fetchAndSave(7) } throws RuntimeException("Remote timeout")

        val result = useCase(7)

        assertTrue(result is FetchAlbumResult.Error)
        assertEquals("Remote timeout", (result as FetchAlbumResult.Error).error.message)
    }

    @Test
    fun `supports integer boundary ids`() = runTest {
        val maxResult = useCase(Int.MAX_VALUE)
        val minResult = useCase(Int.MIN_VALUE)

        assertTrue(maxResult is FetchAlbumResult.Success)
        assertTrue(minResult is FetchAlbumResult.Success)
        coVerify { repository.fetchAndSave(Int.MAX_VALUE) }
        coVerify { repository.fetchAndSave(Int.MIN_VALUE) }
    }
}
