package com.ricardomello.moisestest.shared.domain

data class Song(
    val id: Int,
    val title: String,
    val artist: String,
    val albumId: Int,
    val artworkUrl: String? = null,
    val previewUrl: String? = null,
)
