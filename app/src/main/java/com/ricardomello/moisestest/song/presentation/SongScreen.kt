package com.ricardomello.moisestest.song.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.ricardomello.moisestest.navigation.Screen
import com.ricardomello.moisestest.ui.components.SongArtwork
import com.ricardomello.moisestest.ui.components.SongOptionsBottomSheet
import com.ricardomello.moisestest.ui.theme.MusicArtistText
import com.ricardomello.moisestest.ui.theme.MusicBackground
import com.ricardomello.moisestest.ui.theme.MusicControl
import com.ricardomello.moisestest.ui.theme.MusicSubdued
import com.ricardomello.moisestest.util.remainingTimeString
import com.ricardomello.moisestest.util.toTimeString

@Composable
fun SongScreen(
    navController: NavController,
    viewModel: SongViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val albumId = navController.currentBackStackEntry
        ?.arguments?.getInt(Screen.Song.ARG_ALBUM_ID)
        ?.takeIf { it != -1 }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MusicBackground
    ) {
        when (val state = uiState) {
            is SongState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
            is SongState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = state.message, color = Color.Red, fontSize = 16.sp)
                }
            }
            is SongState.Content -> {
                SongContent(
                    state = state,
                    onBack = { navController.popBackStack() },
                    onMoreClick = viewModel::onMoreClick,
                    onPlayPause = viewModel::onPlayPauseToggle,
                    onRepeat = viewModel::onRepeatToggle,
                    onSeek = viewModel::onSeek,
                    onSkipPrev = state.prevSongId?.let { prevId ->
                        {
                            navController.navigate(Screen.Song.createRoute(prevId, albumId)) {
                                popUpTo(Screen.Song.route) {
                                    inclusive = true
                                }
                            }
                        }
                    },
                    onSkipNext = state.nextSongId?.let { nextId ->
                        {
                            navController.navigate(Screen.Song.createRoute(nextId, albumId)) {
                                popUpTo(Screen.Song.route) {
                                    inclusive = true
                                }
                            }
                        }
                    },
                )
                if (state.showOptions) {
                    SongOptionsBottomSheet(
                        song = state.song,
                        onDismiss = viewModel::onDismissOptions,
                        onViewAlbum = {
                            viewModel.onDismissOptions()
                            navController.navigate(Screen.Album.createRoute(state.song.albumId))
                        }
                    )
                }
            }
        }
    }
}

@Composable
internal fun SongContent(
    state: SongState.Content,
    onBack: () -> Unit,
    onMoreClick: () -> Unit,
    onPlayPause: () -> Unit,
    onRepeat: () -> Unit,
    onSeek: (Int) -> Unit,
    onSkipPrev: (() -> Unit)?,
    onSkipNext: (() -> Unit)?,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SongTopBar(onBack = onBack, onMoreClick = onMoreClick)

        Spacer(modifier = Modifier.weight(1f))

        SongArtwork(
            artworkUrl = state.song.artworkUrl,
            songId = state.song.id,
            size = 280.dp,
            cornerRadius = 16.dp,
        )

        Spacer(modifier = Modifier.height(32.dp))

        SongInfoSection(title = state.song.title, artist = state.song.artist)

        Spacer(modifier = Modifier.height(24.dp))

        ProgressSection(
            progressSeconds = state.progressSeconds,
            durationSeconds = state.durationSeconds,
            onSeek = onSeek
        )

        Spacer(modifier = Modifier.height(24.dp))

        ControlsRow(
            isPlaying = state.isPlaying,
            isRepeatOn = state.isRepeatOn,
            onPlayPause = onPlayPause,
            onRepeat = onRepeat,
            onSkipPrev = onSkipPrev,
            onSkipNext = onSkipNext,
        )

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun SongTopBar(onBack: () -> Unit, onMoreClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Text(
                text = "Now playing",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.testTag("song_now_playing_label")
            )
        }
        IconButton(onClick = onMoreClick) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More options",
                tint = Color.White
            )
        }
    }
}

@Composable
private fun SongInfoSection(title: String, artist: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.testTag("song_title")
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = artist,
            color = MusicArtistText,
            fontSize = 16.sp,
            modifier = Modifier.testTag("song_artist")
        )
    }
}

@Composable
private fun ProgressSection(
    progressSeconds: Int,
    durationSeconds: Int,
    onSeek: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Slider(
            value = progressSeconds.toFloat(),
            onValueChange = { onSeek(it.toInt()) },
            valueRange = 0f..durationSeconds.toFloat(),
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color.White,
                inactiveTrackColor = MusicControl
            )
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = progressSeconds.toTimeString(),
                color = MusicArtistText,
                fontSize = 12.sp,
                modifier = Modifier.testTag("song_progress_time")
            )
            Text(
                text = remainingTimeString(progressSeconds, durationSeconds),
                color = MusicArtistText,
                fontSize = 12.sp,
                modifier = Modifier.testTag("song_remaining_time")
            )
        }
    }
}

@Composable
private fun ControlsRow(
    isPlaying: Boolean,
    isRepeatOn: Boolean,
    onPlayPause: () -> Unit,
    onRepeat: () -> Unit,
    onSkipPrev: (() -> Unit)?,
    onSkipNext: (() -> Unit)?,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { onSkipPrev?.invoke() },
            enabled = onSkipPrev != null,
            modifier = Modifier.testTag("song_skip_prev_button")
        ) {
            Icon(
                imageVector = Icons.Filled.SkipPrevious,
                contentDescription = "Skip previous",
                tint = if (onSkipPrev != null) MusicArtistText else MusicSubdued,
                modifier = Modifier.size(36.dp)
            )
        }

        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(MusicControl),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = onPlayPause,
                modifier = Modifier.testTag("song_play_pause_button")
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        IconButton(
            onClick = { onSkipNext?.invoke() },
            enabled = onSkipNext != null,
            modifier = Modifier.testTag("song_skip_next_button")
        ) {
            Icon(
                imageVector = Icons.Filled.SkipNext,
                contentDescription = "Skip next",
                tint = if (onSkipNext != null) MusicArtistText else MusicSubdued,
                modifier = Modifier.size(36.dp)
            )
        }

        IconButton(
            onClick = onRepeat,
            modifier = Modifier.testTag("song_repeat_button")
        ) {
            Icon(
                imageVector = Icons.Filled.Repeat,
                contentDescription = "Repeat",
                tint = if (isRepeatOn) Color.White else MusicSubdued,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
