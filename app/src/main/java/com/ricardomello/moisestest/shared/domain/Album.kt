package com.ricardomello.moisestest.shared.domain

data class Album(
    val id: Int,
    val title: String,
    val artist: String,
    val songs: List<Song>,
)
