package com.ricardomello.moisestest.album.data.mapper

import com.ricardomello.moisestest.shared.data.local.SongEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AlbumMapperTest {

    @Test
    fun `toAlbum returns null for empty list`() {
        val album = emptyList<SongEntity>().toAlbum()
        assertNull(album)
    }

    @Test
    fun `toAlbum maps album and songs from first entity`() {
        val entities = listOf(
            SongEntity(
                trackId = 1L,
                trackName = "Track 1",
                artistName = "Artist",
                collectionId = 7L,
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
                collectionId = 7L,
                collectionName = "Album",
                artworkUrl100 = null,
                previewUrl = null,
                primaryGenreName = null,
                trackTimeMillis = null,
                releaseDate = null,
                trackNumber = 2,
            ),
        )

        val album = entities.toAlbum()

        requireNotNull(album)
        assertEquals(7, album.id)
        assertEquals("Album", album.title)
        assertEquals("Artist", album.artist)
        assertEquals(2, album.songs.size)
        assertEquals(1, album.songs.first().id)
    }

    @Test
    fun `toAlbum falls back to trackName when collectionName is null`() {
        val entities = listOf(
            SongEntity(
                trackId = 1L,
                trackName = "Fallback Title",
                artistName = "Artist",
                collectionId = 8L,
                collectionName = null,
                artworkUrl100 = null,
                previewUrl = null,
                primaryGenreName = null,
                trackTimeMillis = null,
                releaseDate = null,
            ),
        )

        val album = entities.toAlbum()

        requireNotNull(album)
        assertEquals("Fallback Title", album.title)
    }
}

