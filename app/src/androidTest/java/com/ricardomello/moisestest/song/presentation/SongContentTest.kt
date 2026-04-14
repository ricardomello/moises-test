package com.ricardomello.moisestest.song.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.ricardomello.moisestest.shared.domain.Song
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SongContentTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun rendersSongInfoAndInvokesPlaybackCallbacks() {
        val state = SongState.Content(
            song = Song(
                id = 1,
                title = "Now Playing Song",
                artist = "Artist Name",
                albumId = 10,
                artworkUrl = null,
                previewUrl = null,
            ),
            isPlaying = false,
            isRepeatOn = false,
            progressSeconds = 30,
            durationSeconds = 120,
            prevSongId = 2,
            nextSongId = 3,
        )
        var playPauseClicks = 0
        var repeatClicks = 0
        var prevClicks = 0
        var nextClicks = 0

        composeRule.setContent {
            SongContent(
                state = state,
                onBack = {},
                onMoreClick = {},
                onPlayPause = { playPauseClicks++ },
                onRepeat = { repeatClicks++ },
                onSeek = {},
                onSkipPrev = { prevClicks++ },
                onSkipNext = { nextClicks++ },
            )
        }

        composeRule.onNodeWithTag("song_now_playing_label").assertIsDisplayed()
        composeRule.onNodeWithTag("song_title").assertIsDisplayed()
        composeRule.onNodeWithTag("song_artist").assertIsDisplayed()
        composeRule.onNodeWithTag("song_progress_time").assertIsDisplayed()
        composeRule.onNodeWithTag("song_remaining_time").assertIsDisplayed()

        composeRule.onNodeWithTag("song_play_pause_button").performClick()
        composeRule.onNodeWithTag("song_repeat_button").performClick()
        composeRule.onNodeWithTag("song_skip_prev_button").performClick()
        composeRule.onNodeWithTag("song_skip_next_button").performClick()

        assertEquals(1, playPauseClicks)
        assertEquals(1, repeatClicks)
        assertEquals(1, prevClicks)
        assertEquals(1, nextClicks)
    }

    @Test
    fun skipControlsAreDisabledWhenHandlersAreNull() {
        val state = SongState.Content(
            song = Song(
                id = 1,
                title = "Now Playing Song",
                artist = "Artist Name",
                albumId = 10,
                artworkUrl = null,
                previewUrl = null,
            ),
            isPlaying = true,
            isRepeatOn = false,
            progressSeconds = 0,
            durationSeconds = 100,
            prevSongId = null,
            nextSongId = null,
        )

        composeRule.setContent {
            SongContent(
                state = state,
                onBack = {},
                onMoreClick = {},
                onPlayPause = {},
                onRepeat = {},
                onSeek = {},
                onSkipPrev = null,
                onSkipNext = null,
            )
        }

        composeRule.onNodeWithTag("song_skip_prev_button").assertIsNotEnabled()
        composeRule.onNodeWithTag("song_skip_next_button").assertIsNotEnabled()
    }
}
