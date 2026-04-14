package com.ricardomello.moisestest.home.data

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
import org.junit.Assert.assertTrue
import org.junit.Test

class SongRepositoryTest {
    private val remoteDataSource: MusicRemoteDataSource = mockk()
    private val dao: SongDao = mockk(relaxed = true)
    private val repository = SongRepositoryImpl(remoteDataSource, dao)

    @Test
    fun `getSongById maps dao entity to domain`() = runTest {
        every { dao.getById(1L) } returns flowOf(
            SongEntity(
                trackId = 1L,
                trackName = "Track",
                artistName = "Artist",
                collectionId = 3L,
                collectionName = "Album",
                artworkUrl100 = "art",
                previewUrl = "preview",
                primaryGenreName = null,
                trackTimeMillis = null,
                releaseDate = null,
            ),
        )

        val song = repository.getSongById(1).first()

        requireNotNull(song)
        assertEquals(1, song.id)
        assertEquals("Track", song.title)
        assertEquals(3, song.albumId)
    }

    @Test
    fun `getSongById returns null when dao emits null`() = runTest {
        every { dao.getById(99L) } returns flowOf(null)

        val song = repository.getSongById(99).first()

        assertNull(song)
    }

    @Test
    fun `getRecentlyPlayed maps all dao entities`() = runTest {
        every { dao.getRecentlyPlayed() } returns flowOf(
            listOf(
                SongEntity(
                    trackId = 1L,
                    trackName = "A",
                    artistName = "Artist A",
                    collectionId = 5L,
                    collectionName = "Col A",
                    artworkUrl100 = null,
                    previewUrl = null,
                    primaryGenreName = null,
                    trackTimeMillis = null,
                    releaseDate = null,
                ),
                SongEntity(
                    trackId = 2L,
                    trackName = "B",
                    artistName = "Artist B",
                    collectionId = 6L,
                    collectionName = "Col B",
                    artworkUrl100 = null,
                    previewUrl = null,
                    primaryGenreName = null,
                    trackTimeMillis = null,
                    releaseDate = null,
                ),
            ),
        )

        val songs = repository.getRecentlyPlayed().first()

        assertEquals(2, songs.size)
        assertEquals("A", songs[0].title)
        assertEquals("B", songs[1].title)
    }

    @Test
    fun `search fetches remote songs saves entities and returns mapped songs`() = runTest {
        coEvery { remoteDataSource.searchSongs("rock", 10, 20) } returns listOf(
            RemoteSong(
                id = 1L,
                title = "Track",
                artist = "Artist",
                albumId = 2L,
                albumName = "Album",
                artworkUrl = "art",
                previewUrl = "preview",
                genre = "Pop",
                durationMs = 123L,
                releaseDate = "2026-01-01",
                trackNumber = 1,
            ),
        )

        val songs = repository.search("rock", 10)

        assertEquals(1, songs.size)
        assertEquals(1, songs[0].id)
        coVerify(exactly = 1) { remoteDataSource.searchSongs("rock", 10, 20) }
        coVerify(exactly = 1) {
            dao.insertAll(withArg { inserted ->
                assertEquals(1, inserted.size)
                assertEquals(1L, inserted.first().trackId)
            })
        }
    }

    @Test
    fun `search saves empty list when remote returns empty`() = runTest {
        coEvery { remoteDataSource.searchSongs("none", 0, 20) } returns emptyList()

        val songs = repository.search("none", 0)

        assertTrue(songs.isEmpty())
        coVerify(exactly = 1) { dao.insertAll(emptyList()) }
    }

    @Test
    fun `markAsPlayed updates dao with current timestamp`() = runTest {
        repository.markAsPlayed(7)

        coVerify(exactly = 1) {
            dao.updateLastPlayedAt(7L, withArg { timestamp -> assertTrue(timestamp > 0L) })
        }
    }
}

