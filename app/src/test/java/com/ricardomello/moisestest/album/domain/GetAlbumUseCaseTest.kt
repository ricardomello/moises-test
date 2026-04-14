package com.ricardomello.moisestest.album.domain

import com.ricardomello.moisestest.shared.domain.Album
import com.ricardomello.moisestest.shared.domain.AlbumRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetAlbumUseCaseTest {
    private val repository: AlbumRepository = mockk()
    private val useCase = GetAlbumUseCase(repository)

    @Test
    fun `returns success with repository flow`() {
        val flow: Flow<Album?> = flowOf(Album(1, "Album", "Artist", emptyList()))
        every { repository.getAlbum(1) } returns flow

        val result = useCase(1)

        assertTrue(result is GetAlbumResult.Success)
        assertEquals(flow, (result as GetAlbumResult.Success).flow)
    }

    @Test
    fun `returns success when repository emits null album`() {
        val flow: Flow<Album?> = flowOf(null)
        every { repository.getAlbum(2) } returns flow

        val result = useCase(2)

        assertTrue(result is GetAlbumResult.Success)
        assertEquals(flow, (result as GetAlbumResult.Success).flow)
    }

    @Test
    fun `returns failure with repository message`() {
        every { repository.getAlbum(3) } throws IllegalStateException("Album source failure")

        val result = useCase(3)

        assertTrue(result is GetAlbumResult.Error)
        assertEquals("Album source failure", (result as GetAlbumResult.Error).error.message)
    }
}
