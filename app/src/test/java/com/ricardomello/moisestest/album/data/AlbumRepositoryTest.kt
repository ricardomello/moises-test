package com.ricardomello.moisestest.album.data

import com.ricardomello.moisestest.shared.data.local.SongDao
import com.ricardomello.moisestest.shared.data.local.SongEntity
import com.ricardomello.moisestest.shared.data.remote.MusicRemoteDataSource
import com.ricardomello.moisestest.shared.data.remote.RemoteSong
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AlbumRepositoryTest {
    private val remoteDataSource: MusicRemoteDataSource = mockk()
    private val dao: SongDao = mockk(relaxed = true)
    private val repository = AlbumRepositoryImpl(remoteDataSource, dao)

    @Test
    fun `getAlbum maps dao entities to album`() = runTest {
        every { dao.getByAlbum(4L) } returns flowOf(
            listOf(
                SongEntity(
                    trackId = 1L,
                    trackName = "Track 1",
                    artistName = "Artist",
                    collectionId = 4L,
                    collectionName = "Album",
                    artworkUrl100 = null,
                    previewUrl = null,
                    primaryGenreName = null,
                    trackTimeMillis = null,
                    releaseDate = null,
                    trackNumber = 1,
                ),
                SongEntity(
                    trackId = 2L,
                    trackName = "Track 2",
                    artistName = "Artist",
                    collectionId = 4L,
                    collectionName = "Album",
                    artworkUrl100 = null,
                    previewUrl = null,
                    primaryGenreName = null,
                    trackTimeMillis = null,
                    releaseDate = null,
                    trackNumber = 2,
                ),
            ),
        )

        val album = repository.getAlbum(4).first()

        requireNotNull(album)
        assertEquals(4, album.id)
        assertEquals("Album", album.title)
        assertEquals(2, album.songs.size)
    }

    @Test
    fun `getAlbum returns null when dao emits empty list`() = runTest {
        every { dao.getByAlbum(8L) } returns flowOf(emptyList())

        val album = repository.getAlbum(8).first()

        assertNull(album)
    }

    @Test
    fun `fetchAndSave replaces album tracks when remote returns data`() = runTest {
        coEvery { remoteDataSource.fetchAlbumTracks(9) } returns listOf(
            RemoteSong(
                id = 100L,
                title = "T1",
                artist = "Artist",
                albumId = 9L,
                albumName = "Album 9",
                artworkUrl = null,
                previewUrl = null,
                genre = null,
                durationMs = null,
                releaseDate = null,
                trackNumber = 1,
            ),
        )

        repository.fetchAndSave(9)

        coVerify(exactly = 1) { dao.deleteByAlbum(9L) }
        coVerify(exactly = 1) {
            dao.insertAll(withArg { inserted ->
                assertEquals(1, inserted.size)
                assertEquals(100L, inserted.first().trackId)
                assertEquals(9L, inserted.first().collectionId)
            })
        }
    }

    @Test
    fun `fetchAndSave does nothing when remote returns empty`() = runTest {
        coEvery { remoteDataSource.fetchAlbumTracks(3) } returns emptyList()

        repository.fetchAndSave(3)

        coVerify(exactly = 0) { dao.deleteByAlbum(any()) }
        coVerify(exactly = 0) { dao.insertAll(any()) }
    }
}

