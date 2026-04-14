package com.ricardomello.moisestest.home.data.mapper

import com.ricardomello.moisestest.shared.data.local.SongEntity
import com.ricardomello.moisestest.shared.data.remote.RemoteSong
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SongMapperTest {

    @Test
    fun `toEntity maps remote song fields`() {
        val remote = RemoteSong(
            id = 10L,
            title = "Track",
            artist = "Artist",
            albumId = 20L,
            albumName = "Album",
            artworkUrl = "art",
            previewUrl = "preview",
            genre = "Pop",
            durationMs = 1234L,
            releaseDate = "2026-01-01",
            trackNumber = 2,
        )

        val entity = remote.toEntity()

        assertEquals(10L, entity.trackId)
        assertEquals("Track", entity.trackName)
        assertEquals("Artist", entity.artistName)
        assertEquals(20L, entity.collectionId)
        assertEquals("Album", entity.collectionName)
        assertEquals("art", entity.artworkUrl100)
        assertEquals("preview", entity.previewUrl)
        assertEquals("Pop", entity.primaryGenreName)
        assertEquals(1234L, entity.trackTimeMillis)
        assertEquals("2026-01-01", entity.releaseDate)
        assertEquals(2, entity.trackNumber)
    }

    @Test
    fun `toDomain maps entity fields`() {
        val entity = SongEntity(
            trackId = 11L,
            trackName = "Song",
            artistName = "Band",
            collectionId = 22L,
            collectionName = "Collection",
            artworkUrl100 = "cover",
            previewUrl = "sample",
            primaryGenreName = "Rock",
            trackTimeMillis = 999L,
            releaseDate = "2026-02-02",
            trackNumber = 3,
            lastPlayedAt = 123L,
        )

        val domain = entity.toDomain()

        assertEquals(11, domain.id)
        assertEquals("Song", domain.title)
        assertEquals("Band", domain.artist)
        assertEquals(22, domain.albumId)
        assertEquals("cover", domain.artworkUrl)
        assertEquals("sample", domain.previewUrl)
    }

    @Test
    fun `toDomain keeps nullable urls`() {
        val entity = SongEntity(
            trackId = 1L,
            trackName = "Song",
            artistName = "Band",
            collectionId = 2L,
            collectionName = null,
            artworkUrl100 = null,
            previewUrl = null,
            primaryGenreName = null,
            trackTimeMillis = null,
            releaseDate = null,
        )

        val domain = entity.toDomain()

        assertNull(domain.artworkUrl)
        assertNull(domain.previewUrl)
    }
}

