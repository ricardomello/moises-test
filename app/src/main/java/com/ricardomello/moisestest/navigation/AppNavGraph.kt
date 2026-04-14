package com.ricardomello.moisestest.navigation

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.ricardomello.moisestest.navigation.Screen
import com.ricardomello.moisestest.album.presentation.AlbumScreen
import com.ricardomello.moisestest.home.presentation.HomeScreen
import com.ricardomello.moisestest.player.presentation.MiniPlayerViewModel
import com.ricardomello.moisestest.song.presentation.SongScreen
import com.ricardomello.moisestest.ui.components.MiniPlayerBar

private val miniPlayerHeight = 66.dp

@Composable
fun AppNavGraph(navController: NavHostController) {
    val miniPlayerViewModel: MiniPlayerViewModel = hiltViewModel()
    val currentSong by miniPlayerViewModel.currentSong.collectAsStateWithLifecycle()
    val isPlaying by miniPlayerViewModel.isPlaying.collectAsStateWithLifecycle()
    val progress by miniPlayerViewModel.progress.collectAsStateWithLifecycle()

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val onSongScreen = currentRoute?.startsWith("/song/") == true
    val showMiniPlayer = currentSong != null && !onSongScreen

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (showMiniPlayer) miniPlayerHeight else 0.dp),
        ) {
            composable(
                route = Screen.Home.route,
                exitTransition = { slideOutHorizontally { -it } },
                popEnterTransition = { slideInHorizontally { -it } }
            ) {
                HomeScreen(navController = navController)
            }
            composable(
                route = Screen.Song.route,
                arguments = listOf(
                    navArgument(Screen.Song.ARG_SONG_ID) { type = NavType.IntType },
                    navArgument(Screen.Song.ARG_ALBUM_ID) { type = NavType.IntType; defaultValue = -1 }
                ),
                enterTransition = { slideInHorizontally { it } },
                popExitTransition = { slideOutHorizontally { it } }
            ) {
                SongScreen(navController = navController)
            }
            composable(
                route = Screen.Album.route,
                arguments = listOf(
                    navArgument(Screen.Album.ARG_ALBUM_ID) { type = NavType.IntType }
                ),
                enterTransition = { slideInHorizontally { it } },
                popExitTransition = { slideOutHorizontally { it } }
            ) {
                AlbumScreen(navController = navController)
            }
        }

        MiniPlayerBar(
            song = if (showMiniPlayer) currentSong else null,
            isPlaying = isPlaying,
            progress = progress,
            onPlayPause = miniPlayerViewModel::onPlayPause,
            onClose = miniPlayerViewModel::onClose,
            onClick = {
                currentSong?.let { song ->
                    navController.navigate(Screen.Song.createRoute(song.id))
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}
