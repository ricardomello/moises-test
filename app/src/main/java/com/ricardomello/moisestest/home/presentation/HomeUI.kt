package com.ricardomello.moisestest.home.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.ricardomello.moisestest.navigation.Screen
import com.ricardomello.moisestest.shared.domain.Song
import com.ricardomello.moisestest.ui.components.SongArtwork
import com.ricardomello.moisestest.ui.components.SongOptionsBottomSheet
import com.ricardomello.moisestest.ui.theme.MusicArtistText
import com.ricardomello.moisestest.ui.theme.MusicBackground
import com.ricardomello.moisestest.ui.theme.MusicSubdued
import com.ricardomello.moisestest.ui.theme.MusicSurface

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MusicBackground
    ) {
        when (val state = uiState) {
            is HomeState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
            is HomeState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = state.message, color = Color.Red, fontSize = 16.sp)
                }
            }
            is HomeState.Content -> {
                HomeContent(
                    state = state,
                    onQueryChanged = viewModel::onSearchQueryChanged,
                    onLoadMore = viewModel::onLoadMore,
                    onSongClick = { song -> navController.navigate(Screen.Song.createRoute(song.id)) },
                    onMoreClick = viewModel::onSongMoreClick
                )
                state.selectedSong?.let { song ->
                    SongOptionsBottomSheet(
                        song = song,
                        onDismiss = viewModel::onDismissBottomSheet,
                        onViewAlbum = {
                            viewModel.onDismissBottomSheet()
                            navController.navigate(Screen.Album.createRoute(song.albumId))
                        }
                    )
                }
            }
        }
    }
}

@Composable
internal fun HomeContent(
    state: HomeState.Content,
    onQueryChanged: (String) -> Unit,
    onLoadMore: () -> Unit,
    onSongClick: (Song) -> Unit,
    onMoreClick: (Song) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Songs",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier
                .padding(start = 20.dp, top = 24.dp, bottom = 16.dp)
                .testTag("home_songs_title")
        )

        SearchBar(
            query = state.searchQuery,
            onQueryChanged = onQueryChanged,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        when {
            state.isSearching -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
            state.songs.isEmpty() -> EmptyState(isSearchActive = state.searchQuery.isNotBlank())
            else -> {
                val listState = rememberLazyListState()
                val shouldLoadMore by remember {
                    derivedStateOf {
                        val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                        val total = listState.layoutInfo.totalItemsCount
                        total > 0 && lastVisible >= total - 3 && state.canLoadMore && !state.isLoadingMore
                    }
                }
                LaunchedEffect(shouldLoadMore) {
                    if (shouldLoadMore) onLoadMore()
                }
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    items(state.songs, key = { it.id }) { song ->
                        SongItem(
                            song = song,
                            onClick = { onSongClick(song) },
                            onMoreClick = { onMoreClick(song) }
                        )
                    }
                    if (state.isLoadingMore) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BasicTextField(
        value = query,
        onValueChange = onQueryChanged,
        singleLine = true,
        textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
        cursorBrush = SolidColor(Color.White),
        modifier = modifier
            .height(48.dp)
            .background(MusicSurface, RoundedCornerShape(50)),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = MusicSubdued,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.weight(1f)) {
                    if (query.isEmpty()) {
                        Text(
                            text = "Search",
                            color = MusicSubdued,
                            fontSize = 16.sp
                        )
                    }
                    innerTextField()
                }
                if (query.isNotEmpty()) {
                    IconButton(
                        onClick = { onQueryChanged("") },
                        modifier = Modifier
                            .size(20.dp)
                            .testTag("home_clear_search")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear search",
                            tint = MusicSubdued,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun SongItem(song: Song, onClick: () -> Unit, onMoreClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp)
            .testTag("home_song_item_${song.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SongArtwork(artworkUrl = song.artworkUrl, songId = song.id, size = 56.dp)

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.testTag("home_song_title_${song.id}")
            )
            Text(
                text = song.artist,
                color = MusicArtistText,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.testTag("home_song_artist_${song.id}")
            )
        }

        IconButton(
            onClick = onMoreClick,
            modifier = Modifier.testTag("home_song_more_options_${song.id}")
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More options",
                tint = MusicSubdued
            )
        }
    }
}

@Composable
private fun EmptyState(isSearchActive: Boolean) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                tint = MusicSubdued,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (isSearchActive) "No results found" else "Nothing here yet",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.testTag("home_empty_title")
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isSearchActive) "Try a different search term" else "Play a song to see it here",
                color = MusicSubdued,
                fontSize = 14.sp,
                modifier = Modifier.testTag("home_empty_subtitle")
            )
        }
    }
}
