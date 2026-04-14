package com.ricardomello.moisestest.home.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.ricardomello.moisestest.shared.domain.Song
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class HomeContentTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun rendersSongsAndCallsMoreOptionsCallback() {
        val song = sampleSong()
        var moreClickCount = 0

        composeRule.setContent {
            HomeContent(
                state = HomeState.Content(
                    songs = listOf(song),
                    searchQuery = "",
                ),
                onQueryChanged = {},
                onLoadMore = {},
                onSongClick = {},
                onMoreClick = { moreClickCount++ },
            )
        }

        composeRule.onNodeWithTag("home_songs_title").assertIsDisplayed()
        composeRule.onNodeWithTag("home_song_title_${song.id}", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithTag("home_song_artist_${song.id}", useUnmergedTree = true).assertIsDisplayed()

        composeRule.onNodeWithTag("home_song_more_options_${song.id}", useUnmergedTree = true).performClick()
        assertEquals(1, moreClickCount)
    }

    @Test
    fun showsNoResultsForActiveSearchWithoutSongs() {
        composeRule.setContent {
            HomeContent(
                state = HomeState.Content(
                    songs = emptyList(),
                    searchQuery = "unmatched query",
                ),
                onQueryChanged = {},
                onLoadMore = {},
                onSongClick = {},
                onMoreClick = {},
            )
        }

        composeRule.onNodeWithTag("home_empty_title").assertIsDisplayed()
        composeRule.onNodeWithTag("home_empty_subtitle").assertIsDisplayed()
    }

    @Test
    fun clearSearchActionEmitsEmptyQuery() {
        var latestQuery = ""

        composeRule.setContent {
            HomeContent(
                state = HomeState.Content(
                    songs = emptyList(),
                    searchQuery = "abc",
                ),
                onQueryChanged = { latestQuery = it },
                onLoadMore = {},
                onSongClick = {},
                onMoreClick = {},
            )
        }

        composeRule.onNodeWithTag("home_clear_search").performClick()
        assertEquals("", latestQuery)
    }

    private fun sampleSong() = Song(
        id = 1,
        title = "Test Song",
        artist = "Test Artist",
        albumId = 10,
        artworkUrl = null,
        previewUrl = null,
    )
}
