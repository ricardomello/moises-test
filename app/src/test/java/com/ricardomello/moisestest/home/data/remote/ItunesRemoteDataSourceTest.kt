package com.ricardomello.moisestest.home.data.remote

import com.ricardomello.moisestest.home.data.ItunesSearchApi
import com.ricardomello.moisestest.home.data.dto.ItunesSearchResponse
import com.ricardomello.moisestest.home.data.dto.ItunesSearchResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ItunesRemoteDataSourceTest {
    private val api: ItunesSearchApi = mockk()
    private val dataSource = ItunesRemoteDataSource(api)

    @Test
    fun `searchSongs filters items without trackId and maps valid tracks`() = runTest {
        coEvery { api.search(term = "rock", entity = "song", offset = 5, limit = 20) } returns
            ItunesSearchResponse(
                resultCount = 2,
                results = listOf(
                    ItunesSearchResult(
                        trackId = 10L,
                        trackName = "Song",
                        artistName = "Artist",
                        collectionId = 20L,
                        collectionName = "Album",
                        artworkUrl100 = "art",
                        previewUrl = "preview",
                        primaryGenreName = "Pop",
                        trackTimeMillis = 1234L,
                        releaseDate = "2026-01-01",
                        trackNumber = 4,
                    ),
                    ItunesSearchResult(trackId = null, trackName = "invalid"),
                ),
            )

        val songs = dataSource.searchSongs(term = "rock", offset = 5, limit = 20)

        assertEquals(1, songs.size)
        assertEquals(10L, songs.first().id)
        assertEquals("Song", songs.first().title)
        assertEquals(20L, songs.first().albumId)
        coVerify(exactly = 1) { api.search(term = "rock", entity = "song", offset = 5, limit = 20) }
    }

    @Test
    fun `fetchAlbumTracks keeps only track wrapper with non-null trackId`() = runTest {
        coEvery { api.lookup(id = 7L, entity = "song") } returns
            ItunesSearchResponse(
                resultCount = 3,
                results = listOf(
                    ItunesSearchResult(wrapperType = "collection", trackId = 1L),
                    ItunesSearchResult(wrapperType = "track", trackId = null),
                    ItunesSearchResult(
                        wrapperType = "track",
                        trackId = 30L,
                        trackName = "Track",
                        artistName = "Artist",
                        collectionId = 7L,
                    ),
                ),
            )

        val tracks = dataSource.fetchAlbumTracks(7)

        assertEquals(1, tracks.size)
        assertEquals(30L, tracks.first().id)
        coVerify(exactly = 1) { api.lookup(id = 7L, entity = "song") }
    }

    @Test
    fun `searchSongs maps nullable fields to safe defaults`() = runTest {
        coEvery { api.search(term = "x", entity = "song", offset = 0, limit = 20) } returns
            ItunesSearchResponse(
                resultCount = 1,
                results = listOf(
                    ItunesSearchResult(
                        trackId = 1L,
                        trackName = null,
                        artistName = null,
                        collectionId = null,
                    ),
                ),
            )

        val songs = dataSource.searchSongs("x")

        assertEquals(1, songs.size)
        assertEquals("", songs.first().title)
        assertEquals("", songs.first().artist)
        assertEquals(0L, songs.first().albumId)
        assertTrue(songs.first().albumName == null)
    }
}

