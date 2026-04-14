package com.ricardomello.moisestest.album.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.ricardomello.moisestest.navigation.Screen
import com.ricardomello.moisestest.shared.domain.Album
import com.ricardomello.moisestest.shared.domain.Song
import com.ricardomello.moisestest.ui.components.SongArtwork
import com.ricardomello.moisestest.ui.theme.MusicArtistText
import com.ricardomello.moisestest.ui.theme.MusicBackground

@Composable
fun AlbumScreen(
    navController: NavController,
    viewModel: AlbumViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MusicBackground
    ) {
        when (val state = uiState) {
            is AlbumState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
            is AlbumState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = state.message, color = Color.Red, fontSize = 16.sp)
                }
            }
            is AlbumState.Content -> {
                AlbumContent(
                    state = state,
                    onBack = { navController.popBackStack() },
                    onSongClick = { song ->
                        navController.navigate(Screen.Song.createRoute(song.id, state.album.id))
                    }
                )
            }
        }
    }
}

@Composable
internal fun AlbumContent(
    state: AlbumState.Content,
    onBack: () -> Unit,
    onSongClick: (Song) -> Unit,
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            AlbumHeader(album = state.album, onBack = onBack)
        }
        if (state.refreshError != null) {
            item {
                Text(
                    text = state.refreshError,
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .testTag("album_refresh_error")
                )
            }
        }
        items(state.album.songs, key = { it.id }) { song ->
            AlbumSongItem(song = song, onClick = { onSongClick(song) })
        }
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AlbumHeader(album: Album, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("album_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Text(
                text = album.title,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            SongArtwork(
                artworkUrl = album.songs.firstOrNull()?.artworkUrl,
                songId = album.id,
                size = 180.dp,
                cornerRadius = 12.dp,
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = album.title,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.testTag("album_title")
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = album.artist,
                color = MusicArtistText,
                fontSize = 14.sp,
                modifier = Modifier.testTag("album_artist")
            )
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}

@Composable
private fun AlbumSongItem(song: Song, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp)
            .testTag("album_song_item_${song.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SongArtwork(
            artworkUrl = song.artworkUrl,
            songId = song.id,
            size = 48.dp,
            cornerRadius = 6.dp,
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.testTag("album_song_title_${song.id}")
            )
            Text(
                text = song.artist,
                color = MusicArtistText,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
