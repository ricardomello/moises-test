package com.ricardomello.moisestest.navigation

sealed class Screen(val route: String) {
    object Home: Screen("/home")
    object Song : Screen("/song/{songId}?albumId={albumId}") {
        const val ARG_SONG_ID = "songId"
        const val ARG_ALBUM_ID = "albumId"
        fun createRoute(songId: Int, albumId: Int? = null) =
            if (albumId != null) "/song/$songId?albumId=$albumId"
            else "/song/$songId"
    }
    object Album : Screen("/album/{albumId}") {
        const val ARG_ALBUM_ID = "albumId"
        fun createRoute(albumId: Int) = "/album/$albumId"
    }
}