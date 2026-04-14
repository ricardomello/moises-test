package com.ricardomello.moisestest.album.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.ricardomello.moisestest.shared.domain.Album
import com.ricardomello.moisestest.shared.domain.Song
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class AlbumContentTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun rendersAlbumAndCallsBackHandlers() {
        val song = sampleSong()
        val album = sampleAlbum(song)
        var backClicks = 0
        var clickedSongId: Int? = null

        composeRule.setContent {
            AlbumContent(
                state = AlbumState.Content(album = album),
                onBack = { backClicks++ },
                onSongClick = { clickedSongId = it.id },
            )
        }

        composeRule.onNodeWithTag("album_title").assertIsDisplayed()
        composeRule.onNodeWithTag("album_artist").assertIsDisplayed()
        composeRule.onNodeWithTag("album_song_title_${song.id}", useUnmergedTree = true).assertIsDisplayed()

        composeRule.onNodeWithTag("album_back_button").performClick()
        assertEquals(1, backClicks)

        composeRule.onNodeWithTag("album_song_item_${song.id}").performClick()
        assertEquals(song.id, clickedSongId)
    }

    @Test
    fun showsRefreshErrorWhenPresent() {
        val album = sampleAlbum(sampleSong())
        val refreshError = "Unable to refresh"

        composeRule.setContent {
            AlbumContent(
                state = AlbumState.Content(album = album, refreshError = refreshError),
                onBack = {},
                onSongClick = {},
            )
        }

        composeRule.onNodeWithTag("album_refresh_error").assertIsDisplayed()
    }

    private fun sampleSong() = Song(
        id = 1,
        title = "Track One",
        artist = "The Artist",
        albumId = 200,
        artworkUrl = null,
        previewUrl = null,
    )

    private fun sampleAlbum(song: Song) = Album(
        id = 200,
        title = "Album Name",
        artist = "The Artist",
        songs = listOf(song),
    )
}
